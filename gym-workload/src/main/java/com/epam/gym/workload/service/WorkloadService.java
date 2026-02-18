package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadRepository repository;
    private final WorkloadMapper mapper;
    private final List<WorkloadActionHandler> handlers;
    private final Map<ActionType, WorkloadActionHandler> handlerMap = new EnumMap<>(ActionType.class);

    @PostConstruct
    void initHandlers() {
        log.debug("Initializing handlers: {}", handlers);
        handlers.forEach(
                handler -> {
                    if (handler == null || handler.getSupportedAction() == null) {
                        log.error(
                                "Handler is null or returned null for getSupportedAction: {}",
                                handler);
                        throw new IllegalStateException(
                                "Handler is null or returned null for getSupportedAction");
                    }
                    handlerMap.put(handler.getSupportedAction(), handler);
                });
    }

    @Transactional
    public void updateWorkload(WorkloadRequest request) {
        // try{
        //     Thread.sleep(5500);
        // } catch (InterruptedException e) {
        //     throw new RuntimeException(e);
        // }
        log.info("Updating workload for trainer: {}", request.username());

        TrainerEntity trainer =
                repository
                        .findByUsername(request.username())
                        .orElseGet(() -> createTrainer(request));

        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setActive(request.isActive()); // Update status if changed

        LocalDate date = request.trainingDate();
        int yearNum = date.getYear();
        int monthNum = date.getMonthValue();
        int duration = request.trainingDuration();

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

        MonthEntity monthEntity =
                yearEntity.getMonths().stream()
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

        WorkloadActionHandler handler = handlerMap.get(request.actionType());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported action type: " + request.actionType());
        }
        handler.handle(monthEntity, duration);

        repository.save(trainer);
        log.info("Workload updated successfully for trainer: {}", request.username());
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
