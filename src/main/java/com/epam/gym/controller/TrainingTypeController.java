package com.epam.gym.controller;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.openapi.annotation.operation.GetAllOperation;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Training Types", description = "Operations in Training Types")
@RestController
@RequestMapping("/api/v1/training-types")
public class TrainingTypeController {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);
    private final TrainingTypeService trainingTypeService;
    private final LogUtils logUtils;

    public TrainingTypeController(TrainingTypeService trainingTypeService, LogUtils logUtils) {
        this.trainingTypeService = trainingTypeService;
        this.logUtils = logUtils;
    }

    @GetAllOperation(summary = "Get Training Types", description = "Get All Training Types")
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        logUtils.info(log, "Get training types request");
        return ResponseEntity.ok(trainingTypeService.listAll());
    }
}
