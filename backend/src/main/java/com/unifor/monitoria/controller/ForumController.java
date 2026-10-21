package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.AnexoForumRepository;
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

    @Autowired
    private AnexoForumRepository anexoForumRepository;

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
        anexoForumRepository.deleteByReferenciaIdAndTipo(duvidaId.longValue(), AnexoForum.TipoAnexo.DUVIDA);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/anexo")
    public ResponseEntity<?> adicionarAnexo(@RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();

        String url = (String) payload.get("url");
        Long referenciaId = Long.valueOf(payload.get("referenciaId").toString());
        String tipoStr = (String) payload.get("tipo");

        if (url == null || referenciaId == null || tipoStr == null) {
            return ResponseEntity.badRequest().build();
        }

        AnexoForum anexo = new AnexoForum();
        anexo.setUrl(url);
        anexo.setReferenciaId(referenciaId);
        anexo.setTipo(AnexoForum.TipoAnexo.valueOf(tipoStr));

        return ResponseEntity.ok(anexoForumRepository.save(anexo));
    }

    @GetMapping("/anexos/{referenciaId}/{tipo}")
    public ResponseEntity<?> listarAnexos(@PathVariable Long referenciaId, @PathVariable String tipo) {
        return ResponseEntity.ok(anexoForumRepository.findByReferenciaIdAndTipo(referenciaId, AnexoForum.TipoAnexo.valueOf(tipo)));
    }

    @DeleteMapping("/anexo/{id}")
    public ResponseEntity<Void> excluirAnexo(@PathVariable Long id) {
        anexoForumRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/anexo/{id}")
    public ResponseEntity<?> atualizarAnexo(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("url")) return ResponseEntity.badRequest().build();

        return anexoForumRepository.findById(id)
                .map(anexo -> {
                    anexo.setUrl(payload.get("url"));
                    return ResponseEntity.ok(anexoForumRepository.save(anexo));
                })
                .orElse(ResponseEntity.notFound().build());
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
