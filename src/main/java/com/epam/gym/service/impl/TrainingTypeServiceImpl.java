package com.epam.gym.service.impl;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository repo;

    @Override
    public List<TrainingType.Type> listAll() {
        return Arrays.stream(TrainingType.Type.values())
                     .toList();
    }

    @Override
    public TrainingType getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Training type not found: " + id));
    }
}
