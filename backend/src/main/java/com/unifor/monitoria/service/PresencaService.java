package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresencaService {

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private UserRepository userRepository;

    public List<User> getAlunosMatriculados(Integer scheduleId) {
        // 1. Busca o horário para saber a qual disciplina pertence
        // Como o Schedule tem a MonitoringSession, que tem a Discipline
        // Precisamos de uma forma de chegar na disciplina.
        // Vou assumir que temos acesso via Schedule -> MonitoringSession -> Discipline
        // Para simplificar, vou precisar de um repositório de Schedule para pegar o objeto
        return null; // Implementarei a lógica completa no Controller ou ajustando o Repository
    }

    public Presenca registrarPresenca(Integer scheduleId, Integer userId) {
        // Lógica simplificada: o controller passará as entidades
        return null;
    }
}
