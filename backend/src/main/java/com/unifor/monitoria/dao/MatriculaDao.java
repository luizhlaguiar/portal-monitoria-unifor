package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Matricula;
import com.unifor.monitoria.model.User;

import java.util.List;

public interface MatriculaDao extends GenericDao<Matricula, Integer> {

    boolean existsByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId);

    List<Matricula> findByDisciplinaId(Integer disciplinaId);

    List<User> findStudentsByDisciplineId(Integer disciplineId);
}