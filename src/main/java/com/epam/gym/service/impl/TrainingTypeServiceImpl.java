package com.epam.gym.service.impl;

import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private final TrainingTypeRepository repo;

    public List<TrainingType> listAll() {
        return repo.findAll();
    }

    public TrainingType getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new com.epam.gym.exception.NotFoundException("Training type not found: " + id));
    }
}
