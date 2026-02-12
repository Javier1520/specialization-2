package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceStrategyTest {

    @Mock TrainerWorkloadRepository repository;
    @Mock WorkloadMapper mapper;

    @Mock AddWorkloadActionHandler addHandler;
    @Mock DeleteWorkloadActionHandler deleteHandler;

    WorkloadService workloadService;

    @BeforeEach
    void setUp() {
        when(addHandler.getSupportedAction()).thenReturn(ActionType.ADD);
        when(deleteHandler.getSupportedAction()).thenReturn(ActionType.DELETE);

        workloadService =
                new WorkloadService(repository, mapper, List.of(addHandler, deleteHandler));
        workloadService.initHandlers();
    }

    @Test
    void updateWorkload_usesAddHandler() {
        TrainerEntity trainer =
                TrainerEntity.builder()
                        .username("t1")
                        .years(new ArrayList<>())
                        .build();
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.of(trainer));

        WorkloadRequest request =
                new WorkloadRequest(
                        "t1",
                        "F",
                        "L",
                        true,
                        LocalDate.of(2025, 1, 10),
                        60,
                        ActionType.ADD);

        workloadService.updateWorkload(request);

        org.mockito.Mockito.verify(addHandler)
                .handle(any(MonthEntity.class), org.mockito.Mockito.eq(60));
        org.mockito.Mockito.verify(repository).save(trainer);
    }

    @Test
    void updateWorkload_usesDeleteHandler() {
        TrainerEntity trainer =
                TrainerEntity.builder()
                        .username("t1")
                        .years(new ArrayList<>())
                        .build();
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.of(trainer));

        WorkloadRequest request =
                new WorkloadRequest(
                        "t1",
                        "F",
                        "L",
                        true,
                        LocalDate.of(2025, 1, 10),
                        60,
                        ActionType.DELETE);

        workloadService.updateWorkload(request);

        org.mockito.Mockito.verify(deleteHandler)
                .handle(any(MonthEntity.class), org.mockito.Mockito.eq(60));
        org.mockito.Mockito.verify(repository).save(trainer);
    }

    @Test
    void updateWorkload_unsupportedAction_throws() {
        WorkloadRequest badRequest =
                new WorkloadRequest(
                        "t1",
                        "F",
                        "L",
                        true,
                        LocalDate.of(2025, 1, 10),
                        60,
                        null);

        assertThrows(IllegalArgumentException.class, () -> workloadService.updateWorkload(badRequest));
    }

    @Test
    void getWorkload_callsRepositoryAndMapper() {
        TrainerEntity trainer = new TrainerEntity();
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.of(trainer));
        when(mapper.toDto(trainer)).thenReturn(new com.epam.gym.workload.dto.TrainerWorkloadDto("t1", "F", "L", true, List.of()));

        workloadService.getWorkload("t1");

        org.mockito.Mockito.verify(repository).findByUsername("t1");
        org.mockito.Mockito.verify(mapper).toDto(trainer);
    }

    @Test
    void getWorkload_notFound_throws() {
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> workloadService.getWorkload("t1"));
    }

    @Test
    void getTrainingHours_returnsCorrectHours() {
        MonthEntity month = MonthEntity.builder().monthNumber(1).trainingDuration(120).build();
        YearEntity year = YearEntity.builder().yearNumber(2025).months(List.of(month)).build();
        TrainerEntity trainer = TrainerEntity.builder().years(List.of(year)).build();

        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.of(trainer));

        var result = workloadService.getTrainingHours("t1", 2025, 1);

        assertEquals(120L, result.trainingHours());
    }

    @Test
    void getTrainingHours_notFound_throws() {
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> workloadService.getTrainingHours("t1", 2025, 1));
    }

    @Test
    void updateWorkload_createsNewTrainer() {
        when(repository.findByUsername("newTrainer")).thenReturn(java.util.Optional.empty());

        WorkloadRequest request =
                new WorkloadRequest(
                        "newTrainer",
                        "New",
                        "User",
                        true,
                        LocalDate.of(2025, 1, 10),
                        60,
                        ActionType.ADD);

        workloadService.updateWorkload(request);

        org.mockito.Mockito.verify(repository).save(any(TrainerEntity.class));
        org.mockito.Mockito.verify(addHandler).handle(any(MonthEntity.class), org.mockito.Mockito.eq(60));
    }



}

