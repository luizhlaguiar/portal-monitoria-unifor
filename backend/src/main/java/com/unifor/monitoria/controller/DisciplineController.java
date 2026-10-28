package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/disciplines")
public class DisciplineController {

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MonitoringSessionRepository monitoringSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Discipline> getDisciplineById(@PathVariable Integer id) {
        return disciplineRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<List<Discipline>> getStudentDisciplines(@PathVariable Integer userId) {
        List<Discipline> disciplines = disciplineRepository.findDisciplinesByStudentId(userId);
        if (disciplines.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(disciplines);
    }

    @GetMapping("/monitor/{userId}")
    public ResponseEntity<List<Discipline>> getMonitorDisciplines(@PathVariable Integer userId) {
        List<Discipline> disciplines = disciplineRepository.findDisciplinesByMonitorId(userId);
        if (disciplines.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(disciplines);
    }

    @GetMapping("/{id}/session/{monitorId}")
    public ResponseEntity<Integer> getMonitoringSessionId(@PathVariable Integer id, @PathVariable Integer monitorId) {
        List<MonitoringSession> sessions = monitoringSessionRepository.findByDisciplineId(id);
        return sessions.stream()
            .filter(s -> s.getMonitor().getId().equals(monitorId))
            .map(MonitoringSession::getId)
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/monitors")
    public ResponseEntity<List<User>> getMonitorsByDiscipline(@PathVariable Integer id) {
        List<MonitoringSession> sessions = monitoringSessionRepository.findByDisciplineId(id);
        List<User> monitors = sessions.stream()
            .map(MonitoringSession::getMonitor)
            .collect(Collectors.toList());
        return ResponseEntity.ok(monitors);
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<Schedule>> getSchedulesByHDiscipline(@PathVariable Integer id) {
        System.out.println(">>> BUSCANDO HORÁRIOS PARA DISCIPLINA ID: " + id);
        List<Schedule> schedules = scheduleRepository.findByDisciplineId(id);
        System.out.println(">>> QUANTIDADE ENCONTRADA: " + (schedules != null ? schedules.size() : "NULL"));
        if (schedules != null && !schedules.isEmpty()) {
            schedules.forEach(s -> System.out.println(">>> Horário Encontrado: ID=" + s.getId() + ", Data=" + s.getDate()));
        }
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<Activity>> getActivitiesByDiscipline(@PathVariable Integer id) {
        List<Activity> activities = activityRepository.findByDisciplineId(id);
        return ResponseEntity.ok(activities);
    }
}
