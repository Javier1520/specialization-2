package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.entity.TrainerWorkload;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadMongoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadMongoRepository repository;
    private final WorkloadMapper mapper;

    public void addWorkload(AddWorkloadRequest request) {
        log.info("Adding workload for trainer: {}", request.username());
        TrainerWorkload trainer = resolveTrainer(request.username(), request.firstName(), request.lastName(), request.isActive());
        TrainerWorkload.MonthSummary monthSummary = resolveMonthSummary(trainer, request.trainingDate());

        long currentDuration = monthSummary.getTrainingDuration() != null ? monthSummary.getTrainingDuration() : 0;
        monthSummary.setTrainingDuration(currentDuration + request.trainingDuration());

        repository.save(trainer);
        log.info("Workload added successfully for trainer: {}", request.username());
    }

    public void deleteWorkload(DeleteWorkloadRequest request) {
        log.info("Deleting workload for trainer: {}", request.username());
        TrainerWorkload trainer = resolveTrainer(request.username(), request.firstName(), request.lastName(), request.isActive());
        TrainerWorkload.MonthSummary monthSummary = resolveMonthSummary(trainer, request.trainingDate());

        long currentDuration = monthSummary.getTrainingDuration() != null ? monthSummary.getTrainingDuration() : 0;
        long newDuration = currentDuration - request.trainingDuration();
        if (newDuration < 0) {
            newDuration = 0;
        }
        monthSummary.setTrainingDuration(newDuration);

        repository.save(trainer);
        log.info("Workload deleted successfully for trainer: {}", request.username());
    }

    public TrainerWorkloadDto getWorkload(String username) {
        TrainerWorkload trainer =
                repository
                        .findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));
        return mapper.toDto(trainer);
    }

    public TrainingHoursDto getTrainingHours(String username, Integer year, Integer month) {
        TrainerWorkload trainer =
                repository
                        .findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));

        long hours =
                trainer.getYears().stream()
                        .filter(y -> y.getYearNumber().equals(year))
                        .findFirst()
                        .flatMap(
                                yearSummary ->
                                        yearSummary.getMonths().stream()
                                                .filter(m -> m.getMonthNumber().equals(month))
                                                .findFirst()
                                                .map(TrainerWorkload.MonthSummary::getTrainingDuration))
                        .orElse(0L);

        return new TrainingHoursDto(username, year, month, hours);
    }

    private TrainerWorkload resolveTrainer(String username, String firstName, String lastName, Boolean isActive) {
        TrainerWorkload trainer =
                repository
                        .findByUsername(username)
                        .orElseGet(() -> createTrainer(username, firstName, lastName, isActive));

        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setIsActive(isActive);

        return trainer;
    }

    private TrainerWorkload.MonthSummary resolveMonthSummary(TrainerWorkload trainer, LocalDate date) {
        int yearNum = date.getYear();
        int monthNum = date.getMonthValue();

        TrainerWorkload.YearSummary yearSummary =
                trainer.getYears().stream()
                        .filter(y -> y.getYearNumber() == yearNum)
                        .findFirst()
                        .orElseGet(
                                () -> {
                                    TrainerWorkload.YearSummary newYear =
                                            TrainerWorkload.YearSummary.builder()
                                                    .yearNumber(yearNum)
                                                    .months(new ArrayList<>())
                                                    .build();
                                    trainer.getYears().add(newYear);
                                    return newYear;
                                });

        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonthNumber() == monthNum)
                .findFirst()
                .orElseGet(
                        () -> {
                            TrainerWorkload.MonthSummary newMonth =
                                    TrainerWorkload.MonthSummary.builder()
                                            .monthNumber(monthNum)
                                            .trainingDuration(0L)
                                            .build();
                            yearSummary.getMonths().add(newMonth);
                            return newMonth;
                        });
    }

    private TrainerWorkload createTrainer(String username, String firstName, String lastName, Boolean isActive) {
        return TrainerWorkload.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(isActive)
                .years(new ArrayList<>())
                .build();
    }
}
