package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "horarios_monitoria")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnoreProperties({"schedules", "discipline"})
    @ManyToOne
    @JoinColumn(name = "monitoria_id")
    private MonitoringSession monitoringSession;

    @Column(name = "date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("startTime")
    @Column(name = "start_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonProperty("endTime")
    @Column(name = "end_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Column(name = "room")
    private String room;

    public Schedule() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public MonitoringSession getMonitoringSession() { return monitoringSession; }
    public void setMonitoringSession(MonitoringSession monitoringSession) { this.monitoringSession = monitoringSession; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}
