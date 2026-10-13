package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "monitorias")
public class MonitoringSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Discipline discipline;

    @ManyToOne
    @JoinColumn(name = "monitor_id")
    private User monitor;

    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public MonitoringSession() {}

    public MonitoringSession(Integer id, Discipline discipline, User monitor, String dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
        this.id = id;
        this.discipline = discipline;
        this.monitor = monitor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public User getMonitor() { return monitor; }
    public void setMonitor(User monitor) { this.monitor = monitor; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}
