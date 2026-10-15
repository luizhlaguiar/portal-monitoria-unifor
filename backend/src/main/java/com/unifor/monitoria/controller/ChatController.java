package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.ChatMessage;
import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println(">>> WebSocket recebeu mensagem de " + chatMessage.getSenderId() + " para " + chatMessage.getReceiverId());
        try {
            // 1. Enriquecer a mensagem com nomes reais
            Optional<User> senderOpt = userRepository.findById(chatMessage.getSenderId());
            Optional<User> receiverOpt = userRepository.findById(chatMessage.getReceiverId());

            if (senderOpt.isPresent()) {
                chatMessage.setSenderName(senderOpt.get().getNome());
            }
            if (receiverOpt.isPresent()) {
                chatMessage.setReceiverName(receiverOpt.get().getNome());
            }

            // 2. Pub/Sub via Redis: Notifica todos os servidores que uma mensagem foi enviada
            // Isso garante a escalabilidade horizontal
            redisTemplate.convertAndSend("chat-messages", chatMessage);
            System.out.println(">>> Mensagem publicada no Redis para distribuição");

            // 3. Envio imediato via WebSocket para o receptor (Low Latency)
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/messages",
                    chatMessage
            );
            System.out.println(">>> Mensagem enviada instantaneamente via WebSocket");

            // 4. Gravação ASSÍNCRONA no MySQL (Não trava o chat)
            chatService.saveMessageAsync(chatMessage);
            System.out.println(">>> Gravação no banco disparada em background");

        } catch (Exception e) {
            System.err.println(">>> ERRO CRÍTICO ao processar mensagem no WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
