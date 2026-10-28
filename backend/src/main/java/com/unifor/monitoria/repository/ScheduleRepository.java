package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    @Query("SELECT s FROM Schedule s WHERE s.monitoringSession.discipline.id = :disciplineId")
    List<Schedule> findByDisciplineId(@Param("disciplineId") Integer disciplineId);
}
