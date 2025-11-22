package com.epam.gym.service;

import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;

import java.util.Date;
import java.util.List;

public interface TrainerService {
    Trainer createTrainer(Trainer payload);
    Trainer getByUsername(String username);
    Trainer getByUsernameWithTrainees(String username);
    void changePassword(String username, String newPassword);
    Trainer updateTrainer(String username, Trainer update);
    void setActive(String username, boolean active);
    List<Training> getTrainerTrainings(String username, Date from, Date to, String traineeName);
}

