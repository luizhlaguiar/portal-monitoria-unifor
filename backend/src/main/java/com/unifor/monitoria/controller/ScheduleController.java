package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.MonitoringSessionDao;
import com.unifor.monitoria.dao.ScheduleDao;
import com.unifor.monitoria.model.MonitoringSession;
import com.unifor.monitoria.model.Schedule;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private MonitoringSessionDao monitoringSessionDao;

    @Autowired
    private AccessService access;

    @PostMapping
    public ResponseEntity<Schedule> addSchedule(@RequestBody Schedule schedule) {
        AuthUser me = CurrentUser.require();
        if (schedule.getMonitoringSession() == null || schedule.getMonitoringSession().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monitoria obrigatoria");
        }
        MonitoringSession session = monitoringSessionDao.findById(schedule.getMonitoringSession().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitoria nao encontrada"));
        access.requireOwnsSession(me, session);

        schedule.setId(null);
        schedule.setMonitoringSession(session);
        return ResponseEntity.ok(scheduleDao.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Integer id, @RequestBody Schedule details) {
        AuthUser me = CurrentUser.require();
        Schedule existing = scheduleDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario nao encontrado"));
        access.requireOwnsSession(me, existing.getMonitoringSession());

        existing.setDate(details.getDate());
        existing.setStartTime(details.getStartTime());
        existing.setEndTime(details.getEndTime());
        existing.setRoom(details.getRoom());
        return ResponseEntity.ok(scheduleDao.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
        AuthUser me = CurrentUser.require();
        Schedule existing = scheduleDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario nao encontrado"));
        access.requireOwnsSession(me, existing.getMonitoringSession());

        scheduleDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
