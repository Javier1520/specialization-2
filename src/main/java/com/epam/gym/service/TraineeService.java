package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;

import java.util.List;

public interface TraineeService {

    RegistrationResponse createTrainee(TraineeRegistrationRequest request);

    TraineeProfileResponse getByUsername(String username);

    TraineeProfileResponse updateTrainee(String username, UpdateTraineeRequest request);

    void changePassword(String username, String newPassword);

    void setActive(String username, boolean active);

    void deleteByUsername(String username);

    List<TrainingResponse> getTraineeTrainings(String username, TrainingFilterRequest filter);

    List<TrainerInfoResponse> getTrainersNotAssignedToTrainee(String traineeUsername);

    List<TrainerInfoResponse> updateTraineeTrainers(String traineeUsername, UpdateTraineeTrainersRequest request);
}

