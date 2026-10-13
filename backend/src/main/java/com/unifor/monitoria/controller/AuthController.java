package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.User;
import com.unifor.monitoria.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String matricula = loginRequest.get("matricula");
            String senha = loginRequest.get("senha");

            if (matricula == null || senha == null) {
                return ResponseEntity.badRequest().body("Matrícula e senha são obrigatórias");
            }

            Optional<User> userOpt = userRepository.findByMatricula(matricula);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getSenha().equals(senha)) {
                    Map<String, Object> response = Map.of(
                        "id", user.getId(),
                        "nome", user.getNome(),
                        "role", user.getRole().toString()
                    );
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno no servidor: " + e.getMessage());
        }
    }
}
