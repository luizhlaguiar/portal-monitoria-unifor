package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "presencas")
public class Presenca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "horario_id", nullable = false)
    @JsonIgnoreProperties({"date", "startTime", "endTime", "room"})
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"senha"})
    private User user;

    @Column(name = "data_presenca")
    private LocalDateTime dataPresenca;

    public Presenca() {}

    public Presenca(Schedule schedule, User user, LocalDateTime dataPresenca) {
        this.schedule = schedule;
        this.user = user;
        this.dataPresenca = dataPresenca;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getDataPresenca() { return dataPresenca; }
    public void setDataPresenca(LocalDateTime dataPresenca) { this.dataPresenca = dataPresenca; }
}
