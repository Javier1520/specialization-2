package com.epam.gym.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.model.TrainingType;

class TrainingTypeMapperTest {

    private TrainingTypeMapper trainingTypeMapper = Mappers.getMapper(TrainingTypeMapper.class);

    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainingType = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();
    }

    @Test
    void toResponse_mapsCorrectly() {
        // When
        TrainingTypeResponse result = trainingTypeMapper.toResponse(trainingType);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.trainingTypeId());
        assertEquals("Cardio", result.trainingType());
    }

    @Test
    void toResponseList_mapsListCorrectly() {
        // Given
        TrainingType cardio = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();

        TrainingType strength = TrainingType.builder()
                .id(2L)
                .name("Strength")
                .build();

        List<TrainingType> trainingTypes = List.of(cardio, strength);

        // When
        List<TrainingTypeResponse> result = trainingTypeMapper.toResponseList(trainingTypes);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).trainingTypeId());
        assertEquals("Cardio", result.get(0).trainingType());
        assertEquals(2L, result.get(1).trainingTypeId());
        assertEquals("Strength", result.get(1).trainingType());
    }

    @Test
    void toResponseList_emptyList_returnsEmptyList() {
        // Given
        List<TrainingType> trainingTypes = List.of();

        // When
        List<TrainingTypeResponse> result = trainingTypeMapper.toResponseList(trainingTypes);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}

