package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anexos_forum")
public class AnexoForum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Enumerated(EnumType.STRING)
    private TipoAnexo tipo;

    private Long referenciaId;
    private LocalDateTime dataUpload;

    public enum TipoAnexo {
        DUVIDA, RESPOSTA
    }

    public AnexoForum() {
        this.dataUpload = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public TipoAnexo getTipo() { return tipo; }
    public void setTipo(TipoAnexo tipo) { this.tipo = tipo; }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
