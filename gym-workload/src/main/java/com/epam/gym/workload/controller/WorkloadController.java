package com.epam.gym.workload.controller;

import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<Void> updateWorkload(@Valid @RequestBody WorkloadRequest request) {
        workloadService.updateWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<TrainerEntity> getWorkload(@RequestParam String username) {
        return ResponseEntity.ok(workloadService.getWorkload(username));
    }
}
