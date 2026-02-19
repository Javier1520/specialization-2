package com.epam.gym.workload.controller;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Void> addWorkload(@Valid @RequestBody AddWorkloadRequest request) {
        workloadService.addWorkload(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWorkload(@Valid @RequestBody DeleteWorkloadRequest request) {
        workloadService.deleteWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadDto> getWorkload(
            @PathVariable("username") String username) {
        return ResponseEntity.ok(workloadService.getWorkload(username));
    }

    @GetMapping("/hours")
    public ResponseEntity<TrainingHoursDto> getTrainingHours(
            @RequestParam("username") String username,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        return ResponseEntity.ok(workloadService.getTrainingHours(username, year, month));
    }
}
