package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import java.util.List;

public interface TrainerService {

    RegistrationResponse createTrainer(TrainerRegistrationRequest request);

    TrainerProfileResponse getByUsername(String username);

    TrainerProfileResponse updateTrainer(String username, UpdateTrainerRequest request);

    void changePassword(String username, String newPassword);

    void setActive(String username, boolean active);

    List<TrainingResponse> getTrainerTrainings(
            String username, TrainerTrainingFilterRequest filter);
}
