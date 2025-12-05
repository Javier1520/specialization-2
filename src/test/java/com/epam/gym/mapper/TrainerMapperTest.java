package com.epam.gym.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.TraineeInfoResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TrainerMapperTest {

  private final TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);

  private TrainerRegistrationRequest registrationRequest;
  private UpdateTrainerRequest updateRequest;
  private Trainer trainer;
  private Trainee trainee;

  @BeforeEach
  void setUp() {
    registrationRequest =
        new TrainerRegistrationRequest("Trainer", "One", TrainingType.Type.CARDIO);

    updateRequest =
        new UpdateTrainerRequest("trainer1", "Updated", "Trainer", TrainingType.Type.CARDIO, true);

    trainee =
        Trainee.builder().id(1L).username("trainee1").firstName("John").lastName("Doe").build();

    trainer =
        Trainer.builder()
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
  }

  @Test
  void toEntity_fromRegistrationRequest_mapsCorrectly() {
    // When
    Trainer result = trainerMapper.toEntity(registrationRequest);

    // Then
    assertNotNull(result);
    assertEquals("Trainer", result.getFirstName());
    assertEquals("One", result.getLastName());
    assertEquals(TrainingType.Type.CARDIO, result.getSpecialization());
    assertNull(result.getId());
    assertNull(result.getUsername());
    assertNull(result.getPassword());
    assertNull(result.getIsActive());
    assertNull(result.getTrainings());
    assertNull(result.getTrainees());
  }

  @Test
  void updateEntityFromRequest_updatesFieldsCorrectly() {
    // Given
    Trainer existing =
        Trainer.builder()
            .id(1L)
            .username("trainer1")
            .password("password123")
            .firstName("Trainer")
            .lastName("One")
            .specialization(TrainingType.Type.CARDIO)
            .build();

    // When
    trainerMapper.updateEntityFromRequest(updateRequest, existing);

    // Then
    assertEquals("Updated", existing.getFirstName());
    assertEquals("Trainer", existing.getLastName());
    assertEquals(1L, existing.getId());
    assertEquals("trainer1", existing.getUsername());
    assertEquals("password123", existing.getPassword());
    assertEquals(TrainingType.Type.CARDIO, existing.getSpecialization());
  }

  @Test
  void toProfileResponse_mapsCorrectly() {
    // Given
    trainer.getTrainees().add(trainee);

    // When
    TrainerProfileResponse result = trainerMapper.toProfileResponse(trainer);

    // Then
    assertNotNull(result);
    assertEquals("trainer1", result.username());
    assertEquals("Trainer", result.firstName());
    assertEquals("One", result.lastName());
    assertEquals(TrainingType.Type.CARDIO, result.specialization());
    assertEquals(true, result.isActive());
    assertNotNull(result.trainees());
    assertEquals(1, result.trainees().size());
  }

  @Test
  void toProfileResponse_nullTrainees_handlesGracefully() {
    // Given
    trainer.setTrainees(null);

    // When
    TrainerProfileResponse result = trainerMapper.toProfileResponse(trainer);

    // Then
    assertNotNull(result);
    assertNull(result.trainees());
  }

  @Test
  void toInfoResponse_mapsCorrectly() {
    // When
    TrainerInfoResponse result = trainerMapper.toInfoResponse(trainer);

    // Then
    assertNotNull(result);
    assertEquals("trainer1", result.trainerUsername());
    assertEquals("Trainer", result.trainerFirstName());
    assertEquals("One", result.trainerLastName());
    assertEquals(TrainingType.Type.CARDIO, result.trainerSpecialization());
  }

  @Test
  void traineesToInfoResponseList_mapsListCorrectly() {
    // Given
    List<Trainee> trainees = List.of(trainee);

    // When
    List<TraineeInfoResponse> result = trainerMapper.traineesToInfoResponseList(trainees);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("trainee1", result.get(0).traineeUsername());
    assertEquals("John", result.get(0).traineeFirstName());
    assertEquals("Doe", result.get(0).traineeLastName());
  }

  @Test
  void traineesToInfoResponseList_nullList_returnsNull() {
    // When
    List<TraineeInfoResponse> result = trainerMapper.traineesToInfoResponseList(null);

    // Then
    assertNull(result);
  }

  @Test
  void traineeToInfoResponse_mapsCorrectly() {
    // When
    TraineeInfoResponse result = trainerMapper.traineeToInfoResponse(trainee);

    // Then
    assertNotNull(result);
    assertEquals("trainee1", result.traineeUsername());
    assertEquals("John", result.traineeFirstName());
    assertEquals("Doe", result.traineeLastName());
  }
}
