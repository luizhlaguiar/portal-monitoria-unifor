package com.unifor.monitoria.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DuvidaDTO {
    private Integer id;
    private String titulo;
    private String descricao;
    private Map<String, Object> aluno;
    private Integer alunoId;
    private String alunoNome;
    private Map<String, Object> disciplina;
    private Integer disciplinaId;
    private String disciplinaNome;
    private String status;
    private LocalDateTime dataCriacao;
    private List<RespostaDuvidaDTO> respostas;

    public DuvidaDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Map<String, Object> getAluno() { return aluno; }
    public void setAluno(Map<String, Object> aluno) { this.aluno = aluno; }

    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public Map<String, Object> getDisciplina() { return disciplina; }
    public void setDisciplina(Map<String, Object> disciplina) { this.disciplina = disciplina; }

    public Integer getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Integer disciplinaId) { this.disciplinaId = disciplinaId; }

    public String getDisciplinaNome() { return disciplinaNome; }
    public void setDisciplinaNome(String disciplinaNome) { this.disciplinaNome = disciplinaNome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<RespostaDuvidaDTO> getRespostas() { return respostas; }
    public void setRespostas(List<RespostaDuvidaDTO> respostas) { this.respostas = respostas; }
}
