package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Async
    public void saveMessageAsync(ChatMessage chatMessage) {
        System.out.println(">>> [ChatService ASYNC] Iniciando salvamento no banco para a mensagem: " + chatMessage.getContent());
        try {
            User sender = userRepository.findById(chatMessage.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found: " + chatMessage.getSenderId()));
            User receiver = userRepository.findById(chatMessage.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found: " + chatMessage.getReceiverId()));

            Conversation conversation = getOrCreateConversation(sender, receiver);

            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(chatMessage.getContent());
            message.setArquivoUrl(chatMessage.getArquivoUrl());
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);

            messageRepository.save(message);
            System.out.println(">>> [ChatService ASYNC] SUCESSO: Mensagem salva no MySQL ID: " + message.getId());
        } catch (Exception e) {
            System.err.println(">>> [ChatService ASYNC] ERRO ao salvar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Message saveMessage(ChatMessage chatMessage) {
        User sender = userRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(chatMessage.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = getOrCreateConversation(sender, receiver);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(chatMessage.getContent());
        message.setArquivoUrl(chatMessage.getArquivoUrl());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        return messageRepository.save(message);
    }

    public List<Message> getChatHistoryList(Integer userId, Integer receiverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Optional<Conversation> convOpt = conversationRepository.findBetweenUsers(user, receiver);

        if (!convOpt.isPresent()) {
            return Collections.emptyList();
        }

        return messageRepository.findByConversationIdOrderByTimestampAsc(convOpt.get().getId());
    }

    public List<UserDTO> getConversationsForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("MONITOR".equals(user.getRole())) {
            return conversationRepository.findByMonitor(user).stream()
                    .map(Conversation::getUser)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(u -> new UserDTO(u.getId(), u.getNome(), u.getEmail()))
                    .collect(Collectors.toList());
        } else {
            return conversationRepository.findByUser(user).stream()
                    .map(Conversation::getMonitor)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(u -> new UserDTO(u.getId(), u.getNome(), u.getEmail()))
                    .collect(Collectors.toList());
        }
    }

    public List<UserDTO> getConversationsForMonitor(Integer monitorId) {
        return getConversationsForUser(monitorId);
    }

    public List<UserDTO> getAvailableMonitors() {
        return userRepository.findByRole("MONITOR").stream()
                .map(u -> new UserDTO(u.getId(), u.getNome(), u.getEmail()))
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAvailableAlunos() {
        return userRepository.findByRole("ALUNO").stream()
                .map(u -> new UserDTO(u.getId(), u.getNome(), u.getEmail()))
                .collect(Collectors.toList());
    }

    public void markAsRead(Integer messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setRead(true);
            messageRepository.save(m);
        });
    }

    private Conversation getOrCreateConversation(User user, User monitor) {
        return conversationRepository.findBetweenUsers(user, monitor)
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setUser(user);
                    conv.setMonitor(monitor);
                    return conversationRepository.save(conv);
                });
    }
}