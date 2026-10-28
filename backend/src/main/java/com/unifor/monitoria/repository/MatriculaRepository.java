package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
    boolean existsByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);
    List<Matricula> findByDisciplinaId(Integer disciplinaId);
}
