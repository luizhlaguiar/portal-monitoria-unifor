package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.MonitoringSession;

import java.util.List;

public interface MonitoringSessionDao extends GenericDao<MonitoringSession, Integer> {

    List<MonitoringSession> findByDisciplineId(Integer disciplineId);

    boolean existsByMonitorAndDiscipline(Integer monitorId, Integer disciplineId);
}