package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }

        // Try to find as trainee first
        Trainee trainee = traineeRepository.findByUsername(username).orElse(null);
        if (trainee != null) {
            if (!trainee.getPassword().equals(password)) {
                throw new ValidationException("Invalid username or password");
            }
            if (!Boolean.TRUE.equals(trainee.getIsActive())) {
                throw new ValidationException("Trainee account is inactive");
            }
            log.info("Authenticated trainee: {}", username);
            return;
        }

        // Try to find as trainer
        Trainer trainer = trainerRepository.findByUsername(username).orElse(null);
        if (trainer != null) {
            if (!trainer.getPassword().equals(password)) {
                throw new ValidationException("Invalid username or password");
            }
            if (!Boolean.TRUE.equals(trainer.getIsActive())) {
                throw new ValidationException("Trainer account is inactive");
            }
            log.info("Authenticated trainer: {}", username);
            return;
        }

        throw new NotFoundException("User not found: " + username);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 10) {
            throw new ValidationException("New password must be at least 10 characters");
        }

        // Try to find as trainee first
        Trainee trainee = traineeRepository.findByUsername(username).orElse(null);
        if (trainee != null) {
            if (!trainee.getPassword().equals(oldPassword)) {
                throw new ValidationException("Invalid old password");
            }
            trainee.setPassword(newPassword);
            traineeRepository.save(trainee);
            log.info("Changed password for trainee: {}", username);
            return;
        }

        // Try to find as trainer
        Trainer trainer = trainerRepository.findByUsername(username).orElse(null);
        if (trainer != null) {
            if (!trainer.getPassword().equals(oldPassword)) {
                throw new ValidationException("Invalid old password");
            }
            trainer.setPassword(newPassword);
            trainerRepository.save(trainer);
            log.info("Changed password for trainer: {}", username);
            return;
        }

        throw new NotFoundException("User not found: " + username);
    }
}

