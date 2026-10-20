package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Duvida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DuvidaRepository extends JpaRepository<Duvida, Integer> {

    @Query("SELECT DISTINCT d FROM Duvida d " +
           "JOIN FETCH d.aluno " +
           "JOIN FETCH d.disciplina " +
           "WHERE d.disciplina.id = :disciplinaId")
    List<Duvida> findByDisciplina_Id(@Param("disciplinaId") Integer disciplinaId);
}
