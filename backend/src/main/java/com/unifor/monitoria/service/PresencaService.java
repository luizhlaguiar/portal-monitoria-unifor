package com.unifor.monitoria.service;

import com.unifor.monitoria.model.*;
import com.unifor.monitoria.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresencaService {

    @Autowired
    private PresencaDao presencaDao;

    @Autowired
    private MatriculaDao matriculaDao;

    @Autowired
    private UserDao userDao;

    public List<User> getAlunosMatriculados(Integer scheduleId) {
        return null;
    }

    public Presenca registrarPresenca(Integer scheduleId, Integer userId) {
        return null;
    }
}
