package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    private static final int MAX_TRAINING_YEARS_AHEAD = 5;

    @Transactional
    public Training addTraining(Training payload) {
        validateTrainingPayload(payload);
        prepareTrainingPayload(payload);

        Training saved = trainingRepository.save(payload);
        printLog("Created training", saved);

        return saved;
    }

    private void validateTrainingPayload(Training p) {
        if (p == null) {
            throw new ValidationException("Training payload required");
        }

        if (p.getName() == null || p.getName().isBlank()) {
            throw new ValidationException("Training name required");
        }

        Date trainingDate = p.getDate();
        if (trainingDate == null) {
            throw new ValidationException("Training date required");
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, MAX_TRAINING_YEARS_AHEAD);
        Date maxDate = cal.getTime();
        if (trainingDate.after(maxDate)) {
            throw new ValidationException("Training date unreasonable");
        }

        if (p.getDuration() == null || isNotPositive(p.getDuration())) {
            throw new ValidationException("Duration must be positive");
        }

        if (p.getTrainee() == null || p.getTrainee().getUsername() == null || p.getTrainee().getUsername().isBlank()) {
            throw new ValidationException("Trainee username required");
        }

        if (p.getTrainer() == null || p.getTrainer().getUsername() == null || p.getTrainer().getUsername().isBlank()) {
            throw new ValidationException("Trainer username required");
        }

        if (p.getSpecialization() == null) {
            throw new ValidationException("Training type required");
        }
    }

    private void prepareTrainingPayload(Training payload) {
        Trainee trainee = findTraineeByUsername(payload.getTrainee().getUsername());
        Trainer trainer = findTrainerByUsername(payload.getTrainer().getUsername());

        payload.setTrainee(trainee);
        payload.setTrainer(trainer);
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private boolean isNotPositive(Integer value) {
        return value < 0;
    }

    private static void printLog(String message, Training saved) {
        log.info("{} id={} trainee={} trainer={}",
                message,
                saved.getId(),
                saved.getTrainee().getUsername(),
                saved.getTrainer().getUsername());
    }
}
