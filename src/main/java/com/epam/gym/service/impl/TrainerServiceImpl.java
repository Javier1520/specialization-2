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

import java.time.LocalDate;
import java.time.ZoneId;
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
        return trainerRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    @Transactional(readOnly = true)
    public Trainer getByUsernameWithTrainees(String username) {
        return trainerRepository.findByUsernameWithTrainees(username).orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    public void changePassword(String username, String newPassword) {
        Optional.ofNullable(newPassword)
            .filter(pwd -> pwd.length() >= 10)
            .orElseThrow(() -> new ValidationException("Password must be at least 10 chars"));

        Trainer t = (trainerRepository.findByUsername(username))
            .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        t.setPassword(newPassword);
        trainerRepository.save(t);
        log.info("Changed password for trainer {}", username);
    }

    public Trainer updateTrainer(String username, Trainer update) {
        Trainer existing = trainerRepository.findByUsernameWithTrainees(username)
            .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        validateTrainerPayload(update);

        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        existing.setIsActive(update.getIsActive());

        trainerRepository.save(existing);
        log.info("Updated trainer {}", username);
        return existing;
    }

    public void setActive(String username, boolean active) {
        Trainer t = trainerRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        if (Objects.equals(t.getIsActive(), active)) {
            throw new ValidationException("Trainer already " + (active ? "active" : "inactive"));
        }

        t.setIsActive(active);
        trainerRepository.save(t);
        log.info("Set trainer {} active={}", username, active);
    }

    //Get Trainer Trainings List by trainer username and criteria (from date, to date, trainee name).
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        ZoneId zoneId = ZoneId.systemDefault();
        Date dateFrom = Date.from(from.atStartOfDay(zoneId).toInstant());
        Date dateTo = Date.from(to.atStartOfDay(zoneId).toInstant());

        return trainingRepository.findByTrainerUsernameAndCriteria(username, dateFrom, dateTo, traineeName);
    }
}
