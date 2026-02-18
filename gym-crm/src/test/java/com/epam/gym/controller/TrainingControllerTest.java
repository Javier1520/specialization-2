package com.epam.gym.controller;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.service.TrainingService;
import com.epam.gym.util.LogUtils;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock private TrainingService trainingService;

    @Mock private LogUtils logUtils;

    @Mock private WorkloadClient workloadClient;

    @InjectMocks private TrainingController trainingController;

    private AddTrainingRequest request;

    @BeforeEach
    void setUp() {
        Date trainingDate = new Date();
        request =
                new AddTrainingRequest(
                        "trainee1",
                        "trainer1",
                        "Morning Run",
                        trainingDate,
                        60,
                        ActionType.ADD);
    }

    @Test
    void updateTraining_success_returnsCreated() {
        // Given
        doNothing().when(trainingService).updateTraining(any(AddTrainingRequest.class));

        // When
        ResponseEntity<Void> response = trainingController.updateTraining(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(trainingService).updateTraining(any(AddTrainingRequest.class));
    }

    @Test
    void updateTraining_traineeNotFound_throwsException() {
        // Given
        doThrow(new NotFoundException("Trainee not found: trainee1"))
                .when(trainingService)
                .updateTraining(any(AddTrainingRequest.class));

        // When & Then
        try {
            trainingController.updateTraining(request);
        } catch (NotFoundException e) {
            assertEquals("Trainee not found: trainee1", e.getMessage());
        }
        verify(trainingService).updateTraining(any(AddTrainingRequest.class));
    }

    @Test
    void updateTraining_trainerNotFound_throwsException() {
        // Given
        doThrow(new NotFoundException("Trainer not found: trainer1"))
                .when(trainingService)
                .updateTraining(any(AddTrainingRequest.class));

        // When & Then
        try {
            trainingController.updateTraining(request);
        } catch (NotFoundException e) {
            assertEquals("Trainer not found: trainer1", e.getMessage());
        }
        verify(trainingService).updateTraining(any(AddTrainingRequest.class));
    }
}
