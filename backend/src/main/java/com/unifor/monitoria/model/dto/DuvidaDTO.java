package com.unifor.monitoria.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DuvidaDTO {
    private Integer id;
    private String titulo;
    private String descricao;
    private String alunoNome;
    private Integer alunoId;
    private String disciplinaNome;
    private Integer disciplinaId;
    private String status;
    private LocalDateTime dataCriacao;
}
