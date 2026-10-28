package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.AnexoForum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnexoForumRepository extends JpaRepository<AnexoForum, Long> {
    List<AnexoForum> findByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo);
    void deleteByReferenciaIdAndTipo(Long referenciaId, AnexoForum.TipoAnexo tipo);
}
