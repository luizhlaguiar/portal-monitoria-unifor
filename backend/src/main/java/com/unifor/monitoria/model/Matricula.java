package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "matriculas")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private User aluno;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Discipline disciplina;

    private LocalDate dataMatricula = LocalDate.now();

    public Matricula() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getAluno() { return aluno; }
    public void setAluno(User aluno) { this.aluno = aluno; }

    public Discipline getDisciplina() { return disciplina; }
    public void setDisciplina(Discipline disciplina) { this.disciplina = disciplina; }

    public LocalDate getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDate dataMatricula) { this.dataMatricula = dataMatricula; }
}
