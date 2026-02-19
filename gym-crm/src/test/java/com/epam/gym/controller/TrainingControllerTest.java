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

    private AddTrainingRequest addRequest;
    private AddTrainingRequest deleteRequest;

    @BeforeEach
    void setUp() {
        Date trainingDate = new Date();
        addRequest =
                new AddTrainingRequest(
                        "trainee1",
                        "trainer1",
                        "Morning Run",
                        trainingDate,
                        60,
                        ActionType.ADD);
        deleteRequest =
                new AddTrainingRequest(
                        "trainee1",
                        "trainer1",
                        "Morning Run",
                        trainingDate,
                        60,
                        ActionType.DELETE);
    }

    @Test
    void addTraining_success_returnsCreated() {
        // Given
        doNothing().when(trainingService).addTraining(any(AddTrainingRequest.class));

        // When
        ResponseEntity<Void> response = trainingController.addTraining(addRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(trainingService).addTraining(any(AddTrainingRequest.class));
    }

    @Test
    void addTraining_traineeNotFound_throwsException() {
        // Given
        doThrow(new NotFoundException("Trainee not found: trainee1"))
                .when(trainingService)
                .addTraining(any(AddTrainingRequest.class));

        // When & Then
        try {
            trainingController.addTraining(addRequest);
        } catch (NotFoundException e) {
            assertEquals("Trainee not found: trainee1", e.getMessage());
        }
        verify(trainingService).addTraining(any(AddTrainingRequest.class));
    }

    @Test
    void addTraining_trainerNotFound_throwsException() {
        // Given
        doThrow(new NotFoundException("Trainer not found: trainer1"))
                .when(trainingService)
                .addTraining(any(AddTrainingRequest.class));

        // When & Then
        try {
            trainingController.addTraining(addRequest);
        } catch (NotFoundException e) {
            assertEquals("Trainer not found: trainer1", e.getMessage());
        }
        verify(trainingService).addTraining(any(AddTrainingRequest.class));
    }

    @Test
    void deleteTraining_success_returnsOk() {
        // Given
        doNothing().when(trainingService).deleteTraining(any(AddTrainingRequest.class));

        // When
        ResponseEntity<Void> response = trainingController.deleteTraining(deleteRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService).deleteTraining(any(AddTrainingRequest.class));
    }

    @Test
    void deleteTraining_notFound_throwsException() {
        // Given
        doThrow(new NotFoundException("Training not found for trainee=trainee1, trainer=trainer1"))
                .when(trainingService)
                .deleteTraining(any(AddTrainingRequest.class));

        // When & Then
        try {
            trainingController.deleteTraining(deleteRequest);
        } catch (NotFoundException e) {
            assertEquals("Training not found for trainee=trainee1, trainer=trainer1", e.getMessage());
        }
        verify(trainingService).deleteTraining(any(AddTrainingRequest.class));
    }
}
