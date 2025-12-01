package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingMapperTest {

    private TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);

    private Training training;
    private Trainer trainer;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainer = Trainer.builder()
                .id(1L)
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .specialization(TrainingType.Type.CARDIO)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .username("trainee1")
                .firstName("John")
                .lastName("Doe")
                .build();

        training = Training.builder()
                .id(1L)
                .name("Morning Run")
                .date(new Date())
                .duration(60)
                .specialization(TrainingType.Type.CARDIO)
                .trainer(trainer)
                .trainee(trainee)
                .build();
    }

    @Test
    void toResponse_mapsCorrectly() {
        // When
        TrainingResponse result = trainingMapper.toResponse(training);

        // Then
        assertNotNull(result);
        assertEquals("Morning Run", result.trainingName());
        assertEquals(training.getDate(), result.trainingDate());
        assertEquals(TrainingType.Type.CARDIO, result.trainingType());
        assertEquals(60, result.trainingDuration());
        assertEquals("Trainer One", result.trainerName());
        assertEquals("John Doe", result.traineeName());
    }

    @Test
    void toResponse_nullTrainer_handlesGracefully() {
        // Given
        training.setTrainer(null);

        // When
        TrainingResponse result = trainingMapper.toResponse(training);

        // Then
        assertNotNull(result);
        assertNull(result.trainerName());
        assertEquals("John Doe", result.traineeName());
    }

    @Test
    void toResponse_nullTrainee_handlesGracefully() {
        // Given
        training.setTrainee(null);

        // When
        TrainingResponse result = trainingMapper.toResponse(training);

        // Then
        assertNotNull(result);
        assertEquals("Trainer One", result.trainerName());
        assertNull(result.traineeName());
    }

    @Test
    void toResponseList_mapsListCorrectly() {
        // Given
        Training training2 = Training.builder()
                .id(2L)
                .name("Evening Workout")
                .date(new Date())
                .duration(45)
                .specialization(TrainingType.Type.STRENGTH)
                .trainer(trainer)
                .trainee(trainee)
                .build();

        List<Training> trainings = List.of(training, training2);

        // When
        List<TrainingResponse> result = trainingMapper.toResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Morning Run", result.get(0).trainingName());
        assertEquals("Evening Workout", result.get(1).trainingName());
    }

    @Test
    void toResponseList_emptyList_returnsEmptyList() {
        // Given
        List<Training> trainings = List.of();

        // When
        List<TrainingResponse> result = trainingMapper.toResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}

