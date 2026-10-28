package com.unifor.monitoria.service;

import com.unifor.monitoria.model.MonitoringSession;
import com.unifor.monitoria.repository.MonitoringSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private MonitoringSessionRepository repository;

    public boolean hasConflict(Long monitorId, LocalDate date, LocalTime start, LocalTime end) {
        List<MonitoringSession> sessions = repository.findAll();
        return sessions.stream().anyMatch(s -> {
            // Precisamos acessar os horários vinculados à sessão de monitoria
            if (s.getSchedules() == null) return false;

            return s.getMonitor().getId().equals(monitorId) &&
                   s.getSchedules().stream().anyMatch(sch ->
                       sch.getDate() != null &&
                       sch.getDate().equals(date) &&
                       (start.isBefore(sch.getEndTime()) && end.isAfter(sch.getStartTime()))
                   );
        });
    }
}
