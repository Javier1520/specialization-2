package com.epam.gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.TrainingService;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainingController trainingController;

    private Trainee trainee;
    private Trainer trainer;
    private AddTrainingRequest request;

    @BeforeEach
    void setUp() {
        trainee = Trainee.builder()
                .id(1L)
                .username("trainee1")
                .firstName("John")
                .lastName("Doe")
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .specialization(TrainingType.Type.CARDIO)
                .build();

        Date trainingDate = new Date();
        request = new AddTrainingRequest("trainee1", "trainer1", "Morning Run", trainingDate, 60);
    }

    @Test
    void addTraining_success_returnsCreated() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
        when(trainingService.addTraining(any(Training.class))).thenReturn(mock(Training.class));

        // When
        ResponseEntity<Void> response = trainingController.addTraining(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainingService).addTraining(any(Training.class));
    }

    @Test
    void addTraining_traineeNotFound_throwsException() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());

        // When & Then
        try {
            trainingController.addTraining(request);
        } catch (NotFoundException e) {
            assertEquals("Trainee not found: trainee1", e.getMessage());
        }
        verify(traineeRepository).findByUsername("trainee1");
    }

    @Test
    void addTraining_trainerNotFound_throwsException() {
        // Given
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

        // When & Then
        try {
            trainingController.addTraining(request);
        } catch (NotFoundException e) {
            assertEquals("Trainer not found: trainer1", e.getMessage());
        }
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository).findByUsername("trainer1");
    }
}

