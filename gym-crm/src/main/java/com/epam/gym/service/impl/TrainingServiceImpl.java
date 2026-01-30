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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingMapper trainingMapper;
    private final LogUtils logUtils;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingMapper trainingMapper,
            LogUtils logUtils) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingMapper = trainingMapper;
        this.logUtils = logUtils;
    }

    @Transactional
    public void addTraining(AddTrainingRequest request) {
        logUtils.info(log, "Add training request: {}", request);
        Trainee trainee =
                traineeRepository
                        .findByUsername(request.traineeUsername())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Trainee not found: " + request.traineeUsername()));
        Trainer trainer =
                trainerRepository
                        .findByUsername(request.trainerUsername())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Trainer not found: " + request.trainerUsername()));

        Training training = trainingMapper.toEntity(request);
        setAdditionalInfo(training, trainer, trainee);

        Training saved = trainingRepository.save(training);
        logUtils.info(
                log,
                "Created training id={} trainee={} trainer={}",
                saved.getId(),
                saved.getTrainee().getUsername(),
                saved.getTrainer().getUsername());
    }

    private void setAdditionalInfo(Training training, Trainer trainer, Trainee trainee) {
        training.setSpecialization(trainer.getSpecialization());
        training.setTrainee(trainee);
        training.setTrainer(trainer);
    }
}
