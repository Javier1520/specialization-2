package com.epam.gym.facade;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymFacade {

  private final TraineeService traineeService;
  private final TrainerService trainerService;
  private final TrainingService trainingService;
  private final TrainingTypeService trainingTypeService;

  // --- Trainee operations ---
  public RegistrationResponse createTrainee(TraineeRegistrationRequest request) {
    return traineeService.createTrainee(request);
  }

  public TraineeProfileResponse updateTrainee(String username, UpdateTraineeRequest request) {
    return traineeService.updateTrainee(username, request);
  }

  public void changeTraineePassword(String username, String newPassword) {
    traineeService.changePassword(username, newPassword);
  }

  public TraineeProfileResponse getTraineeByUsername(String username) {
    return traineeService.getByUsername(username);
  }

  public void setTraineeActive(String username, boolean active) {
    traineeService.setActive(username, active);
  }

  public void deleteTraineeByUsername(String username) {
    traineeService.deleteByUsername(username);
  }

  public List<TrainingResponse> getTraineeTrainings(String username, TrainingFilterRequest filter) {
    return traineeService.getTraineeTrainings(username, filter);
  }

  public List<TrainerInfoResponse> getTrainersNotAssignedToTrainee(String traineeUsername) {
    return traineeService.getTrainersNotAssignedToTrainee(traineeUsername);
  }

  public List<TrainerInfoResponse> updateTraineeTrainers(
      String traineeUsername, UpdateTraineeTrainersRequest request) {
    return traineeService.updateTraineeTrainers(traineeUsername, request);
  }

  // --- Trainer operations ---
  public RegistrationResponse createTrainer(TrainerRegistrationRequest request) {
    return trainerService.createTrainer(request);
  }

  public TrainerProfileResponse updateTrainer(String username, UpdateTrainerRequest request) {
    return trainerService.updateTrainer(username, request);
  }

  public void changeTrainerPassword(String username, String newPassword) {
    trainerService.changePassword(username, newPassword);
  }

  public TrainerProfileResponse getTrainerByUsername(String username) {
    return trainerService.getByUsername(username);
  }

  public void setTrainerActive(String username, boolean active) {
    trainerService.setActive(username, active);
  }

  public List<TrainingResponse> getTrainerTrainings(
      String username, TrainerTrainingFilterRequest filter) {
    return trainerService.getTrainerTrainings(username, filter);
  }

  // --- Training operations ---
  public void createTraining(AddTrainingRequest request) {
    trainingService.addTraining(request);
  }

  public List<TrainingTypeResponse> listTrainingTypes() {
    return trainingTypeService.listAll();
  }
}
