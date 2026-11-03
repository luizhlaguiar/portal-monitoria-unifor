package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.RespostaDuvidaDao;
import com.unifor.monitoria.model.RespostaDuvida;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Dao
public class RespostaDuvidaDaoImpl extends JpaGenericDao<RespostaDuvida, Integer> implements RespostaDuvidaDao {

    @Override
    public List<RespostaDuvida> findByDuvidaId(Integer duvidaId) {
        return queryList("select r from RespostaDuvida r where r.duvida.id = :did", "did", duvidaId);
    }

    @Override
    public List<RespostaDuvida> findByRespostaPaiId(Integer respostaPaiId) {
        return queryList("select r from RespostaDuvida r where r.respostaPai.id = :pid", "pid", respostaPaiId);
    }

    @Override
    @Transactional
    public void deleteByIdNative(Integer id) {
        nativeUpdate("DELETE FROM respostas_duvidas WHERE id = :id", "id", id);
    }

    @Override
    @Transactional
    public void deleteByDuvidaIdNative(Integer duvidaId) {
        nativeUpdate("DELETE FROM respostas_duvidas WHERE duvida_id = :duvidaId", "duvidaId", duvidaId);
    }
}