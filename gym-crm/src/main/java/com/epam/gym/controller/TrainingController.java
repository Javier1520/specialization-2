package com.epam.gym.controller;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.DeleteTrainingRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.openapi.annotation.operation.CreateOperation;
import com.epam.gym.openapi.annotation.operation.DeleteOperation;
import com.epam.gym.openapi.annotation.operation.GetAllOperation;
import com.epam.gym.service.TrainingService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Trainings", description = "Operations in Trainings")
@RestController
@RequestMapping("/api/v1/trainings")
public class TrainingController {
    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final LogUtils logUtils;
    private final WorkloadClient workloadClient;

    public TrainingController(TrainingService trainingService, LogUtils logUtils, WorkloadClient workloadClient) {
        this.trainingService = trainingService;
        this.logUtils = logUtils;
        this.workloadClient = workloadClient;
    }

    @CreateOperation(summary = "Add Training", description = "Create a new Training in Gym CRM")
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        logUtils.info(
                log,
                "Add training request: traineeUsername={}, trainerUsername={}, trainingName={}",
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingName());

        trainingService.addTraining(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteOperation(summary = "Delete Training", description = "Delete a Training from Gym CRM")
    @DeleteMapping
    public ResponseEntity<Void> deleteTraining(@Valid @RequestBody DeleteTrainingRequest request) {
        logUtils.info(
                log,
                "Delete training request: traineeUsername={}, trainerUsername={}, trainingName={}",
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingName());

        trainingService.deleteTraining(request);
        return ResponseEntity.ok().build();
    }

    @GetAllOperation(
            summary = "Get Trainer Workload",
            description = "Retrieve trainer's monthly summary of provided trainings")
    @GetMapping("/workload/{username}")
    public ResponseEntity<TrainerWorkloadDto> getTrainerWorkload(@PathVariable("username") String username) {
        logUtils.info(log, "Get trainer workload request: username={}", username);
        return ResponseEntity.ok(workloadClient.getWorkload(username));
    }

    @GetAllOperation(
            summary = "Get Training Hours",
            description = "Retrieve training hours for a trainer in a specific month")
    @GetMapping("/hours")
    public ResponseEntity<TrainingHoursDto> getTrainingHours(
            @RequestParam("username") String username,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        logUtils.info(
                log,
                "Get training hours request: username={}, year={}, month={}",
                username,
                year,
                month);
        return ResponseEntity.ok(workloadClient.getTrainingHours(username, year, month));
    }
}
