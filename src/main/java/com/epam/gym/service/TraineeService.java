package com.epam.gym.service;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;

import java.util.Date;
import java.util.List;

public interface TraineeService {
    Trainee createTrainee(Trainee payload);
    Trainee getByUsername(String username);
    Trainee getByUsernameWithTrainers(String username);
    void changePassword(String username, String newPassword);
    Trainee updateTrainee(String username, Trainee update);
    void setActive(String username, boolean active);
    void deleteByUsername(String username);
    List<Training> getTraineeTrainings(String username, Date from, Date to, String trainerName,
                                       TrainingType.Type trainingType);
    List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername);
    void updateTraineeTrainers(String traineeUsername, List<Long> trainerIds);
}

