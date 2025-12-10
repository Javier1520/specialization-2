package com.epam.gym.controller;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.openapi.annotation.operation.CreateOperation;
import com.epam.gym.openapi.annotation.operation.DeleteOperation;
import com.epam.gym.openapi.annotation.operation.GetAllOperation;
import com.epam.gym.openapi.annotation.operation.GetByIdOperation;
import com.epam.gym.openapi.annotation.operation.UpdateOperation;
import com.epam.gym.service.TraineeService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Trainees", description = "Operations in Trainees")
@RestController
@RequestMapping("/api/v1/trainees")
public class TraineeController {
    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final LogUtils logUtils;

    public TraineeController(TraineeService traineeService, LogUtils logUtils) {
        this.traineeService = traineeService;
        this.logUtils = logUtils;
    }

    @CreateOperation(summary = "Create Trainee", description = "Create a new Trainee in Gym CRM")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        logUtils.info(
                log,
                "Trainee registration request: firstName={}, lastName={}",
                request.firstName(),
                request.lastName());
        RegistrationResponse response = traineeService.createTrainee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetByIdOperation(
            summary = "Get Trainee Profile",
            description = "Get Trainee Profile by Username")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        logUtils.info(log, "Get trainee profile request: username={}", username);
        return ResponseEntity.ok(traineeService.getByUsername(username));
    }

    @UpdateOperation(
            summary = "Update Trainee Profile",
            description = "Update Trainee Profile by Username")
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @PathVariable String username, @Valid @RequestBody UpdateTraineeRequest request) {
        logUtils.info(log, "Update trainee profile request: username={}", username);
        return ResponseEntity.ok(traineeService.updateTrainee(username, request));
    }

    @DeleteOperation(
            summary = "Delete Trainee Profile",
            description = "Delete Trainee Profile by Username")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String username) {
        logUtils.info(log, "Delete trainee profile request: username={}", username);
        traineeService.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetAllOperation(
            summary = "Get Trainee Trainings",
            description = "Get Trainee Trainings by Username and Filter")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(@PathVariable String username,
                                                               @ModelAttribute TrainingFilterRequest filter) {
        logUtils.info(
                log,
                "Get trainee trainings request: username={}, periodFrom={}, periodTo={}, trainerName={}, "
                        + "trainingType={}",
                username,
                filter.periodFrom(),
                filter.periodTo(),
                filter.trainerName(),
                filter.trainingType());

        return ResponseEntity.ok(traineeService.getTraineeTrainings(username, filter));
    }

    @GetAllOperation(
            summary = "Get Not Assigned Trainers",
            description = "Get Trainers Not Assigned to Trainee")
    @GetMapping("/{username}/trainers/not-assigned")
    public ResponseEntity<List<TrainerInfoResponse>> getNotAssignedTrainers(@PathVariable String username) {
        logUtils.info(log, "Get not assigned trainers request: traineeUsername={}", username);
        return ResponseEntity.ok(traineeService.getTrainersNotAssignedToTrainee(username));
    }

    @UpdateOperation(
            summary = "Update Trainee Trainers",
            description = "Update Trainee Trainers List")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerInfoResponse>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        logUtils.info(log, "Update trainee trainers request: traineeUsername={}", username);
        return ResponseEntity.ok(traineeService.updateTraineeTrainers(username, request));
    }

    @UpdateOperation(
            summary = "Activate/Deactivate Trainee",
            description = "Activate or Deactivate Trainee Profile")
    @PatchMapping("/{username}/activate")
    public ResponseEntity<Void> activateDeactivate(
            @PathVariable String username, @Valid @RequestBody ActivateDeactivateRequest request) {
        logUtils.info(
                log,
                "Activate/Deactivate trainee request: username={}, isActive={}",
                username,
                request.isActive());
        traineeService.setActive(username, request.isActive());
        return ResponseEntity.ok().build();
    }
}
