package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.MatriculaDao;
import com.unifor.monitoria.model.Matricula;
import com.unifor.monitoria.model.User;

import java.util.List;

@Dao
public class MatriculaDaoImpl extends JpaGenericDao<Matricula, Integer> implements MatriculaDao {

    @Override
    public boolean existsByAlunoIdAndDisciplinaId(Integer alunoId, Integer disciplinaId) {
        return queryExists("select count(m) from Matricula m where m.aluno.id = :aid and m.disciplina.id = :did",
                "aid", alunoId, "did", disciplinaId);
    }

    @Override
    public List<Matricula> findByDisciplinaId(Integer disciplinaId) {
        return queryList("select m from Matricula m where m.disciplina.id = :did", "did", disciplinaId);
    }

    @Override
    public List<User> findStudentsByDisciplineId(Integer disciplineId) {
        return queryListOf("select distinct m.aluno from Matricula m where m.disciplina.id = :did",
                User.class, "did", disciplineId);
    }
}