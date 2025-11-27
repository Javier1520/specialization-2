package com.epam.gym.controller;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.TrainingService;
import com.epam.gym.util.LogUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController {
    private final TrainingService trainingService;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final LogUtils logUtils;

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        logUtils.info(log, "Add training request: traineeUsername={}, trainerUsername={}, trainingName={}",
                request.traineeUsername(), request.trainerUsername(), request.trainingName());

        Trainee trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found: " +
                        request.traineeUsername()));

        Trainer trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found: " +
                        request.trainerUsername()));

        Training training = Training.builder()
                .name(request.trainingName())
                .date(request.trainingDate())
                .duration(request.trainingDuration())
                .specialization(trainer.getSpecialization())
                .trainee(trainee)
                .trainer(trainer)
                .build();

        trainingService.addTraining(training);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

