package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.MonitoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MonitoringSessionRepository extends JpaRepository<MonitoringSession, Integer> {
    List<MonitoringSession> findByDisciplineId(Integer disciplineId);

    @Query("SELECT count(m) > 0 FROM MonitoringSession m WHERE m.monitor.id = :monitorId AND m.discipline.id = :disciplineId")
    boolean existsByMonitorAndDiscipline(@Param("monitorId") Integer monitorId, @Param("disciplineId") Integer disciplineId);
}
