package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "atividades")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Discipline discipline;

    private String titulo;
    private LocalDateTime prazo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    public Activity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDateTime getPrazo() { return prazo; }
    public void setPrazo(LocalDateTime prazo) { this.prazo = prazo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
