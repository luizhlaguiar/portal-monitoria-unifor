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
    public ResponseEntity<List<DuvidaDTO>> listar(@PathVariable Integer disciplinaId) {
        return ResponseEntity.ok(forumService.listarDuvidasPorDisciplina(disciplinaId));
    }

    @PostMapping("/duvida")
    public ResponseEntity<Duvida> criar(@RequestBody Map<String, Object> payload) {
        Integer alunoId = Integer.valueOf(payload.get("alunoId").toString());
        Integer disciplinaId = Integer.valueOf(payload.get("disciplinaId").toString());
        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        return ResponseEntity.ok(forumService.criarDuvida(alunoId, disciplinaId, titulo, descricao));
    }

    @PostMapping("/duvida/{duvidaId}/resposta")
    public ResponseEntity<RespostaDuvida> responder(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        Integer usuarioId = Integer.valueOf(payload.get("usuarioId").toString());
        String conteudo = (String) payload.get("conteudo");
        Integer respostaPaiId = payload.get("respostaPaiId") != null ? Integer.valueOf(payload.get("respostaPaiId").toString()) : null;

        return ResponseEntity.ok(forumService.responderDuvida(duvidaId, usuarioId, conteudo, respostaPaiId));
    }

    @PatchMapping("/duvida/{duvidaId}/status")
    public ResponseEntity<Duvida> atualizarStatusDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(forumService.atualizarStatusDuvida(duvidaId, payload.get("status")));
    }

    @PutMapping("/duvida/{duvidaId}")
    public ResponseEntity<Duvida> editarDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        Integer usuarioId = Integer.valueOf(payload.get("usuarioId").toString());
        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        return ResponseEntity.ok(forumService.editarDuvida(duvidaId, usuarioId, titulo, descricao));
    }

    @DeleteMapping("/resposta/{respostaId}")
    public ResponseEntity<Void> excluirResposta(@PathVariable Integer respostaId, @RequestBody Map<String, Object> payload) {
        Integer usuarioId = Integer.valueOf(payload.get("usuarioId").toString());
        forumService.excluirResposta(respostaId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/resposta/{respostaId}")
    public ResponseEntity<RespostaDuvida> editarResposta(@PathVariable Integer respostaId, @RequestBody Map<String, Object> payload) {
        Integer usuarioId = Integer.valueOf(payload.get("usuarioId").toString());
        String conteudo = (String) payload.get("conteudo");

        return ResponseEntity.ok(forumService.editarResposta(respostaId, usuarioId, conteudo));
    }

    @DeleteMapping("/duvida/{duvidaId}")
    public ResponseEntity<Void> excluirDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        Integer usuarioId = Integer.valueOf(payload.get("usuarioId").toString());
        forumService.excluirDuvida(duvidaId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
