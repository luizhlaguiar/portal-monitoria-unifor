package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.Activity;
import com.unifor.monitoria.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @PostMapping
    public ResponseEntity<Activity> addActivity(@RequestBody Activity activity) {
        return ResponseEntity.ok(activityRepository.save(activity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Integer id, @RequestBody Activity activityDetails) {
        return activityRepository.findById(id)
                .map(activity -> {
                    activity.setTitulo(activityDetails.getTitulo());
                    activity.setPrazo(activityDetails.getPrazo());
                    activity.setDescricao(activityDetails.getDescricao());
                    activity.setArquivoUrl(activityDetails.getArquivoUrl());
                    return ResponseEntity.ok(activityRepository.save(activity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Integer id) {
        activityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
