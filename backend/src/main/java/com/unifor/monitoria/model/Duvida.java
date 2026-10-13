package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "duvidas")
public class Duvida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private User aluno;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Discipline disciplina;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDENTE;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @OneToMany(mappedBy = "duvida", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<RespostaDuvida> respostas;

    public enum Status {
        PENDENTE, EM_ANALISE, RESOLVIDA
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getAluno() { return aluno; }
    public void setAluno(User aluno) { this.aluno = aluno; }

    public Discipline getDisciplina() { return disciplina; }
    public void setDisciplina(Discipline disciplina) { this.disciplina = disciplina; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<RespostaDuvida> getRespostas() { return respostas; }
    public void setRespostas(List<RespostaDuvida> respostas) { this.respostas = respostas; }
}
