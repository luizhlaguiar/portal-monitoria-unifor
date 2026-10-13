package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Integer> {

    @Query(value = "SELECT d.* FROM disciplinas d JOIN matriculas m ON d.id = m.disciplina_id WHERE m.aluno_id = :userId", nativeQuery = true)
    List<Discipline> findDisciplinesByStudentId(@Param("userId") Integer userId);

    @Query(value = "SELECT d.* FROM disciplinas d JOIN monitorias mon ON d.id = mon.disciplina_id WHERE mon.monitor_id = :userId", nativeQuery = true)
    List<Discipline> findDisciplinesByMonitorId(@Param("userId") Integer userId);
}
