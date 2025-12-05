package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TrainerServiceImpl;
import com.epam.gym.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

  @Mock TrainerRepository trainerRepository;
  @Mock TrainingRepository trainingRepository;
  @Mock TraineeRepository traineeRepository;
  @Mock UsernamePasswordGenerator usernamePasswordGenerator;
  @Mock TrainerMapper trainerMapper;
  @Mock TrainingMapper trainingMapper;
  @Mock LogUtils logUtils;
  @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  @InjectMocks TrainerServiceImpl trainerService;

  private TrainerRegistrationRequest registrationRequest;

  @BeforeEach
  void setUp() {
    registrationRequest = new TrainerRegistrationRequest("Tr", "Ai", TrainingType.Type.CARDIO);
  }

  @Test
  void createTrainer_success_generatesUsernameAndSaves() {
    Trainer mappedTrainer =
        Trainer.builder()
            .firstName("Tr")
            .lastName("Ai")
            .specialization(TrainingType.Type.CARDIO)
            .build();
    Trainer saved =
        Trainer.builder().id(5L).username("tr.ai").password("pw").isActive(true).build();

    when(trainerMapper.toEntity(registrationRequest)).thenReturn(mappedTrainer);
    doReturn("tr.ai")
        .when(usernamePasswordGenerator)
        .generateUsername(anyString(), anyString(), any());
    when(usernamePasswordGenerator.generatePassword()).thenReturn("pw");
    when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(trainerRepository.existsByUsername("tr.ai")).thenReturn(false);
    lenient().when(traineeRepository.existsByUsername("tr.ai")).thenReturn(false);
    when(trainerRepository.save(any())).thenReturn(saved);

    RegistrationResponse out = trainerService.createTrainer(registrationRequest);

    assertEquals("tr.ai", out.username());
    assertEquals("pw", out.password());
    verify(trainerMapper).toEntity(registrationRequest);
    verify(trainerRepository).save(any());
  }

  @Test
  void getByUsername_notFound_throws() {
    when(trainerRepository.findByUsernameWithTrainees("x")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> trainerService.getByUsername("x"));
  }

  @Test
  void getByUsername_found_returnsTrainerProfileResponse() {
    Trainer trainer =
        Trainer.builder()
            .username("trainer1")
            .firstName("Tr")
            .lastName("Ai")
            .specialization(TrainingType.Type.CARDIO)
            .isActive(true)
            .build();
    TrainerProfileResponse response =
        new TrainerProfileResponse(
            "trainer1", "Tr", "Ai", TrainingType.Type.CARDIO, true, List.of());

    when(trainerRepository.findByUsernameWithTrainees("trainer1")).thenReturn(Optional.of(trainer));
    when(trainerMapper.toProfileResponse(trainer)).thenReturn(response);

    TrainerProfileResponse result = trainerService.getByUsername("trainer1");
    assertNotNull(result);
    assertEquals("trainer1", result.username());
    verify(trainerRepository).findByUsernameWithTrainees("trainer1");
    verify(trainerMapper).toProfileResponse(trainer);
  }

  @Test
  void changePassword_shortPassword_throws() {
    assertThrows(ValidationException.class, () -> trainerService.changePassword("u", "123"));
  }

  @Test
  void changePassword_userNotFound_throws() {
    when(trainerRepository.findByUsername("u")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> trainerService.changePassword("u", "strongpass"));
  }

  @Test
  void changePassword_success_updatesAndSaves() {
    Trainer t = Trainer.builder().username("u").password("old4567890").build();
    when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    when(trainerRepository.save(any())).thenReturn(t);

    trainerService.changePassword("u", "newstrong_gt_10_chars");

    assertEquals("newstrong_gt_10_chars", t.getPassword());
    verify(trainerRepository).save(t);
  }

  @Test
  void updateTrainer_notFound_throws() {
    UpdateTrainerRequest request =
        new UpdateTrainerRequest("no", "New", "Name", TrainingType.Type.STRENGTH, true);
    when(trainerRepository.findByUsername("no")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> trainerService.updateTrainer("no", request));
  }

  @Test
  void updateTrainer_whenTrainerExists_updatesAndReturnsTrainer() {
    // Arrange
    Trainer existing =
        Trainer.builder()
            .id(1L)
            .username("trainer.user")
            .firstName("Old")
            .lastName("Name")
            .specialization(TrainingType.Type.STRENGTH)
            .isActive(true)
            .build();

    UpdateTrainerRequest request =
        new UpdateTrainerRequest("trainer.user", "New", "Name", TrainingType.Type.STRENGTH, true);

    TrainerProfileResponse response =
        new TrainerProfileResponse(
            "trainer.user", "New", "Name", TrainingType.Type.STRENGTH, true, List.of());

    when(trainerRepository.findByUsername("trainer.user")).thenReturn(Optional.of(existing));
    when(trainerRepository.save(existing)).thenReturn(existing);
    when(trainerMapper.toProfileResponse(existing)).thenReturn(response);

    // Act
    TrainerProfileResponse result = trainerService.updateTrainer("trainer.user", request);

    // Assert
    assertEquals("New", result.firstName());
    assertEquals("Name", result.lastName());
    verify(trainerMapper).updateEntityFromRequest(request, existing);
    verify(trainerRepository).save(existing);
  }

  @Test
  void setActive_sameState_throwsValidation() {
    Trainer t = Trainer.builder().username("u").isActive(true).build();
    when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));
    assertThrows(ValidationException.class, () -> trainerService.setActive("u", true));
  }

  @Test
  void setActive_success_saves() {
    Trainer t = Trainer.builder().username("u").isActive(false).build();
    when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(trainerRepository.save(any())).thenReturn(t);

    trainerService.setActive("u", true);

    assertTrue(t.getIsActive());
    verify(trainerRepository).save(t);
  }

  @Test
  void getTrainerTrainings_callsRepositoryWithConvertedDates() {
    Training tr = Training.builder().id(1L).build();
    TrainingResponse trainingResponse =
        new TrainingResponse(
            "Training1", new Date(), TrainingType.Type.CARDIO, 60, "Trainer1", "Trainee1");

    when(trainingRepository.findByTrainerUsernameWithOptionalFilters(
            eq("t1"), any(Date.class), any(Date.class), isNull()))
        .thenReturn(List.of(tr));
    when(trainingMapper.toResponseList(List.of(tr))).thenReturn(List.of(trainingResponse));

    TrainerTrainingFilterRequest filter =
        new TrainerTrainingFilterRequest(new Date(0), new Date(), null);
    List<TrainingResponse> out = trainerService.getTrainerTrainings("t1", filter);
    assertEquals(1, out.size());
    verify(trainingRepository)
        .findByTrainerUsernameWithOptionalFilters(
            eq("t1"), any(Date.class), any(Date.class), isNull());
    verify(trainingMapper).toResponseList(List.of(tr));
  }
}
