package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.DeleteTrainingRequest;

public interface TrainingService {

    void addTraining(AddTrainingRequest request);

    void deleteTraining(DeleteTrainingRequest request);
}
