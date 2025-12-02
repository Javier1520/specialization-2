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
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final LogUtils logUtils;

    private static final int MAX_TRAINING_YEARS_AHEAD = 5;

    @Transactional
    public Training addTraining(Training payload) {
        validateTrainingPayload(payload);
        prepareTrainingPayload(payload);

        Training saved = trainingRepository.save(payload);
        logUtils.info(log, "Created training id={} trainee={} trainer={}",
                saved.getId(),
                saved.getTrainee().getUsername(),
                saved.getTrainer().getUsername());

        return saved;
    }

    private void validateTrainingPayload(Training p) {
        Optional.ofNullable(p)
                .orElseThrow(() -> new ValidationException("Training payload required"));

        Optional.ofNullable(p.getName())
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("Training name required"));

        Optional.ofNullable(p.getDate())
                .orElseThrow(() -> new ValidationException("Training date required"));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, MAX_TRAINING_YEARS_AHEAD);
        Date maxDate = cal.getTime();
        Optional.of(p.getDate())
                .filter(date -> date.after(maxDate))
                .ifPresent(date -> {
                    throw new ValidationException("Training date unreasonable");
                });

        Optional.ofNullable(p.getDuration())
                .filter(this::isPositive)
                .orElseThrow(() -> new ValidationException("Duration must be positive"));

        Optional.ofNullable(p.getTrainee())
                .map(Trainee::getUsername)
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("Trainee username required"));

        Optional.ofNullable(p.getTrainer())
                .map(Trainer::getUsername)
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("Trainer username required"));

        Optional.ofNullable(p.getSpecialization())
                .orElseThrow(() -> new ValidationException("Training type required"));
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

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isPositive(Integer value) {
        return value > 0;
    }


}
