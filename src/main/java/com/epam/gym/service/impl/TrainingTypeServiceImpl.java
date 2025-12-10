package com.epam.gym.service.impl;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.service.TrainingTypeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository repo;
    private final TrainingTypeMapper mapper;

    public TrainingTypeServiceImpl(TrainingTypeRepository repo, TrainingTypeMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<TrainingTypeResponse> listAll() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public TrainingTypeResponse getById(Long id) {
        TrainingType trainingType =
                repo.findById(id)
                        .orElseThrow(() -> new NotFoundException("Training type not found: " + id));
        return mapper.toResponse(trainingType);
    }
}
