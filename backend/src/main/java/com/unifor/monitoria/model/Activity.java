package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

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
    private LocalDate prazo;

    @Column(columnDefinition = "TEXT")
    private String descricao;
    private String arquivoUrl;

    public Activity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getArquivoUrl() { return arquivoUrl; }
    public void setArquivoUrl(String arquivoUrl) { this.arquivoUrl = arquivoUrl; }
}
