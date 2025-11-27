package com.epam.gym.controller;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.LogUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Slf4j
public class TrainerController {
    private final TrainerService trainerService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final LogUtils logUtils;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        logUtils.info(log, "Trainer registration request: firstName={}, lastName={}", request.firstName(), request.lastName());
        Trainer trainer = trainerMapper.toEntity(request);
        Trainer created = trainerService.createTrainer(trainer);
        RegistrationResponse response = new RegistrationResponse(created.getUsername(), created.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        logUtils.info(log, "Get trainer profile request: username={}", username);
        Trainer trainer = trainerService.getByUsernameWithTrainees(username);
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateProfile(@PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request) {
        logUtils.info(log, "Update trainer profile request: username={}", username);
        Trainer existing = trainerService.getByUsername(username);
        trainerMapper.updateEntityFromRequest(request, existing);
        Trainer updated = trainerService.updateTrainer(username, existing);
        TrainerProfileResponse response = trainerMapper.toProfileResponse(updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String traineeName) {

        logUtils.info(log, "Get trainer trainings request: username={}, periodFrom={}, periodTo={}, traineeName={}",
                username, periodFrom, periodTo, traineeName);

        if (periodFrom != null && periodTo != null && periodFrom.after(periodTo)) {
            throw new ValidationException("periodFrom cannot be after periodTo");
        }

        List<Training> trainings = trainerService.getTrainerTrainings(username, periodFrom, periodTo, traineeName);
        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    public ResponseEntity<Void> activateDeactivate(@PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        logUtils.info(log, "Activate/Deactivate trainer request: username={}, isActive={}", username, request.isActive());
        trainerService.setActive(username, request.isActive());
        return ResponseEntity.ok().build();
    }
}


