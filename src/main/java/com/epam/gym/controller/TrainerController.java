package com.epam.gym.controller;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.openapi.annotation.operation.CreateOperation;
import com.epam.gym.openapi.annotation.operation.GetAllOperation;
import com.epam.gym.openapi.annotation.operation.GetByIdOperation;
import com.epam.gym.openapi.annotation.operation.UpdateOperation;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Trainers", description = "Operations in Trainers")
@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {
  private static final Logger log = LoggerFactory.getLogger(TrainerController.class);
  private final TrainerService trainerService;
  private final LogUtils logUtils;

  public TrainerController(TrainerService trainerService, LogUtils logUtils) {
    this.trainerService = trainerService;
    this.logUtils = logUtils;
  }

  @CreateOperation(summary = "Create Trainer", description = "Create a new Trainer in Gym CRM")
  @PostMapping("/register")
  public ResponseEntity<RegistrationResponse> register(
      @Valid @RequestBody TrainerRegistrationRequest request) {
    logUtils.info(
        log,
        "Trainer registration request: firstName={}, lastName={}",
        request.firstName(),
        request.lastName());
    RegistrationResponse response = trainerService.createTrainer(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetByIdOperation(
      summary = "Get Trainer Profile",
      description = "Get Trainer Profile by Username")
  @GetMapping("/{username}")
  public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
    logUtils.info(log, "Get trainer profile request: username={}", username);
    return ResponseEntity.ok(trainerService.getByUsername(username));
  }

  @UpdateOperation(
      summary = "Update Trainer Profile",
      description = "Update Trainer Profile by Username")
  @PutMapping("/{username}")
  public ResponseEntity<TrainerProfileResponse> updateProfile(
      @PathVariable String username, @Valid @RequestBody UpdateTrainerRequest request) {
    logUtils.info(log, "Update trainer profile request: username={}", username);
    return ResponseEntity.ok(trainerService.updateTrainer(username, request));
  }

  @GetAllOperation(
      summary = "Get Trainer Trainings",
      description = "Get Trainer Trainings by Username and Filter")
  @GetMapping("/{username}/trainings")
  public ResponseEntity<List<TrainingResponse>> getTrainings(
      @PathVariable String username, @Valid @ModelAttribute TrainerTrainingFilterRequest filter) {

    logUtils.info(
        log,
        "Get trainer trainings request: username={}, periodFrom={}, periodTo={}, traineeName={}",
        username,
        filter.periodFrom(),
        filter.periodTo(),
        filter.traineeName());

    return ResponseEntity.ok(trainerService.getTrainerTrainings(username, filter));
  }

  @UpdateOperation(
      summary = "Activate/Deactivate Trainer",
      description = "Activate or Deactivate Trainer Profile")
  @PatchMapping("/{username}/activate")
  public ResponseEntity<Void> activateDeactivate(
      @PathVariable String username, @Valid @RequestBody ActivateDeactivateRequest request) {
    logUtils.info(
        log,
        "Activate/Deactivate trainer request: username={}, isActive={}",
        username,
        request.isActive());
    trainerService.setActive(username, request.isActive());
    return ResponseEntity.ok().build();
  }
}
