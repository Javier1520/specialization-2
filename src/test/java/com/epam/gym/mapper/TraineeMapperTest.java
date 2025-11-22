package com.epam.gym.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.response.TraineeInfoResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;

class TraineeMapperTest {

    private TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);

    private TraineeRegistrationRequest registrationRequest;
    private UpdateTraineeRequest updateRequest;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        registrationRequest = TraineeRegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(new Date())
                .address("123 Main St")
                .build();

        updateRequest = UpdateTraineeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(new Date())
                .address("456 Oak Ave")
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .specialization(TrainingType.Type.CARDIO)
                .build();

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
    }

    @Test
    void toEntity_fromRegistrationRequest_mapsCorrectly() {
        // When
        Trainee result = traineeMapper.toEntity(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(registrationRequest.getDateOfBirth(), result.getDateOfBirth());
        assertEquals("123 Main St", result.getAddress());
        assertNull(result.getId());
        assertNull(result.getUsername());
        assertNull(result.getPassword());
        assertNull(result.getIsActive());
        assertNull(result.getTrainers());
        assertNull(result.getTrainings());
    }

    @Test
    void updateEntityFromRequest_updatesFieldsCorrectly() {
        // Given
        Trainee existing = Trainee.builder()
                .id(1L)
                .username("john.doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(new Date())
                .address("123 Main St")
                .build();

        // When
        traineeMapper.updateEntityFromRequest(updateRequest, existing);

        // Then
        assertEquals("Jane", existing.getFirstName());
        assertEquals("Smith", existing.getLastName());
        assertEquals(updateRequest.getDateOfBirth(), existing.getDateOfBirth());
        assertEquals("456 Oak Ave", existing.getAddress());
        assertEquals(1L, existing.getId());
        assertEquals("john.doe", existing.getUsername());
        assertEquals("password123", existing.getPassword());
    }

    @Test
    void toProfileResponse_mapsCorrectly() {
        // Given
        trainee.getTrainers().add(trainer);

        // When
        TraineeProfileResponse result = traineeMapper.toProfileResponse(trainee);

        // Then
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(trainee.getDateOfBirth(), result.getDateOfBirth());
        assertEquals("123 Main St", result.getAddress());
        assertEquals(true, result.getIsActive());
        assertNotNull(result.getTrainers());
        assertEquals(1, result.getTrainers().size());
    }

    @Test
    void toProfileResponse_nullTrainers_handlesGracefully() {
        // Given
        trainee.setTrainers(null);

        // When
        TraineeProfileResponse result = traineeMapper.toProfileResponse(trainee);

        // Then
        assertNotNull(result);
        assertNull(result.getTrainers());
    }

    @Test
    void toInfoResponse_mapsCorrectly() {
        // When
        TraineeInfoResponse result = traineeMapper.toInfoResponse(trainee);

        // Then
        assertNotNull(result);
        assertEquals("john.doe", result.getTraineeUsername());
        assertEquals("John", result.getTraineeFirstName());
        assertEquals("Doe", result.getTraineeLastName());
    }

    @Test
    void trainersToInfoResponseList_mapsListCorrectly() {
        // Given
        List<Trainer> trainers = List.of(trainer);

        // When
        List<TrainerInfoResponse> result = traineeMapper.trainersToInfoResponseList(trainers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("trainer1", result.get(0).getTrainerUsername());
        assertEquals("Trainer", result.get(0).getTrainerFirstName());
        assertEquals("One", result.get(0).getTrainerLastName());
        assertEquals(TrainingType.Type.CARDIO, result.get(0).getTrainerSpecialization());
    }

    @Test
    void trainersToInfoResponseList_nullList_returnsNull() {
        // When
        List<TrainerInfoResponse> result = traineeMapper.trainersToInfoResponseList(null);

        // Then
        assertNull(result);
    }

    @Test
    void trainerToInfoResponse_mapsCorrectly() {
        // When
        TrainerInfoResponse result = traineeMapper.trainerToInfoResponse(trainer);

        // Then
        assertNotNull(result);
        assertEquals("trainer1", result.getTrainerUsername());
        assertEquals("Trainer", result.getTrainerFirstName());
        assertEquals("One", result.getTrainerLastName());
        assertEquals(TrainingType.Type.CARDIO, result.getTrainerSpecialization());
    }
}

