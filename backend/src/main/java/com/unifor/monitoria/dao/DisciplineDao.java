package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Discipline;

import java.util.List;

public interface DisciplineDao extends GenericDao<Discipline, Integer> {

    List<Discipline> findDisciplinesByStudentId(Integer userId);

    List<Discipline> findDisciplinesByMonitorId(Integer userId);
}