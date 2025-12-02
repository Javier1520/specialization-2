package com.epam.gym.service.impl;

import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.UsernamePasswordGenerator;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LogUtils logUtils;

    public Trainer createTrainer(Trainer payload) {
        validateTrainerPayload(payload);

        prepareTrainer(payload);

        Trainer saved = trainerRepository.save(payload);
        logUtils.info(log, "Created trainer username={} id={}", saved.getUsername(), saved.getId());
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



    private void validateTrainerPayload(Trainer t) {
        Optional.ofNullable(t)
                .orElseThrow(() -> new ValidationException("Trainer payload required"));

        Optional.ofNullable(t.getFirstName())
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("firstName required"));

        Optional.ofNullable(t.getLastName())
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("lastName required"));

        Optional.ofNullable(t.getSpecialization())
                .orElseThrow(() -> new ValidationException("specialization required"));
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
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
        logUtils.info(log, "Changed password for trainer {}", username);
    }

    public Trainer updateTrainer(String username, Trainer update) {
        Trainer existing = findTrainerByUsernameWithTrainees(username);
        validateTrainerPayload(update);
        updateTrainerFields(existing, update);
        trainerRepository.save(existing);
        logUtils.info(log, "Updated trainer {}", username);
        return existing;
    }

    public void setActive(String username, boolean active) {
        Trainer t = findTrainerByUsername(username);
        validateActiveStatusChange(t.getIsActive(), active);
        t.setIsActive(active);
        trainerRepository.save(t);
        logUtils.info(log, "Set trainer {} active={}", username, active);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, TrainerTrainingFilterRequest filter) {
        return trainingRepository.findByTrainerUsernameAndCriteria(
                username,
                filter.periodFrom(),
                filter.periodTo(),
                filter.traineeName()
        );
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
        Optional.ofNullable(current)
                .filter(c -> Objects.equals(c, newStatus))
                .ifPresent(c -> {
                    throw new ValidationException("Trainer already " + (newStatus ? "active" : "inactive"));
                });
    }
}
