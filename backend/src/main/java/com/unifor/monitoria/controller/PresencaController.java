package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presencas")
public class PresencaController {

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/horario/{scheduleId}/alunos")
    public ResponseEntity<List<User>> getAlunosParaChamada(@PathVariable Integer scheduleId) {
        try {
            // 1. Encontrar o horário e a disciplina vinculada
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

            Integer disciplineId = schedule.getMonitoringSession().getDiscipline().getId();

            // 2. Buscar todas as matrículas dessa disciplina
            List<Matricula> matriculas = matriculaRepository.findByDisciplinaId(disciplineId);

            // 3. Converter matrículas em usuários
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
        try {
            Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

            List<Presenca> presencasSalvas = request.getUserIds().stream().map(userId -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

                Presenca p = new Presenca();
                p.setSchedule(schedule);
                p.setUser(user);
                p.setDataPresenca(LocalDateTime.now());
                return presencaRepository.save(p);
            }).collect(Collectors.toList());

            return ResponseEntity.ok(presencasSalvas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/horario/{scheduleId}")
    public ResponseEntity<List<Presenca>> getPresencasPorHorario(@PathVariable Integer scheduleId) {
        return ResponseEntity.ok(presencaRepository.findByScheduleId(scheduleId));
    }

    // DTO interno para a requisição de registro
    public static class PresencaRequest {
        private Integer scheduleId;
        private List<Integer> userIds;

        public Integer getScheduleId() { return scheduleId; }
        public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }
        public List<Integer> getUserIds() { return userIds; }
        public void setUserIds(List<Integer> userIds) { this.userIds = userIds; }
    }
}
