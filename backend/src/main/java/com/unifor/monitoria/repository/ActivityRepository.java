package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {
    List<Activity> findByDisciplineId(Integer disciplineId);
}
