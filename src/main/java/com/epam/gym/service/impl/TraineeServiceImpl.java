package com.epam.gym.service.impl;

import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.UsernamePasswordGenerator;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UsernamePasswordGenerator usernamePasswordGenerator;
    private final LogUtils logUtils;

    private static final int PASSWORD_LENGTH = 10;

    public Trainee createTrainee(Trainee payload) {
        validateTraineePayload(payload);
        prepareTrainee(payload);
        Trainee saved = traineeRepository.save(payload);
        logUtils.info(log, "Created trainee username={} id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    private void prepareTrainee(Trainee payload) {
        payload.setUsername(generateUsername(payload));
        payload.setPassword(getGeneratePassword());
        payload.setIsActive(true);
    }

    private String generateUsername(Trainee payload) {
        return usernamePasswordGenerator.generateUsername(
                payload.getFirstName(), payload.getLastName(),
                this::existsByUsername
        );
    }

    private boolean existsByUsername(String candidate) {
        return traineeRepository.existsByUsername(candidate) || trainerRepository.existsByUsername(candidate);
    }

    private String getGeneratePassword() {
        return usernamePasswordGenerator.generatePassword();
    }



    private void validateTraineePayload(Trainee t) {
        Optional.ofNullable(t)
                .orElseThrow(() -> new ValidationException("Trainee payload required"));

        Optional.of(t.getFirstName())
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("firstName required"));

        Optional.of(t.getLastName())
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("lastName required"));

        Optional.ofNullable(t.getDateOfBirth())
                .ifPresent(this::throwIfInFuture);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void throwIfInFuture(Date date) {
        Optional.of(date)
                .filter(d -> d.after(new Date()))
                .ifPresent(d -> {
                    throw new ValidationException("dateOfBirth cannot be in the future");
                });
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        return findTraineeByUsername(username);
    }

    @Transactional(readOnly = true)
    public Trainee getByUsernameWithTrainers(String username) {
        return findTraineeByUsernameWithTrainers(username);
    }

    public void changePassword(String username, String newPassword) {
        validatePasswordLength(newPassword);
        Trainee trainee = findTraineeByUsername(username);
        trainee.setPassword(newPassword);
        traineeRepository.save(trainee);
        logUtils.info(log, "Changed password for trainee {}", username);
    }

    public Trainee updateTrainee(String username, Trainee update) {
        Trainee existing = findTraineeByUsernameWithTrainers(username);
        validateTraineePayload(update);
        updateTraineeFields(existing, update);
        traineeRepository.save(existing);
        logUtils.info(log, "Updated trainee {}", username);
        return existing;
    }

    public void setActive(String username, boolean active) {
        Trainee trainee = findTraineeByUsername(username);
        validateActiveStatusChange(trainee.getIsActive(), active);
        trainee.setIsActive(active);
        traineeRepository.save(trainee);
        logUtils.info(log, "Set trainee {} active={}", username, active);
    }

    public void deleteByUsername(String username) {
        Trainee t = findTraineeByUsername(username);
        removeTraineeFromTrainers(t);
        traineeRepository.deleteByUsername(username);
        logUtils.info(log, "Deleted trainee {}", username);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, TrainingFilterRequest filter) {
        return trainingRepository.findByTraineeUsernameAndCriteria(
                username,
                filter.periodFrom(),
                filter.periodTo(),
                filter.trainerName(),
                filter.trainingType()
        );
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        Trainee t = findTraineeByUsername(traineeUsername);
        return trainerRepository.findNotAssignedToTrainee(t.getId());
    }

    public void updateTraineeTrainers(String traineeUsername, List<Long> trainerIds) {
        Trainee t = findTraineeByUsername(traineeUsername);
        List<Trainer> trainers = trainerRepository.findAllById(trainerIds);
        validateTrainerIds(trainerIds, trainers);
        updateTrainerRelationships(t, trainers);
        traineeRepository.save(t);
        logUtils.info(log, "Updated trainers for trainee {} to {}", traineeUsername, trainerIds);
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    private Trainee findTraineeByUsernameWithTrainers(String username) {
        return traineeRepository.findByUsernameWithTrainers(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    private void validatePasswordLength(String password) {
        Optional.ofNullable(password)
                .filter(pwd -> pwd.length() == PASSWORD_LENGTH)
                .orElseThrow(() -> new ValidationException("Password must be at least 10 characters"));
    }

    private void updateTraineeFields(Trainee existing, Trainee update) {
        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        existing.setAddress(update.getAddress());
        existing.setDateOfBirth(update.getDateOfBirth());
        existing.setIsActive(update.getIsActive());
    }

    private void validateActiveStatusChange(Boolean current, boolean newStatus) {
        Optional.of(current)
                .filter(currentActive -> Objects.equals(currentActive, newStatus))
                .ifPresent(currentActive -> {
                    throw new ValidationException("Trainee already " + (newStatus ? "active" : "inactive"));
                });
    }

    private void removeTraineeFromTrainers(Trainee trainee) {
        Optional.ofNullable(trainee.getTrainers())
                .ifPresent(trainers -> {
                    trainers.forEach(trainer -> trainer.getTrainees().remove(trainee));
                    trainerRepository.saveAll(trainers);
                });
    }

    private void validateTrainerIds(List<Long> trainerIds, List<Trainer> trainers) {
        if (allTrainersFound(trainers, trainerIds)) {
            return;
        }
        throw new ValidationException("Some trainer ids not found");
    }

    private boolean allTrainersFound(List<Trainer> trainers, List<Long> trainerIds) {
        return trainers.size() == trainerIds.size();
    }

    private void updateTrainerRelationships(Trainee trainee, List<Trainer> trainers) {
        clearOldTrainerRelationships(trainee);
        addNewTrainerRelationships(trainee, trainers);
    }

    private void clearOldTrainerRelationships(Trainee trainee) {
        trainee.getTrainers().forEach(oldTrainer -> oldTrainer.getTrainees().remove(trainee));
        trainee.getTrainers().clear();
    }

    private void addNewTrainerRelationships(Trainee trainee, List<Trainer> trainers) {
        for (Trainer trainer : trainers) {
            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);
        }
    }
}
