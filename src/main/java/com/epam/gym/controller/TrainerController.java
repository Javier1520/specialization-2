package com.epam.gym.controller;

import com.epam.gym.dto.request.*;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Slf4j
public class TrainerController {
    private final TrainerService trainerService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Trainer registration request: firstName={}, lastName={}", request.getFirstName(), request.getLastName());
        Trainer trainer = trainerMapper.toEntity(request);
        Trainer created = trainerService.createTrainer(trainer);
        RegistrationResponse response = RegistrationResponse.builder()
                .username(created.getUsername())
                .password(created.getPassword())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        log.info("Get trainer profile request: username={}", username);
        Trainer trainer = trainerService.getByUsername(username);
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request) {
        log.info("Update trainer profile request: username={}", username);
        Trainer existing = trainerService.getByUsername(username);
        trainerMapper.updateEntityFromRequest(request, existing);
        existing.setIsActive(request.getIsActive());
        Trainer updated = trainerService.updateTrainer(username, existing);
        TrainerProfileResponse response = trainerMapper.toProfileResponse(updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        log.info("Get trainer trainings request: username={}, periodFrom={}, periodTo={}, traineeName={}",
                username, periodFrom, periodTo, traineeName);

        LocalDate from = periodFrom != null ? periodFrom : LocalDate.now().minusYears(1);
        LocalDate to = periodTo != null ? periodTo : LocalDate.now().plusYears(1);

        List<Training> trainings = trainerService.getTrainerTrainings(username, from, to, traineeName);
        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    public ResponseEntity<Void> activateDeactivate(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        log.info("Activate/Deactivate trainer request: username={}, isActive={}", username, request.getIsActive());
        trainerService.setActive(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }
}


