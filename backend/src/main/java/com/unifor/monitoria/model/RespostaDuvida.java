package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "respostas_duvidas")
public class RespostaDuvida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "duvida_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Duvida duvida;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "resposta_pai_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RespostaDuvida respostaPai;

    @OneToMany(mappedBy = "respostaPai", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<RespostaDuvida> respostasFilhas;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;
    private String anexoUrl;

    private LocalDateTime dataResposta = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Duvida getDuvida() { return duvida; }
    public void setDuvida(Duvida duvida) { this.duvida = duvida; }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

    public RespostaDuvida getRespostaPai() { return respostaPai; }
    public void setRespostaPai(RespostaDuvida respostaPai) { this.respostaPai = respostaPai; }

    public List<RespostaDuvida> getRespostasFilhas() { return respostasFilhas; }
    public void setRespostasFilhas(List<RespostaDuvida> respostasFilhas) { this.respostasFilhas = respostasFilhas; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getAnexoUrl() { return anexoUrl; }
    public void setAnexoUrl(String anexoUrl) { this.anexoUrl = anexoUrl; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
}
