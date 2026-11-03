package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.DisciplineDao;
import com.unifor.monitoria.model.Discipline;

import java.util.List;

@Dao
public class DisciplineDaoImpl extends JpaGenericDao<Discipline, Integer> implements DisciplineDao {

    @Override
    public List<Discipline> findDisciplinesByStudentId(Integer userId) {
        return nativeList("SELECT d.* FROM disciplinas d JOIN matriculas m ON d.id = m.disciplina_id WHERE m.aluno_id = :userId",
                "userId", userId);
    }

    @Override
    public List<Discipline> findDisciplinesByMonitorId(Integer userId) {
        return nativeList("SELECT d.* FROM disciplinas d JOIN monitorias mon ON d.id = mon.disciplina_id WHERE mon.monitor_id = :userId",
                "userId", userId);
    }
}