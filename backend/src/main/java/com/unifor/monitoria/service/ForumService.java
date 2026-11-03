package com.unifor.monitoria.service;

import com.unifor.monitoria.model.Duvida;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.model.Discipline;
import com.unifor.monitoria.model.RespostaDuvida;
import com.unifor.monitoria.model.dto.DuvidaDTO;
import com.unifor.monitoria.model.dto.RespostaDuvidaDTO;
import com.unifor.monitoria.dao.DuvidaDao;
import com.unifor.monitoria.dao.RespostaDuvidaDao;
import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.dao.DisciplineDao;
import com.unifor.monitoria.dao.MonitoringSessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ForumService {

    @Autowired
    private DuvidaDao duvidaDao;

    @Autowired
    private RespostaDuvidaDao respostaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DisciplineDao disciplineDao;

    @Autowired
    private MonitoringSessionDao monitoriaDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<DuvidaDTO> listarDuvidasPorDisciplina(Integer disciplinaId) {
        List<Duvida> duvidas = duvidaDao.findByDisciplinaId(disciplinaId);
        List<DuvidaDTO> result = new ArrayList<>();

        for (Duvida d : duvidas) {
            DuvidaDTO dto = new DuvidaDTO();
            dto.setId(d.getId());
            dto.setTitulo(d.getTitulo());
            dto.setDescricao(d.getDescricao());
            dto.setStatus(d.getStatus() != null ? d.getStatus().name() : "PENDENTE");
            dto.setDataCriacao(d.getDataCriacao());

            if (d.getAluno() != null) {
                dto.setAlunoId(d.getAluno().getId());
                dto.setAlunoNome(d.getAluno().getNome());
                dto.setAluno(Map.of("id", d.getAluno().getId(), "nome", d.getAluno().getNome()));
            }

            if (d.getDisciplina() != null) {
                dto.setDisciplinaId(d.getDisciplina().getId());
                dto.setDisciplinaNome(d.getDisciplina().getName());
            }

            List<RespostaDuvida> todasRespostas = respostaDao.findByDuvidaId(d.getId());
            if (todasRespostas != null) {
                Map<Integer, RespostaDuvidaDTO> map = new HashMap<>();
                for (RespostaDuvida r : todasRespostas) {
                    RespostaDuvidaDTO rdto = new RespostaDuvidaDTO();
                    rdto.setId(r.getId());
                    rdto.setConteudo(r.getConteudo());
                    rdto.setDataResposta(r.getDataResposta());
                    rdto.setRespostaPaiId(r.getRespostaPai() != null ? r.getRespostaPai().getId() : null);
                    if (r.getUsuario() != null) {
                        rdto.setUsuarioId(r.getUsuario().getId());
                        rdto.setUsuarioNome(r.getUsuario().getNome());
                        rdto.setUsuario(Map.of("id", r.getUsuario().getId(), "nome", r.getUsuario().getNome()));
                    }
                    map.put(rdto.getId(), rdto);
                }

                List<RespostaDuvidaDTO> roots = new ArrayList<>();
                for (RespostaDuvidaDTO r : map.values()) {
                    if (r.getRespostaPaiId() == null) {
                        roots.add(r);
                    } else {
                        RespostaDuvidaDTO pai = map.get(r.getRespostaPaiId());
                        if (pai != null) {
                            if (pai.getRespostasFilhas() == null) pai.setRespostasFilhas(new ArrayList<>());
                            pai.getRespostasFilhas().add(r);
                        } else {
                            roots.add(r);
                        }
                    }
                }
                dto.setRespostas(roots);
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public Duvida criarDuvida(Integer usuarioId, Integer disciplinaId, String titulo, String descricao) {
        User usuario = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Discipline disciplina = disciplineDao.findById(disciplinaId).orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));

        Duvida d = new Duvida();
        d.setAluno(usuario);
        d.setDisciplina(disciplina);
        d.setTitulo(titulo);
        d.setDescricao(descricao);

        Duvida salva = duvidaDao.save(d);
        redisTemplate.delete("forum:disciplina:" + disciplinaId);
        return salva;
    }

    @Transactional
    public void excluirResposta(Integer respostaId, Integer usuarioId) {
        RespostaDuvida r = respostaDao.findById(respostaId).orElseThrow(() -> new RuntimeException("Resposta não encontrada"));
        User u = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!r.getUsuario().getId().equals(usuarioId) && !"ADMIN".equals(u.getRole().name())) {
            throw new RuntimeException("Sem permissão para excluir");
        }

        Integer discId = (r.getDuvida() != null && r.getDuvida().getDisciplina() != null) ? r.getDuvida().getDisciplina().getId() : null;

        deletarFilhasRecursivamente(respostaId);
        respostaDao.deleteByIdNative(respostaId);

        if (discId != null) redisTemplate.delete("forum:disciplina:" + discId);
    }

    private void deletarFilhasRecursivamente(Integer paiId) {
        List<RespostaDuvida> filhas = respostaDao.findByRespostaPaiId(paiId);
        if (filhas != null) {
            for (RespostaDuvida f : filhas) {
                deletarFilhasRecursivamente(f.getId());
                respostaDao.deleteByIdNative(f.getId());
            }
        }
    }

    @Transactional
    public void excluirDuvida(Integer duvidaId, Integer usuarioId) {
        Duvida d = duvidaDao.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada"));
        User u = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!d.getAluno().getId().equals(usuarioId) && !"ADMIN".equals(u.getRole().name())) {
            throw new RuntimeException("Sem permissão para excluir");
        }

        Integer discId = d.getDisciplina().getId();
        respostaDao.deleteByDuvidaIdNative(duvidaId);
        duvidaDao.delete(d);
        redisTemplate.delete("forum:disciplina:" + discId);
    }

    @Transactional
    public Duvida editarDuvida(Integer duvidaId, Integer usuarioId, String titulo, String descricao) {
        Duvida d = duvidaDao.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada"));
        User u = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!d.getAluno().getId().equals(usuarioId) && !"ADMIN".equals(u.getRole().name())) {
            throw new RuntimeException("Sem permissão para editar");
        }

        d.setTitulo(titulo);
        d.setDescricao(descricao);
        Duvida salva = duvidaDao.save(d);
        redisTemplate.delete("forum:disciplina:" + d.getDisciplina().getId());
        return salva;
    }

    @Transactional
    public RespostaDuvida responderDuvida(Integer duvidaId, Integer usuarioId, String conteudo, Integer paiId) {
        Duvida d = duvidaDao.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada"));
        User u = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        RespostaDuvida r = new RespostaDuvida();
        r.setDuvida(d);
        r.setUsuario(u);
        r.setConteudo(conteudo);
        if (paiId != null) {
            RespostaDuvida pai = respostaDao.findById(paiId).orElseThrow(() -> new RuntimeException("Pai não encontrado"));
            r.setRespostaPai(pai);
        }

        RespostaDuvida salva = respostaDao.save(r);
        redisTemplate.delete("forum:disciplina:" + d.getDisciplina().getId());
        return salva;
    }

    @Transactional
    public Duvida atualizarStatusDuvida(Integer duvidaId, String status) {
        Duvida d = duvidaDao.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada"));
        d.setStatus(Duvida.Status.valueOf(status.toUpperCase()));
        Duvida salva = duvidaDao.save(d);
        redisTemplate.delete("forum:disciplina:" + d.getDisciplina().getId());
        return salva;
    }

    @Transactional
    public RespostaDuvida editarResposta(Integer respostaId, Integer usuarioId, String conteudo) {
        RespostaDuvida r = respostaDao.findById(respostaId).orElseThrow(() -> new RuntimeException("Resposta não encontrada"));
        User u = userDao.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!r.getUsuario().getId().equals(usuarioId) && !"ADMIN".equals(u.getRole().name())) {
            throw new RuntimeException("Sem permissão para editar");
        }

        r.setConteudo(conteudo);
        RespostaDuvida salva = respostaDao.save(r);
        if (r.getDuvida() != null) redisTemplate.delete("forum:disciplina:" + r.getDuvida().getDisciplina().getId());
        return salva;
    }
}
