package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    @Autowired
    private ConversationDao conversationDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DisciplineDao disciplineDao;

    @Autowired
    private MatriculaDao matriculaDao;

    @Autowired
    private MonitoringSessionDao monitoringSessionDao;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Async
    public void saveMessageAsync(ChatMessage chatMessage) {
        try {
            saveMessageSync(chatMessage);
        } catch (Exception e) {
            System.err.println("Erro ao salvar mensagem assincronamente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fluxo completo de envio: valida, GRAVA NO BANCO e so entao entrega ao destinatario em tempo real.
     * Se a gravacao falhar, lanca excecao (nada e entregue e o remetente e avisado).
     */
    public Message sendMessage(ChatMessage chatMessage) {
        Message saved = saveMessageSync(chatMessage);

        chatMessage.setSenderName(saved.getSender().getNome());
        chatMessage.setReceiverName(saved.getReceiver().getNome());
        chatMessage.setTimestamp(saved.getTimestamp());

        try {
            messagingTemplate.convertAndSend("/topic/chat." + chatMessage.getReceiverId(), chatMessage);
        } catch (Exception e) {
            System.err.println("Mensagem gravada, mas falhou a entrega em tempo real: " + e.getMessage());
        }
        try {
            redisTemplate.convertAndSend("chat-messages", chatMessage);
        } catch (Exception e) {
            System.err.println("Mensagem gravada, mas falhou a publicacao no Redis: " + e.getMessage());
        }
        return saved;
    }

    public Message saveMessageSync(ChatMessage chatMessage) {
        if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null) {
            throw new IllegalArgumentException("Remetente e destinatario sao obrigatorios");
        }
        if (chatMessage.getSenderId().equals(chatMessage.getReceiverId())) {
            throw new IllegalArgumentException("Nao e possivel enviar mensagem para si mesmo");
        }
        MessageContentValidator.validate(chatMessage.getContent());

        User sender = userDao.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Remetente nao encontrado"));
        User receiver = userDao.findById(chatMessage.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Destinatario nao encontrado"));

        Integer disciplineId = chatMessage.getDisciplineId();
        if (hasDiscipline(disciplineId)) {
            if (!belongsToDiscipline(sender, disciplineId)) {
                throw new IllegalArgumentException("Voce nao participa desta disciplina");
            }
            if (!belongsToDiscipline(receiver, disciplineId)) {
                throw new IllegalArgumentException("O destinatario nao esta matriculado nesta disciplina");
            }
        } else if (!canChatWith(sender, receiver)) {
            throw new IllegalArgumentException("Voce so pode conversar com quem participa de uma das suas disciplinas");
        }

        return transactionTemplate.execute(status -> {
            Conversation conversation = getOrCreateConversation(sender, receiver, disciplineId);

            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(chatMessage.getContent());
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);

            return messageDao.save(message);
        });
    }

    /**
     * Contatos permitidos: quem divide ao menos uma disciplina com o usuario (como aluno ou monitor).
     * Admin ve todo mundo. Nao inclui o proprio usuario.
     */
    public List<User> getContacts(User me) {
        if (me.getRole() == User.Role.ADMIN) {
            return userDao.findAll().stream()
                    .filter(u -> !u.getId().equals(me.getId()))
                    .collect(Collectors.toList());
        }
        Set<Integer> disciplineIds = new LinkedHashSet<>();
        disciplineDao.findDisciplinesByStudentId(me.getId()).forEach(d -> disciplineIds.add(d.getId()));
        disciplineDao.findDisciplinesByMonitorId(me.getId()).forEach(d -> disciplineIds.add(d.getId()));

        Map<Integer, User> contacts = new LinkedHashMap<>();
        for (Integer id : disciplineIds) {
            matriculaDao.findStudentsByDisciplineId(id).forEach(u -> contacts.putIfAbsent(u.getId(), u));
            monitoringSessionDao.findByDisciplineId(id).forEach(s -> {
                if (s.getMonitor() != null) contacts.putIfAbsent(s.getMonitor().getId(), s.getMonitor());
            });
        }
        contacts.remove(me.getId());
        return new ArrayList<>(contacts.values());
    }

    private boolean canChatWith(User sender, User receiver) {
        if (sender.getRole() == User.Role.ADMIN || receiver.getRole() == User.Role.ADMIN) return true;
        return getContacts(sender).stream().anyMatch(u -> u.getId().equals(receiver.getId()));
    }

    private boolean belongsToDiscipline(User user, Integer disciplineId) {
        if (user.getRole() == User.Role.ADMIN) return true;
        return matriculaDao.existsByAlunoIdAndDisciplinaId(user.getId(), disciplineId)
                || monitoringSessionDao.existsByMonitorAndDiscipline(user.getId(), disciplineId);
    }

    public List<Message> getChatHistoryList(Integer userId, Integer receiverId) {
        return getChatHistoryList(userId, receiverId, null);
    }

    public List<Message> getChatHistoryList(Integer userId, Integer receiverId, Integer disciplineId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User receiver = userDao.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        List<Conversation> conversations = hasDiscipline(disciplineId)
                ? conversationDao.findBetweenUsersInDiscipline(user, receiver, disciplineId)
                : conversationDao.findAllBetweenUsers(user, receiver);

        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ids = conversations.stream().map(Conversation::getId).collect(Collectors.toList());
        return messageDao.findByConversationIdInOrderByTimestampAsc(ids);
    }

    public List<User> getConversationsForUser(Integer userId) {
        return getConversationsForUser(userId, null);
    }

    public List<User> getConversationsForUser(Integer userId, Integer disciplineId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> conversations = hasDiscipline(disciplineId)
                ? conversationDao.findByUserInDiscipline(user, disciplineId)
                : conversationDao.findAllConversationsByUser(user);

        Map<Integer, User> unique = new LinkedHashMap<>();
        for (Conversation conv : conversations) {
            User other = conv.getUser().getId().equals(userId) ? conv.getMonitor() : conv.getUser();
            if (other == null || other.getId().equals(userId)) continue;
            unique.putIfAbsent(other.getId(), other);
        }
        return new ArrayList<>(unique.values());
    }

    public List<User> getConversationsForMonitor(Integer monitorId) {
        User monitor = userDao.findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        return conversationDao.findByMonitor(monitor).stream()
                .map(Conversation::getUser)
                .collect(Collectors.toList());
    }

    public void markAsRead(Integer messageId) {
        messageDao.findById(messageId).ifPresent(m -> {
            m.setRead(true);
            messageDao.save(m);
        });
    }

    public boolean markAsRead(Integer messageId, Integer userId) {
        Optional<Message> found = messageDao.findById(messageId);
        if (found.isEmpty()) return false;
        Message m = found.get();
        if (m.getReceiver() == null || !m.getReceiver().getId().equals(userId)) return false;
        m.setRead(true);
        messageDao.save(m);
        return true;
    }

    private boolean hasDiscipline(Integer disciplineId) {
        return disciplineId != null && disciplineId > 0;
    }

    private Conversation getOrCreateConversation(User user, User monitor, Integer disciplineId) {
        List<Conversation> found = hasDiscipline(disciplineId)
                ? conversationDao.findBetweenUsersInDiscipline(user, monitor, disciplineId)
                : conversationDao.findBetweenUsersWithoutDiscipline(user, monitor);
        if (!found.isEmpty()) {
            return found.get(0);
        }

        Conversation conv = new Conversation();
        if (user.getRole() == User.Role.MONITOR) {
            conv.setUser(monitor);
            conv.setMonitor(user);
        } else {
            conv.setUser(user);
            conv.setMonitor(monitor);
        }
        if (hasDiscipline(disciplineId)) {
            conv.setDiscipline(disciplineDao.findById(disciplineId).orElse(null));
        }
        return conversationDao.save(conv);
    }
}
