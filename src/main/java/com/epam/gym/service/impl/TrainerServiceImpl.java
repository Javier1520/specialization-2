package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final UsernamePasswordGenerator usernamePasswordGenerator;

    public Trainer createTrainer(Trainer payload) {
        validateTrainerPayload(payload);

        prepareTrainer(payload);

        Trainer saved = trainerRepository.save(payload);
        printLog("Created trainer", saved);
        log.info("Created trainer username={} id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    private void prepareTrainer(Trainer payload) {
        payload.setUsername(generateUsername(payload));
        payload.setPassword(getGeneratePassword());
        payload.setIsActive(true);
    }

    private String generateUsername(Trainer payload) {
        return usernamePasswordGenerator.generateUsername(
                payload.getFirstName(), payload.getLastName(),
                this::existsByUsername
        );
    }

    private boolean existsByUsername(String candidate) {
        return trainerRepository.existsByUsername(candidate) || traineeRepository.existsByUsername(candidate);
    }

    private String getGeneratePassword() {
        return usernamePasswordGenerator.generatePassword();
    }

    private static void printLog(String message, Trainer saved) {
        log.info("{} username={} id={}", message, saved.getUsername(), saved.getId());
    }

    private void validateTrainerPayload(Trainer t) {
        if (t == null) {
            throw new ValidationException("Trainer payload required");
        }

        if (t.getFirstName() == null || t.getFirstName().isBlank()) {
            throw new ValidationException("firstName required");
        }

        if (t.getLastName() == null || t.getLastName().isBlank()) {
            throw new ValidationException("lastName required");
        }

        if (t.getSpecialization() == null) {
            throw new ValidationException("specialization required");
        }
    }


    @Transactional(readOnly = true)
    public Trainer getByUsername(String username) {
        return findTrainerByUsername(username);
    }

    @Transactional(readOnly = true)
    public Trainer getByUsernameWithTrainees(String username) {
        return findTrainerByUsernameWithTrainees(username);
    }

    public void changePassword(String username, String newPassword) {
        validatePasswordLength(newPassword);
        Trainer t = findTrainerByUsername(username);
        t.setPassword(newPassword);
        trainerRepository.save(t);
        log.info("Changed password for trainer {}", username);
    }

    public Trainer updateTrainer(String username, Trainer update) {
        Trainer existing = findTrainerByUsernameWithTrainees(username);
        validateTrainerPayload(update);
        updateTrainerFields(existing, update);
        trainerRepository.save(existing);
        log.info("Updated trainer {}", username);
        return existing;
    }

    public void setActive(String username, boolean active) {
        Trainer t = findTrainerByUsername(username);
        validateActiveStatusChange(t.getIsActive(), active);
        t.setIsActive(active);
        trainerRepository.save(t);
        log.info("Set trainer {} active={}", username, active);
    }

    //Get Trainer Trainings List by trainer username and criteria (from date, to date, trainee name).
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, Date from, Date to, String traineeName) {
        return trainingRepository.findByTrainerUsernameAndCriteria(username, from, to, traineeName);
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private Trainer findTrainerByUsernameWithTrainees(String username) {
        return trainerRepository.findByUsernameWithTrainees(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private void validatePasswordLength(String password) {
        Optional.ofNullable(password)
                .filter(pwd -> pwd.length() >= 10)
                .orElseThrow(() -> new ValidationException("Password must be at least 10 chars"));
    }

    private void updateTrainerFields(Trainer existing, Trainer update) {
        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        existing.setIsActive(update.getIsActive());
    }

    private void validateActiveStatusChange(Boolean current, boolean newStatus) {
        if (Objects.equals(current, newStatus)) {
            throw new ValidationException("Trainer already " + (newStatus ? "active" : "inactive"));
        }
    }
}
