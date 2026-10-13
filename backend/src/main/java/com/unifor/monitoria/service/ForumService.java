package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Duvida> listarDuvidasPorDisciplina(Integer disciplinaId) {
        return duvidaRepository.findByDisciplina_Id(disciplinaId);
    }

    @Transactional
    public Duvida criarDuvida(Integer usuarioId, Integer disciplinaId, String titulo, String descricao) {
        System.out.println(">>> DEBUG: Iniciando criacao de duvida");
        System.out.println(">>> UsuarioID: " + usuarioId + " | DisciplinaID: " + disciplinaId);

        User usuario = userRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            System.out.println(">>> ERRO: Usuario nao encontrado!");
            throw new RuntimeException("Usuário não encontrado.");
        }

        String userRole = usuario.getRole() != null ? usuario.getRole().name().trim().toUpperCase() : "";
        System.out.println(">>> Usuario encontrado: " + usuario.getNome() + " Role: " + userRole);

        boolean temPermissao = false;

        if ("ALUNO".equals(userRole)) {
            temPermissao = true; // Liberado para todos os alunos durante os testes
            System.out.println(">>> Permissao Aluno: true (Modo Teste)");
        } else if ("MONITOR".equals(userRole)) {
            try {
                temPermissao = monitoriaRepository.existsByMonitorAndDiscipline(usuarioId, disciplinaId);
                System.out.println(">>> Permissao Monitor: " + temPermissao);
            } catch (Exception e) {
                System.err.println(">>> ERRO ao verificar monitor: " + e.getMessage());
                temPermissao = false;
            }
        } else if ("ADMIN".equals(userRole)) {
            temPermissao = true;
            System.out.println(">>> Permissao Admin: true");
        }

        if (!temPermissao) {
            System.out.println(">>> ERRO: Sem permissao para postar!");
            throw new RuntimeException("Acesso Negado: Você não tem permissão para postar nesta disciplina.");
        }

        Duvida duvida = new Duvida();
        duvida.setAluno(usuario);

        Discipline disciplina = disciplineRepository.findById(disciplinaId).orElse(null);
        if (disciplina == null) {
            System.out.println(">>> ERRO: Disciplina nao encontrada!");
            throw new RuntimeException("Disciplina não encontrada.");
        }
        System.out.println(">>> Disciplina encontrada: " + disciplina.getName());
        duvida.setDisciplina(disciplina);

        duvida.setTitulo(titulo);
        duvida.setDescricao(descricao);

        System.out.println(">>> Tentando salvar no banco...");
        try {
            return duvidaRepository.save(duvida);
        } catch (Exception e) {
            System.err.println(">>> ERRO CRÍTICO AO SALVAR DUVIDA: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar dúvida no banco de dados: " + e.getMessage());
        }
    }

    @Transactional
    public Duvida editarDuvida(Integer duvidaId, Integer usuarioId, String novoTitulo, String novaDescricao) {
        Duvida duvida = duvidaRepository.findById(duvidaId)
                .orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAutor = duvida.getAluno().getId().equals(usuarioId);
        boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

        if (!isAutor && !isAdmin) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para editar esta dúvida.");
        }

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

            deletarRespostasRecursivamente(resposta);
            respostaRepository.delete(resposta);
            System.out.println(">>> Resposta " + respostaId + " e todas as suas filhas excluídas com sucesso.");
        } catch (Exception e) {
            System.err.println(">>> ERRO CRÍTICO AO EXCLUIR RESPOSTA: " + e.getMessage());
            e.printStackTrace();
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
        RespostaDuvida resposta = respostaRepository.findById(respostaId)
                .orElseThrow(() -> new RuntimeException("Resposta não encontrada."));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

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
        Duvida duvida = duvidaRepository.findById(duvidaId)
                .orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        boolean isAutor = duvida.getAluno().getId().equals(usuarioId);
        boolean isAdmin = "ADMIN".equals(usuario.getRole() != null ? usuario.getRole().name().toUpperCase() : "");

        if (!isAutor && !isAdmin) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para excluir esta dúvida.");
        }

        duvidaRepository.delete(duvida);
        System.out.println(">>> Duvida " + duvidaId + " excluida com sucesso pelo usuario " + usuarioId);
    }

    @Transactional
    public RespostaDuvida responderDuvida(Integer duvidaId, Integer usuarioId, String conteudo, Integer respostaPaiId) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));

        RespostaDuvida resposta = new RespostaDuvida();
        resposta.setDuvida(duvida);
        resposta.setUsuario(userRepository.findById(usuarioId).orElseThrow(() -> new RuntimeException("Usuário não encontrado.")));
        resposta.setConteudo(conteudo);

        if (respostaPaiId != null) {
            RespostaDuvida pai = respostaRepository.findById(respostaPaiId)
                    .orElseThrow(() -> new RuntimeException("Resposta pai não encontrada."));
            resposta.setRespostaPai(pai);
        }

        return respostaRepository.save(resposta);
    }

    @Transactional
    public Duvida atualizarStatusDuvida(Integer duvidaId, String statusStr) {
        Duvida duvida = duvidaRepository.findById(duvidaId).orElseThrow(() -> new RuntimeException("Dúvida não encontrada."));
        Duvida.Status status = Duvida.Status.valueOf(statusStr.toUpperCase());
        duvida.setStatus(status);
        return duvidaRepository.save(duvida);
    }
}
