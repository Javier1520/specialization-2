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
import org.mockito.InjectMocks;
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
        workloadService =
                new WorkloadService(repository, mapper, List.of(addHandler, deleteHandler));
        workloadService.initHandlers();
    }

    @Test
    void updateWorkload_usesAddHandler() {
        when(addHandler.getSupportedAction()).thenReturn(ActionType.ADD);
        when(deleteHandler.getSupportedAction()).thenReturn(ActionType.DELETE);

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

        // verify handler invoked by checking that handler.handle was called with some MonthEntity
        org.mockito.Mockito.verify(addHandler)
                .handle(any(MonthEntity.class), org.mockito.Mockito.eq(60));
    }

    @Test
    void updateWorkload_unsupportedAction_throws() {
        when(addHandler.getSupportedAction()).thenReturn(ActionType.ADD);
        when(deleteHandler.getSupportedAction()).thenReturn(ActionType.DELETE);

        TrainerEntity trainer =
                TrainerEntity.builder()
                        .username("t1")
                        .years(new ArrayList<>())
                        .build();
        when(repository.findByUsername("t1")).thenReturn(java.util.Optional.of(trainer));

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
}

