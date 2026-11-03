package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.AnexoForumDao;
import com.unifor.monitoria.dao.DuvidaDao;
import com.unifor.monitoria.dao.RespostaDuvidaDao;
import com.unifor.monitoria.model.AnexoForum;
import com.unifor.monitoria.model.Duvida;
import com.unifor.monitoria.model.RespostaDuvida;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.security.UrlSafety;
import com.unifor.monitoria.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Fórum de dúvidas. Quem age é SEMPRE o usuário do token: os campos alunoId/usuarioId que o front
 * ainda manda no corpo são ignorados. Leitura e escrita exigem fazer parte da disciplina
 * (aluno matriculado, monitor dela ou admin).
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private AnexoForumDao anexoForumDao;

    @Autowired
    private DuvidaDao duvidaDao;

    @Autowired
    private RespostaDuvidaDao respostaDao;

    @Autowired
    private AccessService access;

    @GetMapping("/{disciplinaId}")
    public ResponseEntity<?> listar(@PathVariable Integer disciplinaId) {
        access.requireDisciplineAccess(CurrentUser.require(), disciplinaId);
        return ResponseEntity.ok(forumService.listarDuvidasPorDisciplina(disciplinaId));
    }

    @PostMapping("/duvida")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        Integer disciplinaId = parseInteger(payload.get("disciplinaId"));
        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        if (disciplinaId == null) {
            return ResponseEntity.badRequest().build();
        }
        access.requireDisciplineAccess(me, disciplinaId);

        Object novaDuvida = forumService.criarDuvida(me.id(), disciplinaId, titulo, descricao);
        return ResponseEntity.ok(novaDuvida);
    }

    @PostMapping("/duvida/{duvidaId}/resposta")
    public ResponseEntity<?> responder(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        Duvida duvida = findDuvida(duvidaId);
        access.requireDisciplineAccess(me, duvida.getDisciplina().getId());

        String conteudo = (String) payload.get("conteudo");
        Integer respostaPaiId = parseInteger(payload.get("respostaPaiId"));

        Object resposta = forumService.responderDuvida(duvidaId, me.id(), conteudo, respostaPaiId);
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/duvida/{duvidaId}/status")
    public ResponseEntity<?> atualizarStatusDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("status")) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        Duvida duvida = findDuvida(duvidaId);
        boolean autor = duvida.getAluno() != null && me.id().equals(duvida.getAluno().getId());
        if (!autor && !access.isMonitorOf(me, duvida.getDisciplina().getId())) {
            throw AccessService.forbidden();
        }
        Object updated = forumService.atualizarStatusDuvida(duvidaId, payload.get("status"));
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/duvida/{duvidaId}")
    public ResponseEntity<?> editarDuvida(@PathVariable Integer duvidaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        String titulo = (String) payload.get("titulo");
        String descricao = (String) payload.get("descricao");

        Object editada = forumService.editarDuvida(duvidaId, me.id(), titulo, descricao);
        return ResponseEntity.ok(editada);
    }

    @DeleteMapping("/resposta/{respostaId}")
    public ResponseEntity<Void> excluirResposta(@PathVariable Integer respostaId) {
        AuthUser me = CurrentUser.require();
        forumService.excluirResposta(respostaId, me.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/resposta/{respostaId}")
    public ResponseEntity<?> editarResposta(@PathVariable Integer respostaId, @RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        String conteudo = (String) payload.get("conteudo");

        Object editada = forumService.editarResposta(respostaId, me.id(), conteudo);
        return ResponseEntity.ok(editada);
    }

    @DeleteMapping("/duvida/{duvidaId}")
    public ResponseEntity<Void> excluirDuvida(@PathVariable Integer duvidaId) {
        AuthUser me = CurrentUser.require();

        forumService.excluirDuvida(duvidaId, me.id());
        anexoForumDao.deleteByReferenciaIdAndTipo(duvidaId.longValue(), AnexoForum.TipoAnexo.DUVIDA);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/anexo")
    public ResponseEntity<?> adicionarAnexo(@RequestBody Map<String, Object> payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();

        Object urlObj = payload.get("url");
        Object refObj = payload.get("referenciaId");
        Object tipoObj = payload.get("tipo");
        if (!(urlObj instanceof String url) || refObj == null || !(tipoObj instanceof String tipoStr)) {
            return ResponseEntity.badRequest().build();
        }
        if (!UrlSafety.isSafeHttpUrl(url)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de anexo invalida");
        }
        Long referenciaId = parseLong(refObj);
        AnexoForum.TipoAnexo tipo = parseTipo(tipoStr);
        if (referenciaId == null || tipo == null) return ResponseEntity.badRequest().build();

        requireAnexoOwner(me, tipo, referenciaId);

        AnexoForum anexo = new AnexoForum();
        anexo.setUrl(url);
        anexo.setReferenciaId(referenciaId);
        anexo.setTipo(tipo);

        return ResponseEntity.ok(anexoForumDao.save(anexo));
    }

    @GetMapping("/anexos/{referenciaId}/{tipo}")
    public ResponseEntity<?> listarAnexos(@PathVariable Long referenciaId, @PathVariable String tipo) {
        AuthUser me = CurrentUser.require();
        AnexoForum.TipoAnexo tipoEnum = parseTipo(tipo);
        if (tipoEnum == null) return ResponseEntity.badRequest().build();

        access.requireDisciplineAccess(me, disciplineIdOf(tipoEnum, referenciaId));
        return ResponseEntity.ok(anexoForumDao.findByReferenciaIdAndTipo(referenciaId, tipoEnum));
    }

    @DeleteMapping("/anexo/{id}")
    public ResponseEntity<Void> excluirAnexo(@PathVariable Long id) {
        AuthUser me = CurrentUser.require();
        AnexoForum anexo = anexoForumDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo nao encontrado"));
        requireAnexoOwner(me, anexo.getTipo(), anexo.getReferenciaId());

        anexoForumDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/anexo/{id}")
    public ResponseEntity<?> atualizarAnexo(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("url")) return ResponseEntity.badRequest().build();
        AuthUser me = CurrentUser.require();
        if (!UrlSafety.isSafeHttpUrl(payload.get("url"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de anexo invalida");
        }

        AnexoForum anexo = anexoForumDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo nao encontrado"));
        requireAnexoOwner(me, anexo.getTipo(), anexo.getReferenciaId());

        anexo.setUrl(payload.get("url"));
        return ResponseEntity.ok(anexoForumDao.save(anexo));
    }

    private Duvida findDuvida(Integer id) {
        return duvidaDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duvida nao encontrada"));
    }

    private RespostaDuvida findResposta(Long id) {
        return respostaDao.findById(id.intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resposta nao encontrada"));
    }

    private Integer disciplineIdOf(AnexoForum.TipoAnexo tipo, Long referenciaId) {
        if (tipo == AnexoForum.TipoAnexo.DUVIDA) {
            return findDuvida(referenciaId.intValue()).getDisciplina().getId();
        }
        RespostaDuvida r = findResposta(referenciaId);
        return r.getDuvida().getDisciplina().getId();
    }

    private void requireAnexoOwner(AuthUser me, AnexoForum.TipoAnexo tipo, Long referenciaId) {
        if (tipo == null || referenciaId == null) throw AccessService.forbidden();
        if (me.isAdmin()) return;
        Integer ownerId = (tipo == AnexoForum.TipoAnexo.DUVIDA)
                ? findDuvida(referenciaId.intValue()).getAluno().getId()
                : findResposta(referenciaId).getUsuario().getId();
        if (!me.id().equals(ownerId)) throw AccessService.forbidden();
    }

    private AnexoForum.TipoAnexo parseTipo(String tipo) {
        try {
            return AnexoForum.TipoAnexo.valueOf(tipo.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(Object value) {
        try {
            return Long.valueOf(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
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
