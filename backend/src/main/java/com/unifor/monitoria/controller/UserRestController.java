package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserRestController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ChatService chatService;

    @GetMapping
    public ResponseEntity<List<User>> getContacts() {
        AuthUser me = CurrentUser.require();
        return userDao.findById(me.id())
                .map(user -> ResponseEntity.ok(chatService.getContacts(user)))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}
