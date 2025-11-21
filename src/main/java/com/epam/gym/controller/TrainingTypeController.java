package com.epam.gym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.service.TrainingTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/training-types")
@RequiredArgsConstructor
@Slf4j
public class TrainingTypeController {
    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeMapper trainingTypeMapper;

    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        log.info("Get training types request");
        List<TrainingTypeResponse> response = trainingTypeMapper.toResponseList(trainingTypeService.listAll());
        return ResponseEntity.ok(response);
    }
}


