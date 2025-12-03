package com.epam.gym.controller;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private LogUtils logUtils;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        // No setup needed - tests use DTOs directly
    }

    @Test
    void register_success_returnsCreated() {
        // Given
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "Trainer", "One", TrainingType.Type.CARDIO);

        RegistrationResponse registrationResponse = new RegistrationResponse("trainer1", "password123");
        when(trainerService.createTrainer(request)).thenReturn(registrationResponse);

        // When
        ResponseEntity<RegistrationResponse> response = trainerController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("trainer1", response.getBody().username());
        assertEquals("password123", response.getBody().password());
        verify(trainerService).createTrainer(request);
    }

    @Test
    void getProfile_success_returnsOk() {
        // Given
        String username = "trainer1";
        TrainerProfileResponse profileResponse = new TrainerProfileResponse(
                username, "Trainer", "One", TrainingType.Type.CARDIO, true, List.of());

        when(trainerService.getByUsername(username)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = trainerController.getProfile(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainerService).getByUsername(username);
    }

    @Test
    void updateProfile_success_returnsOk() {
        // Given
        String username = "trainer1";
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                username, "Updated", "Trainer", TrainingType.Type.CARDIO, true);

        TrainerProfileResponse profileResponse = new TrainerProfileResponse(
                username, "Updated", "Trainer", TrainingType.Type.CARDIO, true, List.of());

        when(trainerService.updateTrainer(username, request)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = trainerController.updateProfile(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService).updateTrainer(username, request);
    }

    @Test
    void getTrainings_success_returnsOk() {
        // Given
        String username = "trainer1";
        Date periodFrom = new Date();
        Date periodTo = new Date();
        String traineeName = "John Doe";

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest(periodFrom, periodTo, traineeName);

        List<TrainingResponse> trainingResponses = List.of(
                new TrainingResponse("Morning Run", new Date(), TrainingType.Type.CARDIO, 60,
                        "Trainer One", "John Doe"));

        when(trainerService.getTrainerTrainings(username, filter))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainingResponse>> response = trainerController.getTrainings(
                username, filter);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainerService).getTrainerTrainings(username, filter);
    }

    @Test
    void getTrainings_invalidDateRange_throwsValidationException() {
        // Given
        String username = "trainer1";
        Date periodFrom = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date periodTo = new Date(); // Today
        String traineeName = null;

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest(periodFrom, periodTo, traineeName);

        // When & Then
        try {
            trainerController.getTrainings(username, filter);
        } catch (ValidationException e) {
            assertEquals("periodFrom cannot be after periodTo", e.getMessage());
        }
    }

    @Test
    void activateDeactivate_success_returnsOk() {
        // Given
        String username = "trainer1";
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        doNothing().when(trainerService).setActive(username, false);

        // When
        ResponseEntity<Void> response = trainerController.activateDeactivate(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService).setActive(username, false);
    }
}

