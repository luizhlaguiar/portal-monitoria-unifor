package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @JsonIgnoreProperties("monitoringSession")
    @OneToMany(mappedBy = "monitoringSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    public MonitoringSession() {}

    public MonitoringSession(Integer id, Discipline discipline, User monitor) {
        this.id = id;
        this.discipline = discipline;
        this.monitor = monitor;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public User getMonitor() { return monitor; }
    public void setMonitor(User monitor) { this.monitor = monitor; }

    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
}
