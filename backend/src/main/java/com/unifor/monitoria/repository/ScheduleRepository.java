package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByMonitoringSession_Discipline_Id(Integer disciplineId);
}
