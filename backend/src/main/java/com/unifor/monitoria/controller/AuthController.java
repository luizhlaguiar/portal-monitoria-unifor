package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.security.JwtService;
import com.unifor.monitoria.security.LoginAttemptService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LoginAttemptService loginAttempts;

    private String dummyHash;

    @PostConstruct
    void init() {
        dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        try {
            String matricula = loginRequest.get("matricula");
            String senha = loginRequest.get("senha");

            if (matricula == null || senha == null) {
                return ResponseEntity.badRequest().body("Matrícula e senha são obrigatórias");
            }

            String ip = request.getRemoteAddr();
            long blocked = loginAttempts.secondsBlocked(ip, matricula);
            if (blocked > 0) {
                long minutes = Math.max(1, (blocked + 59) / 60);
                return ResponseEntity.status(429)
                        .header("Retry-After", String.valueOf(blocked))
                        .body("Muitas tentativas de login. Tente novamente em " + minutes + " minuto(s).");
            }

            Optional<User> userOpt = userDao.findByMatricula(matricula);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (checkPassword(user, senha)) {
                    loginAttempts.recordSuccess(ip, matricula);
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("id", user.getId());
                    response.put("nome", user.getNome());
                    response.put("role", user.getRole().toString());
                    response.put("token", jwtService.generate(user));
                    return ResponseEntity.ok(response);
                }
            } else {
                passwordEncoder.matches(senha, dummyHash);
            }

            loginAttempts.recordFailure(ip, matricula);
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno no servidor");
        }
    }

    /** Dados do usuario do token (o front usa para conferir o papel real em vez de confiar no localStorage). */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        AuthUser me = CurrentUser.require();
        return userDao.findById(me.id())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "nome", u.getNome(),
                        "role", u.getRole().toString())))
                .orElse(ResponseEntity.status(401).build());
    }

    private boolean checkPassword(User user, String rawPassword) {
        String stored = user.getSenha();
        if (stored == null) return false;
        if (stored.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        boolean ok = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8), rawPassword.getBytes(StandardCharsets.UTF_8));
        if (ok) {
            user.setSenha(passwordEncoder.encode(rawPassword));
            userDao.save(user);
        }
        return ok;
    }
}
