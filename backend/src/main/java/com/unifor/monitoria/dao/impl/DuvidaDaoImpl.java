package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.DuvidaDao;
import com.unifor.monitoria.model.Duvida;

import java.util.List;

@Dao
public class DuvidaDaoImpl extends JpaGenericDao<Duvida, Integer> implements DuvidaDao {

    @Override
    public List<Duvida> findByDisciplinaId(Integer disciplinaId) {
        return queryList("select distinct d from Duvida d join fetch d.aluno join fetch d.disciplina "
                + "where d.disciplina.id = :disciplinaId", "disciplinaId", disciplinaId);
    }
}