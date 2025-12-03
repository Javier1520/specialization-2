package com.epam.gym.service.impl;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.UsernamePasswordGenerator;
import com.epam.gym.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final UsernamePasswordGenerator usernamePasswordGenerator;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final LogUtils logUtils;

    public RegistrationResponse createTrainer(TrainerRegistrationRequest request) {
        logUtils.info(log, "Trainer registration request: firstName={}, lastName={}", request.firstName(), request.lastName());
        Trainer trainer = trainerMapper.toEntity(request);
        prepareTrainer(trainer);
        Trainer saved = trainerRepository.save(trainer);
        logUtils.info(log, "Created trainer username={} id={}", saved.getUsername(), saved.getId());
        return new RegistrationResponse(saved.getUsername(), saved.getPassword());
    }

    private void prepareTrainer(Trainer payload) {
        payload.setUsername(generateUsername(payload));
        payload.setPassword(getGeneratePassword());
        payload.setIsActive(true);
    }

    private String generateUsername(Trainer payload) {
        return usernamePasswordGenerator.generateUsername(
                payload.getFirstName(), payload.getLastName(),
                this::existsByUsername
        );
    }

    private boolean existsByUsername(String candidate) {
        return trainerRepository.existsByUsername(candidate) || traineeRepository.existsByUsername(candidate);
    }

    private String getGeneratePassword() {
        return usernamePasswordGenerator.generatePassword();
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse getByUsername(String username) {
        logUtils.info(log, "Get trainer profile request: username={}", username);
        Trainer trainer = findTrainerByUsernameWithTrainees(username);
        return trainerMapper.toProfileResponse(trainer);
    }

    public void changePassword(String username, String newPassword) {
        validatePasswordLength(newPassword);
        Trainer t = findTrainerByUsername(username);
        t.setPassword(newPassword);
        trainerRepository.save(t);
        logUtils.info(log, "Changed password for trainer {}", username);
    }

    public TrainerProfileResponse updateTrainer(String username, UpdateTrainerRequest request) {
        logUtils.info(log, "Update trainer profile request: username={}", username);
        Trainer existing = findTrainerByUsername(username);
        trainerMapper.updateEntityFromRequest(request, existing);
        trainerRepository.save(existing);
        logUtils.info(log, "Updated trainer {}", username);
        return trainerMapper.toProfileResponse(existing);
    }

    public void setActive(String username, boolean active) {
        logUtils.info(log, "Activate/Deactivate trainer request: username={}, isActive={}", username, active);
        Trainer t = findTrainerByUsername(username);
        validateActiveStatusChange(t.getIsActive(), active);
        t.setIsActive(active);
        trainerRepository.save(t);
        logUtils.info(log, "Set trainer {} active={}", username, active);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainerTrainings(String username, TrainerTrainingFilterRequest filter) {
        logUtils.info(log,
                "Get trainer trainings request: username={}, periodFrom={}, periodTo={}, traineeName={}",
                username, filter.periodFrom(), filter.periodTo(), filter.traineeName());
        List<Training> trainings = trainingRepository.findByTrainerUsernameAndCriteria(
                username,
                filter.periodFrom(),
                filter.periodTo(),
                filter.traineeName()
        );
        return trainingMapper.toResponseList(trainings);
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private Trainer findTrainerByUsernameWithTrainees(String username) {
        return trainerRepository.findByUsernameWithTrainees(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private void validatePasswordLength(String password) {
        Optional.ofNullable(password)
                .filter(pwd -> pwd.length() >= 10)
                .orElseThrow(() -> new ValidationException("Password must be at least 10 chars"));
    }

    private void validateActiveStatusChange(Boolean current, boolean newStatus) {
        Optional.ofNullable(current)
                .filter(c -> Objects.equals(c, newStatus))
                .ifPresent(c -> {
                    throw new ValidationException("Trainer already " + (newStatus ? "active" : "inactive"));
                });
    }
}
