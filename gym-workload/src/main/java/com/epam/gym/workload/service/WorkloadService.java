package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadRepository repository;
    private final WorkloadMapper mapper;

    @Transactional
    public void addWorkload(AddWorkloadRequest request) {
        log.info("Adding workload for trainer: {}", request.username());
        TrainerEntity trainer = resolveTrainer(request.username(), request.firstName(), request.lastName(), request.isActive());
        MonthEntity monthEntity = resolveMonthEntity(trainer, request.trainingDate());

        int duration = request.trainingDuration();
        long currentDuration = monthEntity.getTrainingDuration();
        monthEntity.setTrainingDuration(currentDuration + duration);

        repository.save(trainer);
        log.info("Workload added successfully for trainer: {}", request.username());
    }

    @Transactional
    public void deleteWorkload(DeleteWorkloadRequest request) {
        log.info("Deleting workload for trainer: {}", request.username());
        TrainerEntity trainer = resolveTrainer(request.username(), request.firstName(), request.lastName(), request.isActive());
        MonthEntity monthEntity = resolveMonthEntity(trainer, request.trainingDate());

        int duration = request.trainingDuration();
        long currentDuration = monthEntity.getTrainingDuration();
        long newDuration = currentDuration - duration;
        if (newDuration < 0) {
            newDuration = 0;
        }
        monthEntity.setTrainingDuration(newDuration);

        repository.save(trainer);
        log.info("Workload deleted successfully for trainer: {}", request.username());
    }

    public TrainerWorkloadDto getWorkload(String username) {
        TrainerEntity trainer =
                repository
                        .findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));
        return mapper.toDto(trainer);
    }

    public TrainingHoursDto getTrainingHours(String username, Integer year, Integer month) {
        TrainerEntity trainer =
                repository
                        .findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));

        long hours =
                trainer.getYears().stream()
                        .filter(y -> y.getYearNumber() == year)
                        .findFirst()
                        .flatMap(
                                yearEntity ->
                                        yearEntity.getMonths().stream()
                                                .filter(m -> m.getMonthNumber() == month)
                                                .findFirst()
                                                .map(MonthEntity::getTrainingDuration))
                        .orElse(0L);

        return new TrainingHoursDto(username, year, month, hours);
    }

    private TrainerEntity resolveTrainer(String username, String firstName, String lastName, Boolean isActive) {
        TrainerEntity trainer =
                repository
                        .findByUsername(username)
                        .orElseGet(() -> createTrainer(username, firstName, lastName, isActive));

        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setActive(isActive);

        return trainer;
    }

    private MonthEntity resolveMonthEntity(TrainerEntity trainer, LocalDate date) {
        int yearNum = date.getYear();
        int monthNum = date.getMonthValue();

        YearEntity yearEntity =
                trainer.getYears().stream()
                        .filter(y -> y.getYearNumber() == yearNum)
                        .findFirst()
                        .orElseGet(
                                () -> {
                                    YearEntity newYear =
                                            YearEntity.builder()
                                                    .yearNumber(yearNum)
                                                    .trainer(trainer)
                                                    .months(new ArrayList<>())
                                                    .build();
                                    trainer.getYears().add(newYear);
                                    return newYear;
                                });

        return yearEntity.getMonths().stream()
                .filter(m -> m.getMonthNumber() == monthNum)
                .findFirst()
                .orElseGet(
                        () -> {
                            MonthEntity newMonth =
                                    MonthEntity.builder()
                                            .monthNumber(monthNum)
                                            .year(yearEntity)
                                            .trainingDuration(0)
                                            .build();
                            yearEntity.getMonths().add(newMonth);
                            return newMonth;
                        });
    }

    private TrainerEntity createTrainer(String username, String firstName, String lastName, Boolean isActive) {
        return TrainerEntity.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(isActive)
                .years(new ArrayList<>())
                .build();
    }
}
