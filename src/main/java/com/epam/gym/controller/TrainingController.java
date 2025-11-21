package com.epam.gym.controller;

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

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        log.info("Add training request: traineeUsername={}, trainerUsername={}, trainingName={}",
                request.getTraineeUsername(), request.getTrainerUsername(), request.getTrainingName());

        com.epam.gym.model.Trainee trainee = traineeRepository.findByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new com.epam.gym.exception.NotFoundException("Trainee not found: " + request.getTraineeUsername()));

        com.epam.gym.model.Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new com.epam.gym.exception.NotFoundException("Trainer not found: " + request.getTrainerUsername()));

        Training training = Training.builder()
                .name(request.getTrainingName())
                .date(request.getTrainingDate())
                .duration(request.getTrainingDuration())
                .specialization(trainer.getSpecialization())
                .trainee(trainee)
                .trainer(trainer)
                .build();

        trainingService.addTraining(training);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

