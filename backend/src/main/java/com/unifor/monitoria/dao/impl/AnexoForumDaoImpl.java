package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.AnexoForumDao;
import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.model.AnexoForum;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Dao
public class AnexoForumDaoImpl extends JpaGenericDao<AnexoForum, Long> implements AnexoForumDao {

    @Override
    public List<AnexoForum> findByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo) {
        return queryList("select a from AnexoForum a where a.referenciaId = :ref and a.tipo = :tipo",
                "ref", referenciaId, "tipo", tipo);
    }

    @Override
    @Transactional
    public void deleteByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo) {
        findByReferenciaIdAndTipo(referenciaId, tipo).forEach(em::remove);
    }
}