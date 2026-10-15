package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.model.DuvidaDTO;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ForumService {

    @Autowired
    private DuvidaRepository duvidaRepository;

    @Autowired
    private RespostaDuvidaRepository respostaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private MonitoringSessionRepository monitoriaRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<DuvidaDTO> listarDuvidasPorDisciplina(Integer disciplinaId) {
        System.out.println(">>> [DIAGNOSTICO] Buscando duvidas no MySQL (Cache Desativado)");
        try {
            List<Duvida> duvidas = duvidaRepository.findByDisciplina_Id(disciplinaId);

            return duvidas.stream().map(d -> {
                DuvidaDTO dto = new DuvidaDTO();
                dto.setId(d.getId());
                dto.setTitulo(d.getTitulo());
                dto.setDescricao(d.getDescricao());

                if (d.getAluno() != null) {
                    dto.setAlunoId(d.getAluno().getId());
                    dto.setAlunoNome(d.getAluno().getNome());
                    dto.setAluno(Map.of(
                        "id", d.getAluno().getId(),
                        "nome", d.getAluno().getNome()
                    ));
                }

                if (d.getDisciplina() != null) {
                    dto.setDisciplinaId(d.getDisciplina().getId());
                    dto.setDisciplinaNome(d.getDisciplina().getName());
                    dto.setDisciplina(Map.of(
                        "id", d.getDisciplina().getId(),
                        "name", d.getDisciplina().getName()
                    ));
                }

                dto.setStatus(d.getStatus() != null ? d.getStatus().name() : "PENDENTE");
                dto.setDataCriacao(d.getDataCriacao());

                if (d.getRespostas() != null) {
                    // 1. Converter todas as respostas para DTO primeiro
                    Map<Integer, RespostaDuvidaDTO> dtoMap = d.getRespostas().stream().collect(Collectors.toMap(
                        r -> r.getId(),
                        r -> {
                            RespostaDuvidaDTO rDto = new RespostaDuvidaDTO();
                            rDto.setId(r.getId());
                            rDto.setConteudo(r.getConteudo());
                            rDto.setDataResposta(r.getDataResposta());
                            if (r.getUsuario() != null) {
                                rDto.setUsuarioId(r.getUsuario().getId());
                                rDto.setUsuarioNome(r.getUsuario().getNome());
                                rDto.setUsuario(Map.of("id", r.getUsuario().getId(), "nome", r.getUsuario().getNome()));
                            }
                            rDto.setRespostaPaiId(r.getRespostaPai() != null ? r.getRespostaPai().getId() : null);
                            return rDto;
                        }
                    ));

                    // 2. Montar a hierarquia de aninhamento
                    List<RespostaDuvidaDTO> rootRespostas = new java.util.ArrayList<>();
                    dtoMap.values().forEach(rDto -> {
                        if (rDto.getRespostaPaiId() == null) {
                            rootRespostas.add(rDto);
                        } else {
                            RespostaDuvidaDTO pai = dtoMap.get(rDto.getRespostaPaiId());
                            if (pai != null) {
                                if (pai.getRespostasFilhas() == null) pai.setRespostasFilhas(new java.util.ArrayList<>());
                                pai.getRespostasFilhas().add(rDto);
                            } else {
                                rootRespostas.add(rDto);
                            }
                        }
                    });
                    dto.setRespostas(rootRespostas);
                }

                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println(">>> [ERRO CRITICO] Falha ao listar duvidas: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public Duvida criarDuvida(Integer usuarioId, Integer disciplinaId, String titulo, String descricao) {
        redisTemplate.delete("forum:disciplina:" + disciplinaId);

        User usuario = userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        String userRole = usuario.getRole() != null ? usuario.getRole().name().trim().toUpperCase() : "";
        boolean temPermissao = false;

        if ("ALUNO".equals(userRole)) {
            temPermissao = true;
        } else if ("MONITOR".equals(userRole)) {
            temPermissao = monitoriaRepository.existsByMonitorAndDiscipline(usuarioId, disciplinaId);
        } else if ("ADMIN".equals(userRole)) {
            temPermissao = true;
        }

        if (!temPermissao) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para postar nesta disciplina.");
        }

        Duvida duvida = new Duvida();
        duvida.setAluno(usuario);
        Discipline disciplina = disciplineRepository.findById(disciplinaId).orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        duvida.setDisciplina(disciplina);
        duvida.setTitulo(titulo);
        duvida.setDescricao(descricao);

        return duvidaRepository.save(duvida);
    }

    @Transactional
    public Duvida editarDuvida(Integer duvidaId, Integer usuarioId, String novoTitulo, String novaDescricao) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));
        User usuario = userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAutor = duvida.getAluno().getId().equals(usuarioId);
        boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

        if (!isAutor && !isAdmin) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para editar esta dúvida.");
        }

        redisTemplate.delete("forum:disciplina:" + duvida.getDisciplina().getId());
        duvida.setTitulo(novoTitulo);
        duvida.setDescricao(novaDescricao);
        return duvidaRepository.save(duvida);
    }

    @Transactional
    public void excluirResposta(Integer respostaId, Integer usuarioId) {
        try {
            RespostaDuvida resposta = respostaRepository.findById(respostaId)
                    .orElseThrow(() -> new RuntimeException("Resposta não encontrada."));
            User usuario = userRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            boolean isAutor = resposta.getUsuario().getId().equals(usuarioId);
            boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

            if (!isAutor && !isAdmin) {
                throw new RuntimeException("Acesso Negado: Você não tem permissão para excluir esta resposta.");
            }

            List<RespostaDuvida> filhas = resposta.getRespostasFilhas();
            if (filhas != null) {
                for (RespostaDuvida filha : filhas) {
                    filha.setRespostaPai(null);
                    respostaRepository.save(filha);
                }
            }

            respostaRepository.delete(resposta);
            System.out.println(">>> Resposta " + respostaId + " excluída. Filhas mantidas.");
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao excluir resposta: " + e.getMessage());
        }
    }

    private void deletarRespostasRecursivamente(RespostaDuvida resposta) {
        if (resposta.getRespostasFilhas() != null && !resposta.getRespostasFilhas().isEmpty()) {
            for (RespostaDuvida filha : resposta.getRespostasFilhas()) {
                deletarRespostasRecursivamente(filha);
                respostaRepository.delete(filha);
            }
        }
    }

    @Transactional
    public RespostaDuvida editarResposta(Integer respostaId, Integer usuarioId, String novoConteudo) {
        RespostaDuvida resposta = respostaRepository.findById(respostaId).orElseThrow(() -> new RuntimeException("Resposta não encontrada."));
        User usuario = userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAutor = resposta.getUsuario().getId().equals(usuarioId);
        boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

        if (!isAutor && !isAdmin) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para editar esta resposta.");
        }

        resposta.setConteudo(novoConteudo);
        return respostaRepository.save(resposta);
    }

    @Transactional
    public void excluirDuvida(Integer duvidaId, Integer usuarioId) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));
        User usuario = userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAutor = duvida.getAluno().getId().equals(usuarioId);
        boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

        if (!isAutor && !isAdmin) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para excluir esta dúvida.");
        }

        redisTemplate.delete("forum:disciplina:" + duvida.getDisciplina().getId());
        duvidaRepository.delete(duvida);
    }

    @Transactional
    public RespostaDuvida responderDuvida(Integer duvidaId, Integer usuarioId, String conteudo, Integer respostaPaiId) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));
        RespostaDuvida resposta = new RespostaDuvida();
        resposta.setDuvida(duvida);
        resposta.setUsuario(userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado.")));
        resposta.setConteudo(conteudo);

        if (respostaPaiId != null) {
            RespostaDuvida pai = respostaRepository.findById(respostaPaiId).orElseThrow(() -> new RuntimeException("Resposta pai não encontrada."));
            resposta.setRespostaPai(pai);
        }

        return respostaRepository.save(resposta);
    }

    @Transactional
    public Duvida atualizarStatusDuvida(Integer duvidaId, String statusStr) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));
        Duvida.Status status = Duvida.Status.valueOf(statusStr.toUpperCase());
        duvida.setStatus(status);
        redisTemplate.delete("forum:disciplina:" + duvida.getDisciplina().getId());
        return duvidaRepository.save(duvida);
    }
}
