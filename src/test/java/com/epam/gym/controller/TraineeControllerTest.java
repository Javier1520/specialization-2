package com.epam.gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.util.LogUtils;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock private TraineeService traineeService;

    @Mock private LogUtils logUtils;

    @InjectMocks private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        // No setup needed - tests use DTOs directly
    }

    @Test
    void register_success_returnsCreated() {
        // Given
        Date dateOfBirth = new Date();
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest("John", "Doe", dateOfBirth, "123 Main St");

        RegistrationResponse registrationResponse =
                new RegistrationResponse("john.doe", "password123");

        when(traineeService.createTrainee(request)).thenReturn(registrationResponse);

        // When
        ResponseEntity<RegistrationResponse> response = traineeController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().username());
        assertEquals("password123", response.getBody().password());
        verify(traineeService).createTrainee(request);
    }

    @Test
    void getProfile_success_returnsOk() {
        // Given
        String username = "john.doe";
        TraineeProfileResponse profileResponse =
                new TraineeProfileResponse("john.doe", "John", "Doe", null, null, true, List.of());

        when(traineeService.getByUsername(username)).thenReturn(profileResponse);

        // When
        ResponseEntity<TraineeProfileResponse> response = traineeController.getProfile(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().username());
        verify(traineeService).getByUsername(username);
    }

    @Test
    void updateProfile_success_returnsOk() {
        // Given
        String username = "john.doe";
        UpdateTraineeRequest request =
                new UpdateTraineeRequest(username, "Jane", "Smith", null, null, true);

        TraineeProfileResponse profileResponse =
                new TraineeProfileResponse(username, "Jane", "Smith", null, null, true, List.of());

        when(traineeService.updateTrainee(username, request)).thenReturn(profileResponse);

        // When
        ResponseEntity<TraineeProfileResponse> response =
                traineeController.updateProfile(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().firstName());
        verify(traineeService).updateTrainee(username, request);
    }

    @Test
    void deleteProfile_success_returnsOk() {
        // Given
        String username = "john.doe";
        doNothing().when(traineeService).deleteByUsername(username);

        // When
        ResponseEntity<Void> response = traineeController.deleteProfile(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).deleteByUsername(username);
    }

    @Test
    void getTrainings_success_returnsOk() {
        // Given
        String username = "john.doe";
        Date periodFrom = new Date();
        Date periodTo = new Date();
        String trainerName = "Trainer One";
        TrainingType.Type trainingType = TrainingType.Type.CARDIO;

        TrainingFilterRequest filter =
                new TrainingFilterRequest(periodFrom, periodTo, trainerName, trainingType);

        List<TrainingResponse> trainingResponses =
                List.of(
                        new TrainingResponse(
                                "Training1",
                                new Date(),
                                TrainingType.Type.CARDIO,
                                60,
                                "Trainer1",
                                "Trainee1"));

        when(traineeService.getTraineeTrainings(username, filter)).thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainingResponse>> response =
                traineeController.getTrainings(username, filter);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(traineeService).getTraineeTrainings(username, filter);
    }

    @Test
    void getNotAssignedTrainers_success_returnsOk() {
        // Given
        String username = "john.doe";
        List<TrainerInfoResponse> trainerInfos =
                List.of(
                        new TrainerInfoResponse(
                                "trainer1", "Trainer", "One", TrainingType.Type.CARDIO));

        when(traineeService.getTrainersNotAssignedToTrainee(username)).thenReturn(trainerInfos);

        // When
        ResponseEntity<List<TrainerInfoResponse>> response =
                traineeController.getNotAssignedTrainers(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(traineeService).getTrainersNotAssignedToTrainee(username);
    }

    @Test
    void updateTrainers_success_returnsOk() {
        // Given
        String username = "john.doe";
        var trainerRequest = new UpdateTraineeTrainersRequest.TrainerUsernameRequest("trainer1");
        var request = new UpdateTraineeTrainersRequest(List.of(trainerRequest));

        List<TrainerInfoResponse> trainerResponses =
                List.of(
                        new TrainerInfoResponse(
                                "trainer1", "Trainer", "One", TrainingType.Type.CARDIO));

        when(traineeService.updateTraineeTrainers(username, request)).thenReturn(trainerResponses);

        // When
        ResponseEntity<List<TrainerInfoResponse>> response =
                traineeController.updateTrainers(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainerResponses, response.getBody());
        verify(traineeService).updateTraineeTrainers(username, request);
    }

    @Test
    void activateDeactivate_success_returnsOk() {
        // Given
        String username = "john.doe";
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        doNothing().when(traineeService).setActive(username, false);

        // When
        ResponseEntity<Void> response = traineeController.activateDeactivate(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).setActive(username, false);
    }
}
