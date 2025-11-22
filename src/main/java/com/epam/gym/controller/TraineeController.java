package com.epam.gym.controller;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.gym.dto.request.ActivateDeactivateRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.TraineeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Slf4j
public class TraineeController {
    private final TraineeService traineeService;
    private final TraineeMapper traineeMapper;
    private final TrainingMapper trainingMapper;
    private final TrainerRepository trainerRepository;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        log.info("Trainee registration request: firstName={}, lastName={}", request.getFirstName(), request.getLastName());
        Trainee trainee = traineeMapper.toEntity(request);
        Trainee created = traineeService.createTrainee(trainee);
        RegistrationResponse response = RegistrationResponse.builder()
                .username(created.getUsername())
                .password(created.getPassword())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        log.info("Get trainee profile request: username={}", username);
        Trainee trainee = traineeService.getByUsernameWithTrainers(username);
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        log.info("Update trainee profile request: username={}", username);
        Trainee existing = traineeService.getByUsername(username);
        traineeMapper.updateEntityFromRequest(request, existing);
        Trainee updated = traineeService.updateTrainee(username, existing);
        TraineeProfileResponse response = traineeMapper.toProfileResponse(updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String username) {
        log.info("Delete trainee profile request: username={}", username);
        traineeService.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingType.Type trainingType) {
        log.info("Get trainee trainings request: username={}, periodFrom={}, periodTo={}, trainerName={}, trainingType={}",
                username, periodFrom, periodTo, trainerName, trainingType);


        List<Training> trainings = traineeService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);
        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainers/not-assigned")
    public ResponseEntity<List<TrainerInfoResponse>> getNotAssignedTrainers(@PathVariable String username) {
        log.info("Get not assigned trainers request: traineeUsername={}", username);
        List<com.epam.gym.model.Trainer> trainers = traineeService.getTrainersNotAssignedToTrainee(username);
        List<TrainerInfoResponse> response = trainers.stream()
                .map(traineeMapper::trainerToInfoResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerInfoResponse>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        log.info("Update trainee trainers request: traineeUsername={}", username);
        List<Long> trainerIds = request.getTrainers().stream()
                .map(t -> {
                    Trainer trainer = trainerRepository.findByUsername(t.getTrainerUsername())
                            .orElseThrow(() -> new NotFoundException("Trainer not found: " + t.getTrainerUsername()));
                    return trainer.getId();
                })
                .toList();

        traineeService.updateTraineeTrainers(username, trainerIds);
        Trainee trainee = traineeService.getByUsernameWithTrainers(username);
        List<TrainerInfoResponse> response = traineeMapper.trainersToInfoResponseList(trainee.getTrainers());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    public ResponseEntity<Void> activateDeactivate(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        log.info("Activate/Deactivate trainee request: username={}, isActive={}", username, request.getIsActive());
        traineeService.setActive(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }
}

