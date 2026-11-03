package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.AnexoForum;

import java.util.List;

public interface AnexoForumDao extends GenericDao<AnexoForum, Long> {

    List<AnexoForum> findByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo);

    void deleteByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo);
}