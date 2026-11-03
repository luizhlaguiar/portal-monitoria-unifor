package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.ChatMessage;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {
    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        AuthUser me = CurrentUser.fromPrincipal(principal);
        if (me == null) {
            System.err.println(">>> [WebSocket Controller] mensagem sem usuario autenticado descartada");
            return;
        }
        try {
            chatMessage.setSenderId(me.id());
            chatService.sendMessage(chatMessage);
        } catch (Exception e) {
            System.err.println(">>> [WebSocket Controller] ERRO AO PROCESSAR MENSAGEM: " + e.getMessage());
        }
    }
}
