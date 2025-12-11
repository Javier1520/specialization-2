package com.epam.gym.service;

import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.RefreshToken;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.security.BruteForceProtectionService;
import com.epam.gym.security.JwtService;
import com.epam.gym.service.impl.AuthenticationServiceImpl;
import com.epam.gym.util.LogUtils;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private LogUtils logUtils;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private BruteForceProtectionService bruteForceProtectionService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthenticationServiceImpl authenticationService;

    private Trainee activeTrainee;
    private Trainee inactiveTrainee;
    private Trainer activeTrainer;
    private Trainer inactiveTrainer;

    @BeforeEach
    void setUp() {
        activeTrainee =
                Trainee.builder()
                        .id(1L)
                        .username("trainee1")
                        .password("password123")
                        .firstName("John")
                        .lastName("Doe")
                        .isActive(true)
                        .build();

        inactiveTrainee =
                Trainee.builder()
                        .id(2L)
                        .username("trainee2")
                        .password("password123")
                        .firstName("Jane")
                        .lastName("Smith")
                        .isActive(false)
                        .build();

        activeTrainer =
                Trainer.builder()
                        .id(1L)
                        .username("trainer1")
                        .password("password123")
                        .firstName("Trainer")
                        .lastName("One")
                        .isActive(true)
                        .specialization(TrainingType.Type.CARDIO)
                        .build();

        inactiveTrainer =
                Trainer.builder()
                        .id(2L)
                        .username("trainer2")
                        .password("password123")
                        .firstName("Trainer")
                        .lastName("Two")
                        .isActive(false)
                        .specialization(TrainingType.Type.STRENGTH)
                        .build();
    }

    @Nested
    @DisplayName("Authenticate Tests")
    class AuthenticateTests {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw ValidationException when username is invalid")
        void authenticate_invalidUsername_throwsValidationException(String username) {
            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.authenticate(username, "password123"));
            assertEquals("Username is required", exception.getMessage());
            verify(traineeRepository, never()).findByUsername(any());
            verify(trainerRepository, never()).findByUsername(any());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw ValidationException when password is invalid")
        void authenticate_invalidPassword_throwsValidationException(String password) {
            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.authenticate("trainee1", password));
            assertEquals("Password is required", exception.getMessage());
            verify(traineeRepository, never()).findByUsername(any());
            verify(trainerRepository, never()).findByUsername(any());
        }

        @Test
        @DisplayName("Should authenticate valid trainee successfully")
        void authenticate_validTrainee_success() {
            when(traineeRepository.findByUsername("trainee1"))
                    .thenReturn(Optional.of(activeTrainee));
            when(jwtService.generateToken("trainee1")).thenReturn("jwt-token");
            when(refreshTokenService.createRefreshToken("trainee1"))
                    .thenReturn(new RefreshToken(1L, "refresh-token", "trainee1", Instant.now()));
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);

            LoginResponse response = authenticationService.authenticate("trainee1", "password123");

            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals("refresh-token", response.refreshToken());
            verify(traineeRepository).findByUsername("trainee1");
            verify(trainerRepository, never()).findByUsername(any());
        }

        @Test
        @DisplayName("Should authenticate valid trainer successfully")
        void authenticate_validTrainer_success() {
            when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
            when(trainerRepository.findByUsername("trainer1"))
                    .thenReturn(Optional.of(activeTrainer));
            when(jwtService.generateToken("trainer1")).thenReturn("jwt-token");
            when(refreshTokenService.createRefreshToken("trainer1"))
                    .thenReturn(new RefreshToken(1L, "refresh-token", "trainer1", Instant.now()));
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);

            LoginResponse response = authenticationService.authenticate("trainer1", "password123");

            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals("refresh-token", response.refreshToken());
            verify(traineeRepository).findByUsername("trainer1");
            verify(trainerRepository).findByUsername("trainer1");
        }

        @Test
        @DisplayName("Should throw ValidationException when trainee password is wrong")
        void authenticate_traineeWrongPassword_throwsValidationException() {
            when(traineeRepository.findByUsername("trainee1"))
                    .thenReturn(Optional.of(activeTrainee));
            when(passwordEncoder.matches("wrongpassword", "password123")).thenReturn(false);

            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.authenticate("trainee1", "wrongpassword"));
            assertEquals("Invalid username or password", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ValidationException when trainee is inactive")
        void authenticate_inactiveTrainee_throwsValidationException() {
            when(traineeRepository.findByUsername("trainee2"))
                    .thenReturn(Optional.of(inactiveTrainee));
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);

            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.authenticate("trainee2", "password123"));
            assertEquals("Trainee account is inactive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ValidationException when trainer is inactive")
        void authenticate_inactiveTrainer_throwsValidationException() {
            when(traineeRepository.findByUsername("trainer2")).thenReturn(Optional.empty());
            when(trainerRepository.findByUsername("trainer2"))
                    .thenReturn(Optional.of(inactiveTrainer));
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);

            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.authenticate("trainer2", "password123"));
            assertEquals("Trainer account is inactive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void authenticate_userNotFound_throwsNotFoundException() {
            when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            NotFoundException exception =
                    assertThrows(
                            NotFoundException.class,
                            () -> authenticationService.authenticate("unknown", "password123"));
            assertEquals("User not found: unknown", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"short", "123456789"})
        @DisplayName("Should throw ValidationException when new password is too short")
        void changePassword_invalidNewPassword_throwsValidationException(String newPassword) {
            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () ->
                                    authenticationService.changePassword(
                                            "trainee1", "oldpass123", newPassword));
            assertEquals("New password must be at least 10 characters", exception.getMessage());
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should change password for valid trainee")
        void changePassword_validTrainee_success() {
            when(traineeRepository.findByUsername("trainee1"))
                    .thenReturn(Optional.of(activeTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(activeTrainee);
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);
            when(passwordEncoder.encode("newpassword123")).thenReturn("newpassword123");

            authenticationService.changePassword("trainee1", "password123", "newpassword123");

            assertEquals("newpassword123", activeTrainee.getPassword());
            verify(traineeRepository).save(activeTrainee);
        }

        @Test
        @DisplayName("Should change password for valid trainer")
        void changePassword_validTrainer_success() {
            when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
            when(trainerRepository.findByUsername("trainer1"))
                    .thenReturn(Optional.of(activeTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(activeTrainer);
            when(passwordEncoder.matches("password123", "password123")).thenReturn(true);
            when(passwordEncoder.encode("newpassword123")).thenReturn("newpassword123");

            authenticationService.changePassword("trainer1", "password123", "newpassword123");

            assertEquals("newpassword123", activeTrainer.getPassword());
            verify(trainerRepository).save(activeTrainer);
        }

        @Test
        @DisplayName("Should throw ValidationException when old password is wrong")
        void changePassword_wrongOldPassword_throwsValidationException() {
            when(traineeRepository.findByUsername("trainee1"))
                    .thenReturn(Optional.of(activeTrainee));
            when(passwordEncoder.matches("wrongoldpass", "password123")).thenReturn(false);

            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () ->
                                    authenticationService.changePassword(
                                            "trainee1", "wrongoldpass", "newpassword123"));
            assertEquals("Invalid old password", exception.getMessage());
            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should return new tokens when refresh token is valid")
        void refreshToken_validToken_success() {
            String refreshTokenStr = "valid-refresh-token";
            RefreshToken refreshToken =
                    new RefreshToken(
                            1L, refreshTokenStr, "trainee1", Instant.now().plusSeconds(3600));

            when(refreshTokenService.findByToken(refreshTokenStr))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
            when(jwtService.generateToken("trainee1")).thenReturn("new-jwt-token");

            LoginResponse response = authenticationService.refreshToken(refreshTokenStr);

            assertNotNull(response);
            assertEquals("new-jwt-token", response.token());
            assertEquals(refreshTokenStr, response.refreshToken());
        }

        @Test
        @DisplayName("Should throw ValidationException when refresh token not found")
        void refreshToken_notFound_throwsValidationException() {
            when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

            ValidationException exception =
                    assertThrows(
                            ValidationException.class,
                            () -> authenticationService.refreshToken("invalid-token"));
            assertEquals("Refresh token is not in database!", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should delete refresh token on logout")
        void logout_success() {
            String refreshToken = "some-refresh-token";
            authenticationService.logout(refreshToken);
            verify(refreshTokenService).deleteByToken(refreshToken);
        }
    }
}
