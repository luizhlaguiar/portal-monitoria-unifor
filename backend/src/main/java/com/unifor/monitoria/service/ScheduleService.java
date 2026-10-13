package com.unifor.monitoria.service;

import com.unifor.monitoria.model.MonitoringSession;
import com.unifor.monitoria.repository.MonitoringSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private MonitoringSessionRepository repository;

    public boolean hasConflict(Long monitorId, String day, LocalTime start, LocalTime end) {
        List<MonitoringSession> sessions = repository.findAll();
        return sessions.stream().anyMatch(s ->
            s.getMonitor().getId().equals(monitorId) &&
            s.getDayOfWeek().equals(day) &&
            (start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime()))
        );
    }
}
