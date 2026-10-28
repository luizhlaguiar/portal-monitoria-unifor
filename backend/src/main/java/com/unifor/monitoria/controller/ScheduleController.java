package com.unifor.monitoria.controller;

import com.unifor.monitoria.model.Schedule;
import com.unifor.monitoria.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @PostMapping
    public ResponseEntity<Schedule> addSchedule(@RequestBody Schedule schedule) {
        return ResponseEntity.ok(scheduleRepository.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Integer id, @RequestBody Schedule schedule) {
        return scheduleRepository.findById(id)
                .map(s -> {
                    schedule.setId(id);
                    return ResponseEntity.ok(scheduleRepository.save(schedule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
