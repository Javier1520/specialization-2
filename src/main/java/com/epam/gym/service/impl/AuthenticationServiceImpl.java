package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.model.RefreshToken;
import com.epam.gym.security.BruteForceProtectionService;
import com.epam.gym.security.JwtService;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.RefreshTokenService;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final RefreshTokenService refreshTokenService;
    private final LogUtils logUtils;

    private static final int PASSWORD_LENGTH = 10;

    @Override
    public LoginResponse authenticate(String username, String password) {
        validateCredentials(username, password);
        bruteForceProtectionService.checkAccountLocked(username);

        try {
            if (authenticateTrainee(username, password) || authenticateTrainer(username, password)) {
                bruteForceProtectionService.resetFailedAttempts(username);
                String jwt = jwtService.generateToken(username);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);
                return new LoginResponse(jwt, refreshToken.getToken());
            }
        } catch (ValidationException e) {
            bruteForceProtectionService.recordFailedAttempt(username);
            throw e;
        }

        bruteForceProtectionService.recordFailedAttempt(username);
        throw new NotFoundException("User not found: " + username);
    }

    @Override
    public LoginResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsername)
                .map(username -> {
                    String token = jwtService.generateToken(username);
                    return new LoginResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new ValidationException("Refresh token is not in database!"));
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
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
        Optional.ofNullable(username)
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("Username is required"));

        Optional.ofNullable(password)
                .filter(this::isNotBlank)
                .orElseThrow(() -> new ValidationException("Password is required"));
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean authenticateTrainee(String username, String password) {
        return traineeRepository.findByUsername(username)
                .map(trainee -> {
                    verifyPassword(password, trainee.getPassword());
                    verifyUserActive(trainee.getIsActive(), "Trainee");
                    logUtils.info(log, "Authenticated trainee: {}", username);
                    return true;
                })
                .orElse(false);
    }

    private boolean authenticateTrainer(String username, String password) {
        return trainerRepository.findByUsername(username)
                .map(trainer -> {
                    verifyPassword(password, trainer.getPassword());
                    verifyUserActive(trainer.getIsActive(), "Trainer");
                    logUtils.info(log, "Authenticated trainer: {}", username);
                    return true;
                })
                .orElse(false);
    }

    private void verifyPassword(String provided, String stored) {
        verifyPassword(provided, stored, "Invalid username or password");
    }

    private void verifyPassword(String provided, String stored, String errorMessage) {
        if (passwordEncoder.matches(provided, stored)) {
            return;
        }
        throw new ValidationException(errorMessage);
    }

    private void verifyUserActive(Boolean isActive, String userType) {
        Optional.ofNullable(isActive)
                .filter(Boolean.TRUE::equals)
                .orElseThrow(() -> new ValidationException(userType + " account is inactive"));
    }

    private void validateNewPassword(String newPassword) {
        Optional.ofNullable(newPassword)
                .filter(p -> p.length() >= PASSWORD_LENGTH)
                .orElseThrow(() -> new ValidationException("New password must be at least " + PASSWORD_LENGTH + " characters"));
    }

    private boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        return traineeRepository.findByUsername(username)
                .map(trainee -> {
                    verifyPassword(oldPassword, trainee.getPassword(), "Invalid old password");
                    trainee.setPassword(passwordEncoder.encode(newPassword));
                    traineeRepository.save(trainee);
                    logUtils.info(log, "Changed password for trainee: {}", username);
                    return true;
                })
                .orElse(false);
    }

    private boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        return trainerRepository.findByUsername(username)
                .map(trainer -> {
                    verifyPassword(oldPassword, trainer.getPassword(), "Invalid old password");
                    trainer.setPassword(passwordEncoder.encode(newPassword));
                    trainerRepository.save(trainer);
                    logUtils.info(log, "Changed password for trainer: {}", username);
                    return true;
                })
                .orElse(false);
    }
}

