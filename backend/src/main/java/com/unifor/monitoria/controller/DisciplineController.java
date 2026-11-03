package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.ActivityDao;
import com.unifor.monitoria.dao.DisciplineDao;
import com.unifor.monitoria.dao.MonitoringSessionDao;
import com.unifor.monitoria.dao.ScheduleDao;
import com.unifor.monitoria.model.Activity;
import com.unifor.monitoria.model.Discipline;
import com.unifor.monitoria.model.MonitoringSession;
import com.unifor.monitoria.model.Schedule;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/disciplines")
public class DisciplineController {

    @Autowired
    private DisciplineDao disciplineDao;

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private MonitoringSessionDao monitoringSessionDao;

    @Autowired
    private AccessService access;

    @GetMapping("/{id}")
    public ResponseEntity<Discipline> getDisciplineById(@PathVariable Integer id) {
        access.requireDisciplineAccess(CurrentUser.require(), id);
        return disciplineDao.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<List<Discipline>> getStudentDisciplines(@PathVariable Integer userId) {
        access.requireSelfOrAdmin(CurrentUser.require(), userId);
        List<Discipline> disciplines = disciplineDao.findDisciplinesByStudentId(userId);
        if (disciplines.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(disciplines);
    }

    @GetMapping("/monitor/{userId}")
    public ResponseEntity<List<Discipline>> getMonitorDisciplines(@PathVariable Integer userId) {
        access.requireSelfOrAdmin(CurrentUser.require(), userId);
        List<Discipline> disciplines = disciplineDao.findDisciplinesByMonitorId(userId);
        if (disciplines.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(disciplines);
    }

    @GetMapping("/{id}/session/{monitorId}")
    public ResponseEntity<Integer> getMonitoringSessionId(@PathVariable Integer id, @PathVariable Integer monitorId) {
        access.requireSelfOrAdmin(CurrentUser.require(), monitorId);
        List<MonitoringSession> sessions = monitoringSessionDao.findByDisciplineId(id);
        return sessions.stream()
            .filter(s -> s.getMonitor().getId().equals(monitorId))
            .map(MonitoringSession::getId)
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/monitors")
    public ResponseEntity<List<User>> getMonitorsByDiscipline(@PathVariable Integer id) {
        access.requireDisciplineAccess(CurrentUser.require(), id);
        List<MonitoringSession> sessions = monitoringSessionDao.findByDisciplineId(id);
        List<User> monitors = sessions.stream()
            .map(MonitoringSession::getMonitor)
            .collect(Collectors.toList());
        return ResponseEntity.ok(monitors);
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<Schedule>> getSchedulesByHDiscipline(@PathVariable Integer id) {
        access.requireDisciplineAccess(CurrentUser.require(), id);
        return ResponseEntity.ok(scheduleDao.findByDisciplineId(id));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<Activity>> getActivitiesByDiscipline(@PathVariable Integer id) {
        access.requireDisciplineAccess(CurrentUser.require(), id);
        List<Activity> activities = activityDao.findByDisciplineId(id);
        return ResponseEntity.ok(activities);
    }
}
