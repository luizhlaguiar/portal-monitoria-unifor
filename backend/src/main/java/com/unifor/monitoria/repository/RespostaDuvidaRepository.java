package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.RespostaDuvida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RespostaDuvidaRepository extends JpaRepository<RespostaDuvida, Integer> {
    List<RespostaDuvida> findByDuvidaId(Integer duvidaId);
    List<RespostaDuvida> findByRespostaPaiId(Integer respostaPaiId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM respostas_duvidas WHERE id = :id", nativeQuery = true)
    void deleteByIdNative(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM respostas_duvidas WHERE duvida_id = :duvidaId", nativeQuery = true)
    void deleteByDuvidaIdNative(@Param("duvidaId") Integer duvidaId);
}
