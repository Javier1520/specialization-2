package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.UsernamePasswordGenerator;
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

    private static final int PASSWORD_LENGTH = 10;

    public Trainee createTrainee(Trainee payload) {
        validateTraineePayload(payload);
        prepareTrainee(payload);
        Trainee saved = traineeRepository.save(payload);
        printLog("Created trainee", saved);
        log.info("Created trainee username={} id={}", saved.getUsername(), saved.getId());
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

    private static void printLog(String message, Trainee saved) {
        log.info("{} username={} id={}", message, saved.getUsername(), saved.getId());
    }

    private void validateTraineePayload(Trainee t) {
        if (t == null) {
            throw new ValidationException("Trainee payload required");
        }

        if (t.getFirstName().isBlank()) {
            throw new ValidationException("firstName required");
        }

        if (t.getLastName().isBlank()) {
            throw new ValidationException("lastName required");
        }

        if (t.getDateOfBirth() != null) {
        throwIfInFuture(t.getDateOfBirth());
        }
    }

    private void throwIfInFuture(Date date) {
        if (date.after(new Date())) {
            throw new ValidationException("dateOfBirth cannot be in the future");
    }
}

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        return traineeRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    @Transactional(readOnly = true)
    public Trainee getByUsernameWithTrainers(String username) {
        return traineeRepository.findByUsernameWithTrainers(username).orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    public void changePassword(String username, String newPassword) {
        Optional.ofNullable(newPassword)
            .filter(password -> password.length() == PASSWORD_LENGTH)
            .orElseThrow(() -> new ValidationException("Password must be at least 10 characters"));

        Trainee trainee = traineeRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        trainee.setPassword(newPassword);
        traineeRepository.save(trainee);
        log.info("Changed password for trainee {}", username);
    }

    public Trainee updateTrainee(String username, Trainee update) {
        Trainee existing = traineeRepository.findByUsernameWithTrainers(username).orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        validateTraineePayload(update);

        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        existing.setAddress(update.getAddress());
        existing.setDateOfBirth(update.getDateOfBirth());
        existing.setIsActive(update.getIsActive());
        traineeRepository.save(existing);
        log.info("Updated trainee {}", username);
        return existing;
    }

    public void setActive(String username, boolean active) {
        Trainee trainee = traineeRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        Optional.of(trainee.getIsActive())
            .filter(currentActive -> Objects.equals(currentActive, active))
            .ifPresent(currentActive -> {
                throw new ValidationException("Trainee already " + (active ? "active" : "inactive"));
            });

        trainee.setIsActive(active);
        traineeRepository.save(trainee);
        log.info("Set trainee {} active={}", username, active);
    }

    public void deleteByUsername(String username) {
        Trainee t = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        Optional.ofNullable(t.getTrainers())
                .ifPresent(trainers -> {
                    trainers.forEach(trainer -> trainer.getTrainees().remove(t));
                    trainerRepository.saveAll(trainers);
                });

        traineeRepository.deleteByUsername(username);
        log.info("Deleted trainee {}", username);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, Date from, Date to, String trainerName, TrainingType.Type trainingType) {
        return trainingRepository.findByTraineeUsernameAndCriteria(username, from, to, trainerName, trainingType);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        Trainee t = traineeRepository.findByUsername(traineeUsername).orElseThrow( () -> new NotFoundException("Trainee not found: " + traineeUsername));
        return trainerRepository.findNotAssignedToTrainee(t.getId());
    }

    public void updateTraineeTrainers(String traineeUsername, List<Long> trainerIds) {
        Trainee t = traineeRepository.findByUsername(traineeUsername).orElseThrow( () -> new NotFoundException("Trainee not found: " + traineeUsername));

        List<Trainer> trainers = trainerRepository.findAllById(trainerIds);
        if (trainers.size() != trainerIds.size()) {
            throw new ValidationException("Some trainer ids not found");
        }

        // Synchronize both sides: remove existing links and set new ones
        // Remove trainee from old trainers not in new list
        t.getTrainers().forEach(oldTrainer -> oldTrainer.getTrainees().remove(t));
        t.getTrainers().clear();

        // add new trainers
        for (Trainer trainer : trainers) {
            t.getTrainers().add(trainer);
            trainer.getTrainees().add(t);
        }

        traineeRepository.save(t);
        log.info("Updated trainers for trainee {} to {}", traineeUsername, trainerIds);
    }
}
