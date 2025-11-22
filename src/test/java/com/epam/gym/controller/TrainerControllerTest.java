package com.epam.gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainerService;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainerController trainerController;

    private Trainer trainer;
    private Training training;

    @BeforeEach
    void setUp() {
        trainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .password("password123")
                .firstName("Trainer")
                .lastName("One")
                .isActive(true)
                .specialization(TrainingType.Type.CARDIO)
                .trainings(new ArrayList<>())
                .trainees(new ArrayList<>())
                .build();

        training = Training.builder()
                .id(1L)
                .name("Morning Run")
                .date(new Date())
                .duration(60)
                .specialization(TrainingType.Type.CARDIO)
                .trainer(trainer)
                .build();
    }

    @Test
    void register_success_returnsCreated() {
        // Given
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Trainer");
        request.setLastName("One");
        request.setSpecialization(TrainingType.Type.CARDIO);

        Trainer createdTrainer = Trainer.builder()
                .username("trainer1")
                .password("password123")
                .build();

        when(trainerMapper.toEntity(request)).thenReturn(trainer);
        when(trainerService.createTrainer(any(Trainer.class))).thenReturn(createdTrainer);

        // When
        ResponseEntity<RegistrationResponse> response = trainerController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("trainer1", response.getBody().getUsername());
        assertEquals("password123", response.getBody().getPassword());
        verify(trainerMapper).toEntity(request);
        verify(trainerService).createTrainer(any(Trainer.class));
    }

    @Test
    void getProfile_success_returnsOk() {
        // Given
        String username = "trainer1";
        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        profileResponse.setFirstName("Trainer");
        profileResponse.setLastName("One");

        when(trainerService.getByUsernameWithTrainees(username)).thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = trainerController.getProfile(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainerService).getByUsernameWithTrainees(username);
        verify(trainerMapper).toProfileResponse(trainer);
    }

    @Test
    void updateProfile_success_returnsOk() {
        // Given
        String username = "trainer1";
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setFirstName("Updated");
        request.setLastName("Trainer");

        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        profileResponse.setFirstName("Updated");
        profileResponse.setLastName("Trainer");

        when(trainerService.getByUsername(username)).thenReturn(trainer);
        doNothing().when(trainerMapper).updateEntityFromRequest(request, trainer);
        when(trainerService.updateTrainer(username, trainer)).thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = trainerController.updateProfile(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService).getByUsername(username);
        verify(trainerMapper).updateEntityFromRequest(request, trainer);
        verify(trainerService).updateTrainer(username, trainer);
        verify(trainerMapper).toProfileResponse(trainer);
    }

    @Test
    void getTrainings_success_returnsOk() {
        // Given
        String username = "trainer1";
        Date periodFrom = new Date();
        Date periodTo = new Date();
        String traineeName = "John Doe";

        List<Training> trainings = List.of(training);
        List<TrainingResponse> trainingResponses = List.of(new TrainingResponse());

        when(trainerService.getTrainerTrainings(username, periodFrom, periodTo, traineeName))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainingResponse>> response = trainerController.getTrainings(
                username, periodFrom, periodTo, traineeName);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainerService).getTrainerTrainings(username, periodFrom, periodTo, traineeName);
        verify(trainingMapper).toResponseList(trainings);
    }

    @Test
    void getTrainings_invalidDateRange_throwsValidationException() {
        // Given
        String username = "trainer1";
        Date periodFrom = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date periodTo = new Date(); // Today
        String traineeName = null;

        // When & Then
        try {
            trainerController.getTrainings(username, periodFrom, periodTo, traineeName);
        } catch (ValidationException e) {
            assertEquals("periodFrom cannot be after periodTo", e.getMessage());
        }
    }

    @Test
    void activateDeactivate_success_returnsOk() {
        // Given
        String username = "trainer1";
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        doNothing().when(trainerService).setActive(username, false);

        // When
        ResponseEntity<Void> response = trainerController.activateDeactivate(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService).setActive(username, false);
    }
}

