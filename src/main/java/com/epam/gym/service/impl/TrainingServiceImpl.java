package com.epam.gym.service.impl;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TrainingService;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingMapper trainingMapper;
    private final LogUtils logUtils;

    @Transactional
    public void addTraining(AddTrainingRequest request) {
        logUtils.info(log, "Add training request: {}", request);
        Trainee trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + request.traineeUsername()));
        Trainer trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + request.trainerUsername()));

        Training training = trainingMapper.toEntity(request);
        training.setSpecialization(trainer.getSpecialization());
        training.setTrainee(trainee);
        training.setTrainer(trainer);

        Training saved = trainingRepository.save(training);
        logUtils.info(log, "Created training id={} trainee={} trainer={}",
                saved.getId(),
                saved.getTrainee().getUsername(),
                saved.getTrainer().getUsername());
    }
}
