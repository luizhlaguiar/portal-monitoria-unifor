package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.ChatMessage;
import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private com.unifor.monitoria.repository.UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println(">>> WebSocket recebeu mensagem de " + chatMessage.getSenderId() + " para " + chatMessage.getReceiverId());
        try {
            // Busca nomes reais dos usuários para enviar no WebSocket
            Optional<User> senderOpt = userRepository.findById(chatMessage.getSenderId());
            Optional<User> receiverOpt = userRepository.findById(chatMessage.getReceiverId());

            if (senderOpt.isPresent()) {
                chatMessage.setSenderName(senderOpt.get().getNome());
            }
            if (receiverOpt.isPresent()) {
                chatMessage.setReceiverName(receiverOpt.get().getNome());
            }

            // Save to DB
            Message savedMessage = chatService.saveMessage(chatMessage);
            System.out.println(">>> Mensagem salva com sucesso no banco: " + savedMessage.getId());

            // Send to private queue: /user/{userId}/queue/messages
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/messages",
                    savedMessage
            );
            System.out.println(">>> Mensagem enviada para a fila do receptor via WebSocket");
        } catch (Exception e) {
            System.err.println(">>> ERRO CRÍTICO ao processar mensagem no WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
