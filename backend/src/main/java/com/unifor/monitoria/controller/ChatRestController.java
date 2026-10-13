package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/history/{userId}/{receiverId}")
    public ResponseEntity<Page<Message>> getHistory(
            @PathVariable Integer userId,
            @PathVariable Integer receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<Message> history = chatService.getChatHistory(userId, receiverId, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/conversations/{monitorId}")
    public ResponseEntity<List<User>> getConversations(@PathVariable Integer monitorId) {
        try {
            List<User> users = chatService.getConversationsForMonitor(monitorId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/read/{messageId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer messageId) {
        chatService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }
}
