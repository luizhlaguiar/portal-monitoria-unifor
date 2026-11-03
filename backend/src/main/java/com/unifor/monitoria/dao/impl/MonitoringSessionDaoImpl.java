package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.MonitoringSessionDao;
import com.unifor.monitoria.model.MonitoringSession;

import java.util.List;

@Dao
public class MonitoringSessionDaoImpl extends JpaGenericDao<MonitoringSession, Integer> implements MonitoringSessionDao {

    @Override
    public List<MonitoringSession> findByDisciplineId(Integer disciplineId) {
        return queryList("select m from MonitoringSession m where m.discipline.id = :did", "did", disciplineId);
    }

    @Override
    public boolean existsByMonitorAndDiscipline(Integer monitorId, Integer disciplineId) {
        return queryExists("select count(m) from MonitoringSession m where m.monitor.id = :mid and m.discipline.id = :did",
                "mid", monitorId, "did", disciplineId);
    }
}