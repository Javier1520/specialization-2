package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.dto.WorkloadRequest;
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
    private final AddWorkloadActionHandler addHandler;
    private final DeleteWorkloadActionHandler deleteHandler;

    @Transactional
    public void addWorkload(WorkloadRequest request) {
        log.info("Adding workload for trainer: {}", request.username());
        MonthEntity monthEntity = resolveMonthEntity(request);
        addHandler.handle(monthEntity, request.trainingDuration());
        repository.save(resolveTrainer(request));
        log.info("Workload added successfully for trainer: {}", request.username());
    }

    @Transactional
    public void deleteWorkload(WorkloadRequest request) {
        log.info("Deleting workload for trainer: {}", request.username());
        MonthEntity monthEntity = resolveMonthEntity(request);
        deleteHandler.handle(monthEntity, request.trainingDuration());
        repository.save(resolveTrainer(request));
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

    private TrainerEntity resolveTrainer(WorkloadRequest request) {
        TrainerEntity trainer =
                repository
                        .findByUsername(request.username())
                        .orElseGet(() -> createTrainer(request));

        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setActive(request.isActive());

        return trainer;
    }

    private MonthEntity resolveMonthEntity(WorkloadRequest request) {
        TrainerEntity trainer = resolveTrainer(request);

        LocalDate date = request.trainingDate();
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

    private TrainerEntity createTrainer(WorkloadRequest request) {
        return TrainerEntity.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .isActive(request.isActive())
                .years(new ArrayList<>())
                .build();
    }
}
