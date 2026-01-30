package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.openapi.annotation.operation.CreateOperation;
import com.epam.gym.service.TrainingService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Trainings", description = "Operations in Trainings")
@RestController
@RequestMapping("/api/v1/trainings")
public class TrainingController {
    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final LogUtils logUtils;

    public TrainingController(TrainingService trainingService, LogUtils logUtils) {
        this.trainingService = trainingService;
        this.logUtils = logUtils;
    }

    @CreateOperation(summary = "Add Training", description = "Add a new Training in Gym CRM")
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
}
