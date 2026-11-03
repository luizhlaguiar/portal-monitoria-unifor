package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.MatriculaDao;
import com.unifor.monitoria.dao.PresencaDao;
import com.unifor.monitoria.dao.ScheduleDao;
import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.Matricula;
import com.unifor.monitoria.model.Presenca;
import com.unifor.monitoria.model.Schedule;
import com.unifor.monitoria.model.User;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presencas")
public class PresencaController {

    @Autowired
    private PresencaDao presencaDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private MatriculaDao matriculaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AccessService access;

    @GetMapping("/horario/{scheduleId}/alunos")
    public ResponseEntity<List<User>> getAlunosParaChamada(@PathVariable Integer scheduleId) {
        Schedule schedule = loadOwnedSchedule(scheduleId);
        try {
            Integer disciplineId = schedule.getMonitoringSession().getDiscipline().getId();

            List<Matricula> matriculas = matriculaDao.findByDisciplinaId(disciplineId);

            List<User> alunos = matriculas.stream()
                    .map(m -> m.getAluno())
                    .filter(u -> u != null)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(alunos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<List<Presenca>> registrarPresencas(@RequestBody PresencaRequest request) {
        if (request == null || request.getScheduleId() == null || request.getUserIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da chamada incompletos");
        }
        Schedule schedule = loadOwnedSchedule(request.getScheduleId());
        Integer disciplineId = schedule.getMonitoringSession().getDiscipline().getId();

        for (Integer userId : request.getUserIds()) {
            if (userId == null || !matriculaDao.existsByAlunoIdAndDisciplinaId(userId, disciplineId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aluno nao matriculado na disciplina");
            }
        }

        try {
            List<Presenca> presencasSalvas = request.getUserIds().stream().map(userId -> {
                User user = userDao.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

                Presenca p = new Presenca();
                p.setSchedule(schedule);
                p.setUser(user);
                p.setDataPresenca(LocalDateTime.now());
                return presencaDao.save(p);
            }).collect(Collectors.toList());

            return ResponseEntity.ok(presencasSalvas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/horario/{scheduleId}")
    public ResponseEntity<List<Presenca>> getPresencasPorHorario(@PathVariable Integer scheduleId) {
        loadOwnedSchedule(scheduleId);
        return ResponseEntity.ok(presencaDao.findByScheduleId(scheduleId));
    }

    private Schedule loadOwnedSchedule(Integer scheduleId) {
        AuthUser me = CurrentUser.require();
        Schedule schedule = scheduleDao.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario nao encontrado"));
        access.requireOwnsSession(me, schedule.getMonitoringSession());
        return schedule;
    }

    public static class PresencaRequest {
        private Integer scheduleId;
        private List<Integer> userIds;

        public Integer getScheduleId() { return scheduleId; }
        public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }
        public List<Integer> getUserIds() { return userIds; }
        public void setUserIds(List<Integer> userIds) { this.userIds = userIds; }
    }
}
