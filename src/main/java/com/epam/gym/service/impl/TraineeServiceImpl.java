package com.epam.gym.service.impl;

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
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.UsernamePasswordGenerator;
import com.epam.gym.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class TraineeServiceImpl implements TraineeService {
  private static final int PASSWORD_LENGTH = 10;
  private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final TrainingRepository trainingRepository;
  private final UsernamePasswordGenerator usernamePasswordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final TraineeMapper traineeMapper;
  private final TrainingMapper trainingMapper;
  private final LogUtils logUtils;

  public TraineeServiceImpl(
      TraineeRepository traineeRepository,
      TrainerRepository trainerRepository,
      TrainingRepository trainingRepository,
      UsernamePasswordGenerator usernamePasswordGenerator,
      PasswordEncoder passwordEncoder,
      TraineeMapper traineeMapper,
      TrainingMapper trainingMapper,
      LogUtils logUtils) {
    this.traineeRepository = traineeRepository;
    this.trainerRepository = trainerRepository;
    this.trainingRepository = trainingRepository;
    this.usernamePasswordGenerator = usernamePasswordGenerator;
    this.passwordEncoder = passwordEncoder;
    this.traineeMapper = traineeMapper;
    this.trainingMapper = trainingMapper;
    this.logUtils = logUtils;
  }

  public RegistrationResponse createTrainee(TraineeRegistrationRequest request) {
    logUtils.info(
        log,
        "Trainee registration request: firstName={}, lastName={}",
        request.firstName(),
        request.lastName());
    Trainee trainee = traineeMapper.toEntity(request);
    String generatedPassword = getGeneratePassword();
    prepareTrainee(trainee, generatedPassword);
    Trainee saved = traineeRepository.save(trainee);
    logUtils.info(log, "Created trainee username={} id={}", saved.getUsername(), saved.getId());
    return new RegistrationResponse(saved.getUsername(), generatedPassword);
  }

  private String getGeneratePassword() {
    return usernamePasswordGenerator.generatePassword();
  }

  private void prepareTrainee(Trainee payload, String plainPassword) {
    payload.setUsername(generateUsername(payload));
    payload.setPassword(passwordEncoder.encode(plainPassword));
    payload.setIsActive(true);
  }

  private String generateUsername(Trainee payload) {
    return usernamePasswordGenerator.generateUsername(
        payload.getFirstName(), payload.getLastName(), this::existsByUsername);
  }

  private boolean existsByUsername(String candidate) {
    return traineeRepository.existsByUsername(candidate)
        || trainerRepository.existsByUsername(candidate);
  }

  @Transactional(readOnly = true)
  public TraineeProfileResponse getByUsername(String username) {
    logUtils.info(log, "Get trainee profile request: username={}", username);
    Trainee trainee = findTraineeByUsernameWithTrainers(username);
    return traineeMapper.toProfileResponse(trainee);
  }

  public void changePassword(String username, String newPassword) {
    validatePasswordLength(newPassword);
    Trainee trainee = findTraineeByUsername(username);
    trainee.setPassword(passwordEncoder.encode(newPassword));
    traineeRepository.save(trainee);
    logUtils.info(log, "Changed password for trainee {}", username);
  }

  private Trainee findTraineeByUsernameWithTrainers(String username) {
    return traineeRepository
        .findByUsernameWithTrainers(username)
        .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
  }

  public TraineeProfileResponse updateTrainee(String username, UpdateTraineeRequest request) {
    logUtils.info(log, "Update trainee profile request: username={}", username);
    Trainee existing = findTraineeByUsername(username);
    traineeMapper.updateEntityFromRequest(request, existing);
    traineeRepository.save(existing);
    logUtils.info(log, "Updated trainee {}", username);
    return traineeMapper.toProfileResponse(existing);
  }

  public void setActive(String username, boolean active) {
    logUtils.info(
        log, "Activate/Deactivate trainee request: username={}, isActive={}", username, active);
    Trainee trainee = findTraineeByUsername(username);
    validateActiveStatusChange(trainee.getIsActive(), active);
    trainee.setIsActive(active);
    traineeRepository.save(trainee);
    logUtils.info(log, "Set trainee {} active={}", username, active);
  }

  public void deleteByUsername(String username) {
    logUtils.info(log, "Delete trainee profile request: username={}", username);
    Trainee t = findTraineeByUsername(username);
    removeTraineeFromTrainers(t);
    traineeRepository.deleteByUsername(username);
    logUtils.info(log, "Deleted trainee {}", username);
  }

  @Transactional(readOnly = true)
  public List<TrainingResponse> getTraineeTrainings(String username, TrainingFilterRequest filter) {
    logUtils.info(
        log,
        "Get trainee trainings request: username={}, periodFrom={}, periodTo={}, trainerName={}, "
            + "trainingType={}",
        username,
        filter.periodFrom(),
        filter.periodTo(),
        filter.trainerName(),
        filter.trainingType());
    List<Training> trainings =
        trainingRepository.findByTraineeUsernameWithOptionalFilters(
            username,
            filter.periodFrom(),
            filter.periodTo(),
            filter.trainerName(),
            filter.trainingType());
    return trainingMapper.toResponseList(trainings);
  }

  @Transactional(readOnly = true)
  public List<TrainerInfoResponse> getTrainersNotAssignedToTrainee(String traineeUsername) {
    logUtils.info(log, "Get not assigned trainers request: traineeUsername={}", traineeUsername);
    Trainee t = findTraineeByUsername(traineeUsername);
    List<Trainer> trainers = trainerRepository.findNotAssignedToTrainee(t.getId());
    return trainers.stream().map(traineeMapper::trainerToInfoResponse).toList();
  }

  public List<TrainerInfoResponse> updateTraineeTrainers(
      String traineeUsername, UpdateTraineeTrainersRequest request) {
    logUtils.info(log, "Update trainee trainers request: traineeUsername={}", traineeUsername);
    Trainee t = findTraineeByUsername(traineeUsername);

    List<Long> trainerIds =
        request.trainers().stream()
            .map(
                tr -> {
                  Trainer trainer =
                      trainerRepository
                          .findByUsername(tr.trainerUsername())
                          .orElseThrow(
                              () ->
                                  new NotFoundException(
                                      "Trainer not found: " + tr.trainerUsername()));
                  return trainer.getId();
                })
            .toList();

    List<Trainer> trainers = trainerRepository.findAllById(trainerIds);
    validateTrainerIds(trainerIds, trainers);
    updateTrainerRelationships(t, trainers);
    traineeRepository.save(t);
    logUtils.info(log, "Updated trainers for trainee {} to {}", traineeUsername, trainerIds);

    return traineeMapper.trainersToInfoResponseList(t.getTrainers());
  }

  private Trainee findTraineeByUsername(String username) {
    return traineeRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
  }

  private void validatePasswordLength(String password) {
    Optional.ofNullable(password)
        .filter(pwd -> pwd.length() == PASSWORD_LENGTH)
        .orElseThrow(() -> new ValidationException("Password must be at least 10 characters"));
  }

  private void validateActiveStatusChange(Boolean current, boolean newStatus) {
    Optional.of(current)
        .filter(currentActive -> Objects.equals(currentActive, newStatus))
        .ifPresent(
            currentActive -> {
              throw new ValidationException(
                  "Trainee already " + (newStatus ? "active" : "inactive"));
            });
  }

  private void removeTraineeFromTrainers(Trainee trainee) {
    Optional.ofNullable(trainee.getTrainers())
        .ifPresent(
            trainers -> {
              trainers.forEach(trainer -> trainer.getTrainees().remove(trainee));
              trainerRepository.saveAll(trainers);
            });
  }

  private void validateTrainerIds(List<Long> trainerIds, List<Trainer> trainers) {
    if (allTrainersFound(trainers, trainerIds)) {
      return;
    }
    throw new ValidationException("Some trainer ids not found");
  }

  private boolean allTrainersFound(List<Trainer> trainers, List<Long> trainerIds) {
    return trainers.size() == trainerIds.size();
  }

  private void updateTrainerRelationships(Trainee trainee, List<Trainer> trainers) {
    clearOldTrainerRelationships(trainee);
    addNewTrainerRelationships(trainee, trainers);
  }

  private void clearOldTrainerRelationships(Trainee trainee) {
    trainee.getTrainers().forEach(oldTrainer -> oldTrainer.getTrainees().remove(trainee));
    trainee.getTrainers().clear();
  }

  private void addNewTrainerRelationships(Trainee trainee, List<Trainer> trainers) {
    for (Trainer trainer : trainers) {
      trainee.getTrainers().add(trainer);
      trainer.getTrainees().add(trainee);
    }
  }
}
