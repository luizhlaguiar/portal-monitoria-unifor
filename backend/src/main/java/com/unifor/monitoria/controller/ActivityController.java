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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Integer id) {
        activityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
