package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
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
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        return messageRepository.save(message);
    }

    public Page<Message> getChatHistory(Integer userId, Integer receiverId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = getOrCreateConversation(user, receiver);
        return messageRepository.findByConversationIdOrderByTimestampDesc(conversation.getId(), PageRequest.of(page, size));
    }

    public List<User> getConversationsForMonitor(Integer monitorId) {
        User monitor = userRepository.findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        return conversationRepository.findByMonitor(monitor).stream()
                .map(Conversation::getUser)
                .collect(Collectors.toList());
    }

    public void markAsRead(Integer messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setRead(true);
            messageRepository.save(m);
        });
    }

    private Conversation getOrCreateConversation(User user, User monitor) {
        return conversationRepository.findByUserAndMonitor(user, monitor)
                .orElseGet(() -> conversationRepository.findByUserAndMonitor(monitor, user)
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setUser(user);
                    conv.setMonitor(monitor);
                    return conversationRepository.save(conv);
                }));
    }
}
