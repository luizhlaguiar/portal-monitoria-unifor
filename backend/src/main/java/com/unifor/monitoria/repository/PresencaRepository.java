package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PresencaRepository extends JpaRepository<Presenca, Integer> {
    List<Presenca> findByScheduleId(Integer scheduleId);
    boolean existsByScheduleIdAndUserId(Integer scheduleId, Integer userId);
}
