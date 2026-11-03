package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.RespostaDuvida;

import java.util.List;

public interface RespostaDuvidaDao extends GenericDao<RespostaDuvida, Integer> {

    List<RespostaDuvida> findByDuvidaId(Integer duvidaId);

    List<RespostaDuvida> findByRespostaPaiId(Integer respostaPaiId);

    void deleteByIdNative(Integer id);

    void deleteByDuvidaIdNative(Integer duvidaId);
}