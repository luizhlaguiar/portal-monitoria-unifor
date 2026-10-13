package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
    boolean existsByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);
}
