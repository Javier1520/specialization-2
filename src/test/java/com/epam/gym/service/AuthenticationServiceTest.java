package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.impl.AuthenticationServiceImpl;
import com.epam.gym.util.LogUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private LogUtils logUtils;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private Trainee activeTrainee;
    private Trainee inactiveTrainee;
    private Trainer activeTrainer;
    private Trainer inactiveTrainer;

    @BeforeEach
    void setUp() {
        activeTrainee = Trainee.builder()
                .id(1L)
                .username("trainee1")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        inactiveTrainee = Trainee.builder()
                .id(2L)
                .username("trainee2")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(false)
                .build();

        activeTrainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .password("password123")
                .firstName("Trainer")
                .lastName("One")
                .isActive(true)
                .specialization(TrainingType.Type.CARDIO)
                .build();

        inactiveTrainer = Trainer.builder()
                .id(2L)
                .username("trainer2")
                .password("password123")
                .firstName("Trainer")
                .lastName("Two")
                .isActive(false)
                .specialization(TrainingType.Type.STRENGTH)
                .build();
    }

    // authenticate() tests

    @Test
    void authenticate_nullUsername_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate(null, "password123")
        );
        assertEquals("Username is required", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_blankUsername_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("   ", "password123")
        );
        assertEquals("Username is required", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_nullPassword_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainee1", null)
        );
        assertEquals("Password is required", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_blankPassword_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainee1", "   ")
        );
        assertEquals("Password is required", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_validTrainee_success() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(activeTrainee));

        // When & Then
        assertDoesNotThrow(() -> authenticationService.authenticate("trainee1", "password123"));
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_traineeWrongPassword_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(activeTrainee));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainee1", "wrongpassword")
        );
        assertEquals("Invalid username or password", exception.getMessage());
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_inactiveTrainee_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainee2")).thenReturn(Optional.of(inactiveTrainee));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainee2", "password123")
        );
        assertEquals("Trainee account is inactive", exception.getMessage());
        verify(traineeRepository).findByUsername("trainee2");
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_traineeNotFound_triesTrainer() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));

        // When & Then
        assertDoesNotThrow(() -> authenticationService.authenticate("trainer1", "password123"));
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
    }

    @Test
    void authenticate_validTrainer_success() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));

        // When & Then
        assertDoesNotThrow(() -> authenticationService.authenticate("trainer1", "password123"));
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
    }

    @Test
    void authenticate_trainerWrongPassword_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainer1", "wrongpassword")
        );
        assertEquals("Invalid username or password", exception.getMessage());
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
    }

    @Test
    void authenticate_inactiveTrainer_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainer2")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer2")).thenReturn(Optional.of(inactiveTrainer));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate("trainer2", "password123")
        );
        assertEquals("Trainer account is inactive", exception.getMessage());
        verify(traineeRepository).findByUsername("trainer2");
        verify(trainerRepository).findByUsername("trainer2");
    }

    @Test
    void authenticate_userNotFound_throwsNotFoundException() {
        // Given
        when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> authenticationService.authenticate("unknown", "password123")
        );
        assertEquals("User not found: unknown", exception.getMessage());
        verify(traineeRepository).findByUsername("unknown");
        verify(trainerRepository).findByUsername("unknown");
    }

    // changePassword() tests

    @Test
    void changePassword_nullNewPassword_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.changePassword("trainee1", "oldpass123",
                        null)
        );
        assertEquals("New password must be at least 10 characters", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void changePassword_shortNewPassword_throwsValidationException() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.changePassword("trainee1", "oldpass123",
                        "short")
        );
        assertEquals("New password must be at least 10 characters", exception.getMessage());
        verify(traineeRepository, never()).findByUsername(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void changePassword_exactly10Characters_success() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(activeTrainee);

        // When & Then
        assertDoesNotThrow(() -> authenticationService.changePassword("trainee1", "password123",
                "newpass123"));
        verify(traineeRepository).findByUsername("trainee1");
        verify(traineeRepository).save(any(Trainee.class));
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void changePassword_validTrainee_success() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(activeTrainee);

        // When
        authenticationService.changePassword("trainee1", "password123",
                "newpassword123");

        // Then
        assertEquals("newpassword123", activeTrainee.getPassword());
        verify(traineeRepository).findByUsername("trainee1");
        verify(traineeRepository).save(eq(activeTrainee));
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void changePassword_traineeWrongOldPassword_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(activeTrainee));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.changePassword("trainee1", "wrongoldpass",
                        "newpassword123")
        );
        assertEquals("Invalid old password", exception.getMessage());
        verify(traineeRepository).findByUsername("trainee1");
        verify(traineeRepository, never()).save(any());
        verify(trainerRepository, never()).findByUsername(any());
    }

    @Test
    void changePassword_traineeNotFound_triesTrainer() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(activeTrainer);

        // When
        authenticationService.changePassword("trainer1", "password123",
                "newpassword123");

        // Then
        assertEquals("newpassword123", activeTrainer.getPassword());
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainerRepository).save(eq(activeTrainer));
    }

    @Test
    void changePassword_validTrainer_success() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(activeTrainer);

        // When
        authenticationService.changePassword("trainer1", "password123",
                "newpassword123");

        // Then
        assertEquals("newpassword123", activeTrainer.getPassword());
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainerRepository).save(eq(activeTrainer));
    }

    @Test
    void changePassword_trainerWrongOldPassword_throwsValidationException() {
        // Given
        when(traineeRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(activeTrainer));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.changePassword("trainer1", "wrongoldpass",
                        "newpassword123")
        );
        assertEquals("Invalid old password", exception.getMessage());
        verify(traineeRepository).findByUsername("trainer1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void changePassword_userNotFound_throwsNotFoundException() {
        // Given
        when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> authenticationService.changePassword("unknown", "oldpass123",
                        "newpassword123")
        );
        assertEquals("User not found: unknown", exception.getMessage());
        verify(traineeRepository).findByUsername("unknown");
        verify(trainerRepository).findByUsername("unknown");
        verify(traineeRepository, never()).save(any());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void changePassword_traineeWithNullIsActive_handlesGracefully() {
        // Given
        Trainee traineeWithNullActive = Trainee.builder()
                .id(3L)
                .username("trainee3")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .isActive(null)
                .build();

        when(traineeRepository.findByUsername("trainee3")).thenReturn(Optional.of(traineeWithNullActive));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(traineeWithNullActive);

        // When
        authenticationService.changePassword("trainee3", "password123",
                "newpassword123");

        // Then
        assertEquals("newpassword123", traineeWithNullActive.getPassword());
        verify(traineeRepository).findByUsername("trainee3");
        verify(traineeRepository).save(eq(traineeWithNullActive));
    }
}

