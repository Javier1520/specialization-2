package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    public static final int PASSWORD_LENGTH = 10;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final LogUtils logUtils;

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        validateCredentials(username, password);

        if (authenticateTrainee(username, password)) {
            return;
        }

        if (authenticateTrainer(username, password)) {
            return;
        }

        throw new NotFoundException("User not found: " + username);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        validateNewPassword(newPassword);

        if (changeTraineePassword(username, oldPassword, newPassword)) {
            return;
        }

        if (changeTrainerPassword(username, oldPassword, newPassword)) {
            return;
        }

        throw new NotFoundException("User not found: " + username);
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }
    }

    private boolean authenticateTrainee(String username, String password) {
        Trainee trainee = traineeRepository.findByUsername(username).orElse(null);
        if (trainee != null) {
            verifyPassword(password, trainee.getPassword());
            verifyUserActive(trainee.getIsActive(), "Trainee");
            logUtils.info(log, "Authenticated trainee: {}", username);
            return true;
        }
        return false;
    }

    private boolean authenticateTrainer(String username, String password) {
        Trainer trainer = trainerRepository.findByUsername(username).orElse(null);
        if (trainer != null) {
            verifyPassword(password, trainer.getPassword());
            verifyUserActive(trainer.getIsActive(), "Trainer");
            logUtils.info(log, "Authenticated trainer: {}", username);
            return true;
        }
        return false;
    }

    private void verifyPassword(String provided, String stored) {
        verifyPassword(provided, stored, "Invalid username or password");
    }

    private void verifyPassword(String provided, String stored, String errorMessage) {
        if (!stored.equals(provided)) {
            throw new ValidationException(errorMessage);
        }
    }

    private void verifyUserActive(Boolean isActive, String userType) {
        if (!Boolean.TRUE.equals(isActive)) {
            throw new ValidationException(userType + " account is inactive");
        }
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < PASSWORD_LENGTH) {
            throw new ValidationException("New password must be at least " + PASSWORD_LENGTH + " characters");
        }
    }

    private boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        Trainee trainee = traineeRepository.findByUsername(username).orElse(null);
        if (trainee != null) {
            verifyPassword(oldPassword, trainee.getPassword(), "Invalid old password");
            trainee.setPassword(newPassword);
            traineeRepository.save(trainee);
            logUtils.info(log, "Changed password for trainee: {}", username);
            return true;
        }
        return false;
    }

    private boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        Trainer trainer = trainerRepository.findByUsername(username).orElse(null);
        if (trainer != null) {
            verifyPassword(oldPassword, trainer.getPassword(), "Invalid old password");
            trainer.setPassword(newPassword);
            trainerRepository.save(trainer);
            logUtils.info(log, "Changed password for trainer: {}", username);
            return true;
        }
        return false;
    }
}

