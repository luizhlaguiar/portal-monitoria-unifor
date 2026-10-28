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
        System.out.println(">>> [WebSocket Controller] RECEBENDO MENSAGEM: " + chatMessage.getContent());
        if (chatMessage.getArquivoUrl() != null) {
            System.out.println(">>> [WebSocket Controller] ARQUIVO DETECTADO: " + chatMessage.getArquivoUrl());
        }

        try {
            Optional<User> senderOpt = userRepository.findById(chatMessage.getSenderId());
            Optional<User> receiverOpt = userRepository.findById(chatMessage.getReceiverId());

            if (senderOpt.isPresent()) chatMessage.setSenderName(senderOpt.get().getNome());
            if (receiverOpt.isPresent()) chatMessage.setReceiverName(receiverOpt.get().getNome());

            // 1. Publicar no Redis para outros servidores
            redisTemplate.convertAndSend("chat-messages", chatMessage);

            // 2. ENVIO PARA O USUÁRIO
            messagingTemplate.convertAndSend("/topic/messages", chatMessage);

            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/messages",
                    chatMessage
            );

            System.out.println(">>> [WebSocket Controller] Mensagem disparada com sucesso");

            // 3. Salvar no banco assincronamente
            chatService.saveMessageAsync(chatMessage);

        } catch (Exception e) {
            System.err.println(">>> [WebSocket Controller] ERRO CRÍTICO AO PROCESSAR MENSAGEM: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
