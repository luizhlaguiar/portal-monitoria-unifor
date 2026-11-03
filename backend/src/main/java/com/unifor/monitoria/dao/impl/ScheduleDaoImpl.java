package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.ScheduleDao;
import com.unifor.monitoria.model.Schedule;

import java.util.List;

@Dao
public class ScheduleDaoImpl extends JpaGenericDao<Schedule, Integer> implements ScheduleDao {

    @Override
    public List<Schedule> findByDisciplineId(Integer disciplineId) {
        return queryList("select s from Schedule s where s.monitoringSession.discipline.id = :did", "did", disciplineId);
    }
}