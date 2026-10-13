package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "horarios_monitoria")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "monitoria_id")
    private MonitoringSession monitoringSession;

    private String dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String room;

    public Schedule() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public MonitoringSession getMonitoringSession() { return monitoringSession; }
    public void setMonitoringSession(MonitoringSession monitoringSession) { this.monitoringSession = monitoringSession; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}
