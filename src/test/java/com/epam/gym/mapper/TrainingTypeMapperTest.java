package com.epam.gym.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.model.TrainingType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TrainingTypeMapperTest {

  private final TrainingTypeMapper trainingTypeMapper = Mappers.getMapper(TrainingTypeMapper.class);

  private TrainingType trainingType;

  @BeforeEach
  void setUp() {
    trainingType = TrainingType.builder().id(1L).name("Cardio").build();
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
    List<TrainingType.Type> trainingTypes =
        List.of(TrainingType.Type.CARDIO, TrainingType.Type.STRENGTH);

    // When
    List<TrainingTypeResponse> result = trainingTypeMapper.toResponseList(trainingTypes);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("CARDIO", result.get(0).trainingType());
    assertEquals("STRENGTH", result.get(1).trainingType());
  }

  @Test
  void toResponseList_emptyList_returnsEmptyList() {
    // Given
    List<TrainingType.Type> trainingTypes = List.of();

    // When
    List<TrainingTypeResponse> result = trainingTypeMapper.toResponseList(trainingTypes);

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }
}
