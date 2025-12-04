package com.epam.gym.service;

import com.epam.gym.dto.response.TrainingTypeResponse;

import java.util.List;

public interface TrainingTypeService {

    List<TrainingTypeResponse> listAll();

    TrainingTypeResponse getById(Long id);
}

