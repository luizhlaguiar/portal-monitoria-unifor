package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.MatriculaDao;
import com.unifor.monitoria.model.ChatMessage;
import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {
    @Autowired
    private ChatService chatService;

    @Autowired
    private MatriculaDao matriculaDao;

    @Autowired
    private AccessService access;

    @GetMapping("/history/{userId}/{receiverId}")
    public ResponseEntity<List<Message>> getHistory(
            @PathVariable Integer userId,
            @PathVariable Integer receiverId,
            @RequestParam(required = false) Integer disciplineId) {
        access.requireSelf(CurrentUser.require(), userId);
        try {
            List<Message> history = chatService.getChatHistoryList(userId, receiverId, disciplineId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<User>> getConversations(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer disciplineId) {
        access.requireSelf(CurrentUser.require(), userId);
        try {
            List<User> users = chatService.getConversationsForUser(userId, disciplineId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/students/{disciplineId}")
    public ResponseEntity<List<User>> getStudentsByDiscipline(@PathVariable Integer disciplineId) {
        access.requireDisciplineAccess(CurrentUser.require(), disciplineId);
        try {
            List<User> students = matriculaDao.findStudentsByDisciplineId(disciplineId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody ChatMessage chatMessage) {
        AuthUser me = CurrentUser.require();
        chatMessage.setSenderId(me.id());
        try {
            Message saved = chatService.sendMessage(chatMessage);
            Map<String, Object> body = new HashMap<>();
            body.put("id", saved.getId());
            body.put("senderId", saved.getSender().getId());
            body.put("receiverId", saved.getReceiver().getId());
            body.put("content", saved.getContent());
            body.put("timestamp", saved.getTimestamp());
            body.put("disciplineId", chatMessage.getDisciplineId());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Nao foi possivel salvar a mensagem."));
        }
    }

    @PostMapping("/read/{messageId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer messageId) {
        AuthUser me = CurrentUser.require();
        boolean ok = chatService.markAsRead(messageId, me.id());
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }
}
