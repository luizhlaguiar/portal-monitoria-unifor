package com.unifor.monitoria.controller;

import com.unifor.monitoria.dao.ActivityDao;
import com.unifor.monitoria.dao.DisciplineDao;
import com.unifor.monitoria.model.Activity;
import com.unifor.monitoria.model.Discipline;
import com.unifor.monitoria.security.AccessService;
import com.unifor.monitoria.security.AuthUser;
import com.unifor.monitoria.security.CurrentUser;
import com.unifor.monitoria.security.UrlSafety;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private DisciplineDao disciplineDao;

    @Autowired
    private AccessService access;

    @PostMapping
    public ResponseEntity<Activity> addActivity(@RequestBody Activity activity) {
        AuthUser me = CurrentUser.require();
        if (activity.getDiscipline() == null || activity.getDiscipline().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disciplina obrigatoria");
        }
        access.requireMonitorOf(me, activity.getDiscipline().getId());
        requireSafeUrl(activity.getArquivoUrl());

        Discipline discipline = disciplineDao.findById(activity.getDiscipline().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina nao encontrada"));
        activity.setId(null);
        activity.setDiscipline(discipline);
        return ResponseEntity.ok(activityDao.save(activity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Integer id, @RequestBody Activity activityDetails) {
        AuthUser me = CurrentUser.require();
        Activity activity = activityDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade nao encontrada"));
        access.requireMonitorOf(me, activity.getDiscipline() != null ? activity.getDiscipline().getId() : null);
        requireSafeUrl(activityDetails.getArquivoUrl());

        activity.setTitulo(activityDetails.getTitulo());
        activity.setPrazo(activityDetails.getPrazo());
        activity.setDescricao(activityDetails.getDescricao());
        activity.setArquivoUrl(activityDetails.getArquivoUrl());
        return ResponseEntity.ok(activityDao.save(activity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Integer id) {
        AuthUser me = CurrentUser.require();
        Activity activity = activityDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade nao encontrada"));
        access.requireMonitorOf(me, activity.getDiscipline() != null ? activity.getDiscipline().getId() : null);

        activityDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void requireSafeUrl(String url) {
        if (!UrlSafety.isSafeOrEmpty(url)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL de anexo invalida");
        }
    }
}
