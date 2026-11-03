package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Duvida;

import java.util.List;

public interface DuvidaDao extends GenericDao<Duvida, Integer> {

    /** Duvidas da disciplina ja com aluno e disciplina carregados (join fetch). */
    List<Duvida> findByDisciplinaId(Integer disciplinaId);
}