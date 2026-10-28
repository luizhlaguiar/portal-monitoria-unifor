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
        return conversationRepository.findBetweenUsers(user, monitor)
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setUser(user);
                    conv.setMonitor(monitor);
                    return conversationRepository.save(conv);
                });
    }
}