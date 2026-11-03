package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Schedule;

import java.util.List;

public interface ScheduleDao extends GenericDao<Schedule, Integer> {

    List<Schedule> findByDisciplineId(Integer disciplineId);
}