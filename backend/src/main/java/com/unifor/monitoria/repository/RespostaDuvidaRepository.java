package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.RespostaDuvida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RespostaDuvidaRepository extends JpaRepository<RespostaDuvida, Integer> {
    List<RespostaDuvida> findByDuvidaId(Integer duvidaId);
}
