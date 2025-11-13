package com.epam.gym.service;

import com.epam.gym.model.TrainingType;

import java.util.List;

public interface TrainingTypeService {
    List<TrainingType> listAll();
    TrainingType getById(Long id);
}

