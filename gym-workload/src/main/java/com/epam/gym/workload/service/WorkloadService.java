package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadRepository repository;

    @Transactional
    public void updateWorkload(WorkloadRequest request) {
        log.info("Updating workload for trainer: {}", request.getUsername());

        TrainerEntity trainer = repository.findByUsername(request.getUsername())
                .orElseGet(() -> createTrainer(request));

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setActive(request.isActive()); // Update status if changed

        LocalDate date = request.getTrainingDate();
        int yearNum = date.getYear();
        int monthNum = date.getMonthValue();
        int duration = request.getTrainingDuration();

        YearEntity yearEntity = trainer.getYears().stream()
                .filter(y -> y.getYearNumber() == yearNum)
                .findFirst()
                .orElseGet(() -> {
                    YearEntity newYear = YearEntity.builder()
                            .yearNumber(yearNum)
                            .trainer(trainer)
                            .months(new java.util.ArrayList<>())
                            .build();
                    trainer.getYears().add(newYear);
                    return newYear;
                });

        MonthEntity monthEntity = yearEntity.getMonths().stream()
                .filter(m -> m.getMonthNumber() == monthNum)
                .findFirst()
                .orElseGet(() -> {
                    MonthEntity newMonth = MonthEntity.builder()
                            .monthNumber(monthNum)
                            .year(yearEntity)
                            .trainingDuration(0)
                            .build();
                    yearEntity.getMonths().add(newMonth);
                    return newMonth;
                });

        if (request.getActionType() == ActionType.ADD) {
            monthEntity.setTrainingDuration(monthEntity.getTrainingDuration() + duration);
        } else if (request.getActionType() == ActionType.DELETE) {
            long newDuration = monthEntity.getTrainingDuration() - duration;
            if (newDuration < 0) newDuration = 0; // Prevent negative duration
            monthEntity.setTrainingDuration(newDuration);
        }

        repository.save(trainer);
        log.info("Workload updated successfully for trainer: {}", request.getUsername());
    }

    public TrainerEntity getWorkload(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + username));
    }

    private TrainerEntity createTrainer(WorkloadRequest request) {
        return TrainerEntity.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.isActive())
                .years(new java.util.ArrayList<>())
                .build();
    }
}
