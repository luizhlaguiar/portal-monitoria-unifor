package com.unifor.monitoria.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RespostaDuvidaDTO {
    private Integer id;
    private String conteudo;
    private LocalDateTime dataResposta;
    private Map<String, Object> usuario;
    private Integer usuarioId;
    private String usuarioNome;
    private Integer respostaPaiId;
    private List<RespostaDuvidaDTO> respostasFilhas;

    public RespostaDuvidaDTO() {}

    public RespostaDuvidaDTO(Integer id, String conteudo, LocalDateTime dataResposta, Map<String, Object> usuario, Integer usuarioId, String usuarioNome, Integer respostaPaiId) {
        this.id = id;
        this.conteudo = conteudo;
        this.dataResposta = dataResposta;
        this.usuario = usuario;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.respostaPaiId = respostaPaiId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public Map<String, Object> getUsuario() { return usuario; }
    public void setUsuario(Map<String, Object> usuario) { this.usuario = usuario; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public Integer getRespostaPaiId() { return respostaPaiId; }
    public void setRespostaPaiId(Integer respostaPaiId) { this.respostaPaiId = respostaPaiId; }

    public List<RespostaDuvidaDTO> getRespostasFilhas() { return respostasFilhas; }
    public void setRespostasFilhas(List<RespostaDuvidaDTO> respostasFilhas) { this.respostasFilhas = respostasFilhas; }
}
