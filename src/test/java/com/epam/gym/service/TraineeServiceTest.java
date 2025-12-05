package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TraineeServiceImpl;
import com.epam.gym.util.LogUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

  @Mock TraineeRepository traineeRepository;
  @Mock TrainerRepository trainerRepository;
  @Mock TrainingRepository trainingRepository;
  @Mock UsernamePasswordGenerator usernamePasswordGenerator;
  @Mock TraineeMapper traineeMapper;
  @Mock TrainingMapper trainingMapper;
  @Mock LogUtils logUtils;
  @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  @InjectMocks TraineeServiceImpl traineeService;

  private TraineeRegistrationRequest registrationRequest;

  @BeforeEach
  void setUp() {
    registrationRequest = new TraineeRegistrationRequest("John", "Doe", new Date(0), "Addr");
  }

  @Test
  void createTrainee_success_generatesUsernameAndSaves() {
    Trainee mappedTrainee =
        Trainee.builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(new Date(0))
            .address("Addr")
            .build();
    Trainee saved =
        Trainee.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .username("john.doe")
            .password("pass123456")
            .isActive(true)
            .build();

    when(traineeMapper.toEntity(registrationRequest)).thenReturn(mappedTrainee);
    doReturn("john.doe")
        .when(usernamePasswordGenerator)
        .generateUsername(anyString(), anyString(), any());

    when(usernamePasswordGenerator.generatePassword()).thenReturn("pass123456");
    when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(traineeRepository.existsByUsername("john.doe")).thenReturn(false);
    lenient().when(trainerRepository.existsByUsername("john.doe")).thenReturn(false);
    when(traineeRepository.save(any(Trainee.class))).thenReturn(saved);

    RegistrationResponse result = traineeService.createTrainee(registrationRequest);

    assertNotNull(result);
    assertEquals("john.doe", result.username());
    assertEquals("pass123456", result.password());
    verify(traineeMapper).toEntity(registrationRequest);
    verify(usernamePasswordGenerator).generateUsername(eq("John"), eq("Doe"), any());
    verify(traineeRepository).save(any(Trainee.class));
  }

  @Test
  void getByUsername_found_returnsTraineeProfileResponse() {
    Trainee t = Trainee.builder().username("u1").build();
    TraineeProfileResponse response =
        new TraineeProfileResponse("u1", "John", "Doe", new Date(0), "Addr", true, List.of());

    when(traineeRepository.findByUsernameWithTrainers("u1")).thenReturn(Optional.of(t));
    when(traineeMapper.toProfileResponse(t)).thenReturn(response);

    TraineeProfileResponse result = traineeService.getByUsername("u1");
    assertNotNull(result);
    assertEquals("u1", result.username());
    verify(traineeRepository).findByUsernameWithTrainers("u1");
    verify(traineeMapper).toProfileResponse(t);
  }

  @Test
  void getByUsername_notFound_throws() {
    when(traineeRepository.findByUsernameWithTrainers("x")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> traineeService.getByUsername("x"));
  }

  @Test
  void changePassword_valid_updatesPasswordAndSaves() {
    Trainee t = Trainee.builder().username("u2").password("old4567890").build();
    when(traineeRepository.findByUsername("u2")).thenReturn(Optional.of(t));
    when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    when(traineeRepository.save(any())).thenReturn(t);

    traineeService.changePassword("u2", "newpass890");

    assertEquals("newpass890", t.getPassword());
    verify(traineeRepository).save(t);
  }

  @Test
  void changePassword_shortPassword_throwsValidation() {
    assertThrows(ValidationException.class, () -> traineeService.changePassword("u", "123"));
  }

  @Test
  void changePassword_userNotFound_throwsNotFound() {
    when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> traineeService.changePassword("no", "password90"));
  }

  @Test
  void updateTrainee_happyPath_updatesFields() {
    Trainee existing =
        Trainee.builder()
            .username("u")
            .firstName("A")
            .lastName("B")
            .address("old")
            .dateOfBirth(new Date(0))
            .build();
    UpdateTraineeRequest request =
        new UpdateTraineeRequest("u", "X", "Y", new Date(1), "new", true);
    TraineeProfileResponse response =
        new TraineeProfileResponse("u", "X", "Y", new Date(1), "new", true, List.of());

    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(existing));
    when(traineeRepository.save(any())).thenReturn(existing);
    when(traineeMapper.toProfileResponse(existing)).thenReturn(response);

    TraineeProfileResponse result = traineeService.updateTrainee("u", request);

    assertEquals("X", result.firstName());
    assertEquals("Y", result.lastName());
    assertEquals("new", result.address());
    verify(traineeMapper).updateEntityFromRequest(request, existing);
    verify(traineeRepository).save(existing);
  }

  @Test
  void updateTrainee_notFound_throwsNotFound() {
    UpdateTraineeRequest request =
        new UpdateTraineeRequest("missing", "X", "Y", new Date(1), "new", true);
    when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> traineeService.updateTrainee("missing", request));
  }

  @Test
  void setActive_whenSameState_throwsValidation() {
    Trainee t = Trainee.builder().username("u").isActive(true).build();
    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    assertThrows(ValidationException.class, () -> traineeService.setActive("u", true));
  }

  @Test
  void setActive_success_callsSave() {
    Trainee t = Trainee.builder().username("u").isActive(false).build();
    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(traineeRepository.save(any())).thenReturn(t);

    traineeService.setActive("u", true);
    assertTrue(t.getIsActive());
    verify(traineeRepository).save(t);
  }

  @Test
  void deleteByUsername_removesAssociationsAndDeletes() {
    Trainee t = Trainee.builder().username("u").id(10L).trainers(new ArrayList<>()).build();
    // create two trainers that reference the trainee
    Trainer trainer1 = Trainer.builder().id(1L).username("tr1").trainees(new ArrayList<>()).build();
    Trainer trainer2 = Trainer.builder().id(2L).username("tr2").trainees(new ArrayList<>()).build();
    // set both to have trainee in their trainees list
    trainer1.getTrainees().add(t);
    trainer2.getTrainees().add(t);
    t.getTrainers().add(trainer1);
    t.getTrainers().add(trainer2);

    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(trainerRepository.saveAll(anyIterable())).thenReturn(List.of(trainer1, trainer2));

    traineeService.deleteByUsername("u");

    // verify trainers had trainee removed
    assertFalse(trainer1.getTrainees().contains(t));
    assertFalse(trainer2.getTrainees().contains(t));
    verify(trainerRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    verify(traineeRepository).deleteByUsername("u");
  }

  @Test
  void deleteByUsername_notFound_throwsNotFound() {
    when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> traineeService.deleteByUsername("no"));
  }

  @Test
  void getTraineeTrainings_convertsDatesAndCallsRepository() {
    Training t = Training.builder().id(99L).build();
    TrainingResponse trainingResponse =
        new TrainingResponse(
            "Training1", new Date(), TrainingType.Type.CARDIO, 60, "Trainer1", "Trainee1");

    when(trainingRepository.findByTraineeUsernameWithOptionalFilters(
            eq("u"), any(Date.class), any(Date.class), isNull(), isNull()))
        .thenReturn(List.of(t));
    when(trainingMapper.toResponseList(List.of(t))).thenReturn(List.of(trainingResponse));

    Date fromDate = new Date(0); // epoch start (Jan 1, 1970)
    Date toDate = new Date(); // current date

    TrainingFilterRequest filter = new TrainingFilterRequest(fromDate, toDate, null, null);
    List<TrainingResponse> result = traineeService.getTraineeTrainings("u", filter);
    assertEquals(1, result.size());
    verify(trainingRepository)
        .findByTraineeUsernameWithOptionalFilters(
            eq("u"), any(Date.class), any(Date.class), isNull(), isNull());
    verify(trainingMapper).toResponseList(List.of(t));
  }

  @Test
  void getTrainersNotAssignedToTrainee_found_callsTrainerRepo() {
    Trainee t = Trainee.builder().id(15L).username("u").build();
    Trainer trainer =
        Trainer.builder()
            .id(1L)
            .username("trainer1")
            .firstName("Trainer")
            .lastName("One")
            .specialization(TrainingType.Type.CARDIO)
            .build();
    TrainerInfoResponse trainerInfo =
        new TrainerInfoResponse("trainer1", "Trainer", "One", TrainingType.Type.CARDIO);

    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(trainerRepository.findNotAssignedToTrainee(15L)).thenReturn(List.of(trainer));
    when(traineeMapper.trainerToInfoResponse(trainer)).thenReturn(trainerInfo);

    List<TrainerInfoResponse> out = traineeService.getTrainersNotAssignedToTrainee("u");
    assertNotNull(out);
    assertEquals(1, out.size());
    verify(trainerRepository).findNotAssignedToTrainee(15L);
  }

  @Test
  void getTrainersNotAssignedToTrainee_notFound_throws() {
    when(traineeRepository.findByUsername("bad")).thenReturn(Optional.empty());
    assertThrows(
        NotFoundException.class, () -> traineeService.getTrainersNotAssignedToTrainee("bad"));
  }

  @Test
  void updateTraineeTrainers_success_synchronizesAssociations() {
    Trainee t = Trainee.builder().username("u").id(20L).trainers(new ArrayList<>()).build();
    // existing trainer
    Trainer old = Trainer.builder().id(1L).username("old").trainees(new ArrayList<>()).build();
    old.getTrainees().add(t); // old has trainee
    t.getTrainers().add(old);

    // new trainers returned by repo
    Trainer new1 = Trainer.builder().id(2L).username("n1").trainees(new ArrayList<>()).build();
    Trainer new2 = Trainer.builder().id(3L).username("n2").trainees(new ArrayList<>()).build();

    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            List.of(
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("n1"),
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("n2")));

    TrainerInfoResponse info1 =
        new TrainerInfoResponse("n1", "First", "Name", TrainingType.Type.CARDIO);
    TrainerInfoResponse info2 =
        new TrainerInfoResponse("n2", "Second", "Name", TrainingType.Type.STRENGTH);

    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(trainerRepository.findByUsername("n1")).thenReturn(Optional.of(new1));
    when(trainerRepository.findByUsername("n2")).thenReturn(Optional.of(new2));
    when(trainerRepository.findAllById(List.of(2L, 3L))).thenReturn(List.of(new1, new2));
    when(traineeRepository.save(any())).thenReturn(t);
    when(traineeMapper.trainersToInfoResponseList(any())).thenReturn(List.of(info1, info2));

    List<TrainerInfoResponse> result = traineeService.updateTraineeTrainers("u", request);

    // old trainer should no longer reference trainee
    assertFalse(old.getTrainees().contains(t));
    // new trainers should reference trainee
    assertTrue(new1.getTrainees().contains(t));
    assertTrue(new2.getTrainees().contains(t));
    assertEquals(2, result.size());
    verify(traineeRepository).save(t);
  }

  @Test
  void updateTraineeTrainers_missingTrainerIds_throwsValidation() {
    Trainee t = Trainee.builder().username("u").id(20L).trainers(new ArrayList<>()).build();
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            List.of(
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("n1"),
                new UpdateTraineeTrainersRequest.TrainerUsernameRequest("n2")));

    Trainer new1 = Trainer.builder().id(2L).username("n1").build();

    when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
    when(trainerRepository.findByUsername("n1")).thenReturn(Optional.of(new1));
    when(trainerRepository.findByUsername("n2")).thenReturn(Optional.of(new1));
    // repo returns only one but requested two ids -> mismatch
    when(trainerRepository.findAllById(List.of(2L, 2L))).thenReturn(List.of(new1));

    assertThrows(
        ValidationException.class, () -> traineeService.updateTraineeTrainers("u", request));
  }

  @Test
  void updateTraineeTrainers_traineeNotFound_throws() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            List.of(new UpdateTraineeTrainersRequest.TrainerUsernameRequest("n1")));
    when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
    assertThrows(
        NotFoundException.class, () -> traineeService.updateTraineeTrainers("no", request));
  }
}
