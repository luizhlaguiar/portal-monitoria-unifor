package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/{disciplinaId}")
    public ResponseEntity<?> listar(@PathVariable Integer disciplinaId) {
        return ResponseEntity.ok(forumService.listarDuvidasPorDisciplina(disciplinaId));
    }

    @PostMapping("/duvida")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        Integer alunoId = parseInteger(payload.get("alunoId"));
        Integer disciplinaId = parseInteger(payload.get("disciplinaId"));
        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        if (alunoId == null || disciplinaId == null) {
            return ResponseEntity.badRequest().build();
        }

        Object novaDuvida = forumService.criarDuvida(alunoId, disciplinaId, titulo, descricao);
        return ResponseEntity.ok(novaDuvida);
    }

    @PostMapping("/duvida/{duvidaId}/resposta")
    public ResponseEntity<?> responder(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        Integer usuarioId = parseInteger(payload.get("usuarioId"));
        String conteudo = (String) payload.get("conteudo");
        Integer respostaPaiId = parseInteger(payload.get("respostaPaiId"));

        if (usuarioId == null) {
            return ResponseEntity.badRequest().build();
        }

        Object resposta = forumService.responderDuvida(duvidaId, usuarioId, conteudo, respostaPaiId);
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/duvida/{duvidaId}/status")
    public ResponseEntity<?> atualizarStatusDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("status")) return ResponseEntity.badRequest().build();
        Object updated = forumService.atualizarStatusDuvida(duvidaId, payload.get("status"));
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/duvida/{duvidaId}")
    public ResponseEntity<?> editarDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        Integer usuarioId = parseInteger(payload.get("usuarioId"));
        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        if (usuarioId == null) {
            return ResponseEntity.badRequest().build();
        }

        Object editada = forumService.editarDuvida(duvidaId, usuarioId, titulo, descricao);
        return ResponseEntity.ok(editada);
    }

    @DeleteMapping("/resposta/{respostaId}")
    public ResponseEntity<Void> excluirResposta(@PathVariable Integer respostaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        Integer usuarioId = parseInteger(payload.get("usuarioId"));
        if (usuarioId == null) {
            return ResponseEntity.badRequest().build();
        }
        forumService.excluirResposta(respostaId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/resposta/{respostaId}")
    public ResponseEntity<?> editarResposta(@PathVariable Integer respostaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        Integer usuarioId = parseInteger(payload.get("usuarioId"));
        String conteudo = (String) payload.get("conteudo");

        if (usuarioId == null) {
            return ResponseEntity.badRequest().build();
        }

        Object editada = forumService.editarResposta(respostaId, usuarioId, conteudo);
        return ResponseEntity.ok(editada);
    }

    @DeleteMapping("/duvida/{duvidaId}")
    public ResponseEntity<Void> excluirDuvida(@PathVariable Integer duvidaId, @RequestParam Integer usuarioId) {
        if (usuarioId == null) return ResponseEntity.badRequest().build();

        forumService.excluirDuvida(duvidaId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
