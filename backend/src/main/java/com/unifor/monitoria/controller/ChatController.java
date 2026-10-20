package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.ChatMessage;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
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
        System.out.println(">>> [WebSocket Controller] RECEBENDO MENSAGEM...");

        try {
            Optional<User> senderOpt = userRepository.findById(chatMessage.getSenderId());
            Optional<User> receiverOpt = userRepository.findById(chatMessage.getReceiverId());

            if (senderOpt.isPresent()) chatMessage.setSenderName(senderOpt.get().getNome());
            if (receiverOpt.isPresent()) chatMessage.setReceiverName(receiverOpt.get().getNome());

            // 1. Publicar no Redis para outros servidores
            redisTemplate.convertAndSend("chat-messages", chatMessage);

            // 2. ENVIO PARA O USUÁRIO
            // Usamos /topic/messages para garantir a entrega instantânea sem depender de autenticação de sessão do Spring
            // O cliente filtrará no frontend se a mensagem é para ele.
            messagingTemplate.convertAndSend("/topic/messages", chatMessage);

            // Mantemos o envio para a fila privada como redundância
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/messages",
                    chatMessage
            );

            System.out.println(">>> [WebSocket Controller] Mensagem disparada via /topic/messages e /user/queue");

            // 3. Salvar no banco assincronamente
            chatService.saveMessageAsync(chatMessage);

        } catch (Exception e) {
            System.err.println(">>> [WebSocket Controller] ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
