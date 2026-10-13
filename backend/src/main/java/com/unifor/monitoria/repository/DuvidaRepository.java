package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Duvida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DuvidaRepository extends JpaRepository<Duvida, Integer> {
    List<Duvida> findByDisciplina_Id(Integer disciplinaId);
}
