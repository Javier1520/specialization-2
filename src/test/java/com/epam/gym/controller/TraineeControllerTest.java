package com.epam.gym.controller;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.TraineeService;
import com.epam.gym.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private LogUtils logUtils;

    @InjectMocks
    private TraineeController traineeController;

    private Trainee trainee;
    private Trainer trainer;
    private Training training;

    @BeforeEach
    void setUp() {
        trainee = Trainee.builder()
                .id(1L)
                .username("john.doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .dateOfBirth(new Date())
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .specialization(TrainingType.Type.CARDIO)
                .build();

        training = Training.builder()
                .id(1L)
                .name("Morning Run")
                .date(new Date())
                .duration(60)
                .specialization(TrainingType.Type.CARDIO)
                .trainee(trainee)
                .trainer(trainer)
                .build();
    }

    @Test
    void register_success_returnsCreated() {
        // Given
        Date dateOfBirth = new Date();
        TraineeRegistrationRequest request = new TraineeRegistrationRequest("John", "Doe",
                dateOfBirth, "123 Main St");

        Trainee createdTrainee = Trainee.builder()
                .username("john.doe")
                .password("password123")
                .build();

        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeService.createTrainee(any(Trainee.class))).thenReturn(createdTrainee);

        // When
        ResponseEntity<RegistrationResponse> response = traineeController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().username());
        assertEquals("password123", response.getBody().password());
        verify(traineeMapper).toEntity(request);
        verify(traineeService).createTrainee(any(Trainee.class));
    }

    @Test
    void getProfile_success_returnsOk() {
        // Given
        String username = "john.doe";
        TraineeProfileResponse profileResponse = new TraineeProfileResponse(
                "john.doe", "John", "Doe", null, null, true,
                List.of());

        when(traineeService.getByUsernameWithTrainers(username)).thenReturn(trainee);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        // When
        ResponseEntity<TraineeProfileResponse> response = traineeController.getProfile(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService).getByUsernameWithTrainers(username);
        verify(traineeMapper).toProfileResponse(trainee);
    }

    @Test
    void updateProfile_success_returnsOk() {
        // Given
        String username = "john.doe";
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                username, "Jane", "Smith", null, null, true);

        TraineeProfileResponse profileResponse = new TraineeProfileResponse(
                username, "Jane", "Smith", null, null, true, List.of());

        when(traineeService.getByUsername(username)).thenReturn(trainee);
        doNothing().when(traineeMapper).updateEntityFromRequest(request, trainee);
        when(traineeService.updateTrainee(username, trainee)).thenReturn(trainee);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        // When
        ResponseEntity<TraineeProfileResponse> response = traineeController.updateProfile(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).getByUsername(username);
        verify(traineeMapper).updateEntityFromRequest(request, trainee);
        verify(traineeService).updateTrainee(username, trainee);
        verify(traineeMapper).toProfileResponse(trainee);
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

        List<Training> trainings = List.of(training);
        List<TrainingResponse> trainingResponses = List.of(
                new TrainingResponse("Training1", new Date(), TrainingType.Type.CARDIO, 60,
                        "Trainer1", "Trainee1"));

        when(traineeService.getTraineeTrainings(username, filter))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainingResponse>> response = traineeController.getTrainings(
                username, filter);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService).getTraineeTrainings(username, filter);
        verify(trainingMapper).toResponseList(trainings);
    }

    @Test
    void getNotAssignedTrainers_success_returnsOk() {
        // Given
        String username = "john.doe";
        List<Trainer> trainers = List.of(trainer);
        TrainerInfoResponse trainerInfoResponse = new TrainerInfoResponse(
                "trainer1", "Trainer", "One", TrainingType.Type.CARDIO);

        when(traineeService.getTrainersNotAssignedToTrainee(username)).thenReturn(trainers);
        when(traineeMapper.trainerToInfoResponse(trainer)).thenReturn(trainerInfoResponse);

        // When
        ResponseEntity<List<TrainerInfoResponse>> response = traineeController.getNotAssignedTrainers(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService).getTrainersNotAssignedToTrainee(username);
    }

    @Test
    void updateTrainers_success_returnsOk() {
        // Given
        String username = "john.doe";
        UpdateTraineeTrainersRequest.TrainerUsernameRequest trainerRequest =
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("trainer1");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(trainerRequest));

        List<TrainerInfoResponse> trainerResponses = List.of(
                new TrainerInfoResponse("trainer1", "Trainer", "One",
                        TrainingType.Type.CARDIO));

        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
        doNothing().when(traineeService).updateTraineeTrainers(username, List.of(1L));
        when(traineeService.getByUsernameWithTrainers(username)).thenReturn(trainee);
        when(traineeMapper.trainersToInfoResponseList(trainee.getTrainers())).thenReturn(trainerResponses);

        // When
        ResponseEntity<List<TrainerInfoResponse>> response = traineeController.updateTrainers(username, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainerResponses, response.getBody());
        verify(trainerRepository).findByUsername("trainer1");
        verify(traineeService).updateTraineeTrainers(username, List.of(1L));
        verify(traineeService).getByUsernameWithTrainers(username);
        verify(traineeMapper).trainersToInfoResponseList(trainee.getTrainers());
    }

    @Test
    void updateTrainers_trainerNotFound_throwsException() {
        // Given
        String username = "john.doe";
        UpdateTraineeTrainersRequest.TrainerUsernameRequest trainerRequest =
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("nonexistent");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of(trainerRequest));

        when(trainerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        try {
            traineeController.updateTrainers(username, request);
        } catch (NotFoundException e) {
            assertEquals("Trainer not found: nonexistent", e.getMessage());
        }
        verify(trainerRepository).findByUsername("nonexistent");
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

