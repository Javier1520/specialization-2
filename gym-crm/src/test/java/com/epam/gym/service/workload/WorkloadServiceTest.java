package com.epam.gym.service.workload;

import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.WorkloadRequest;
import com.epam.gym.util.LogUtils;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock WorkloadActionHandler addHandler;
    @Mock WorkloadActionHandler deleteHandler;
    @Mock LogUtils logUtils;

    private WorkloadService workloadService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.when(addHandler.getSupportedAction()).thenReturn(ActionType.ADD);
        org.mockito.Mockito.when(deleteHandler.getSupportedAction()).thenReturn(ActionType.DELETE);
        workloadService = new WorkloadService(List.of(addHandler, deleteHandler), logUtils);
    }

    @Test
    void updateWorkload_delegatesToCorrectHandler() {
        WorkloadRequest request =
                WorkloadRequest.builder()
                        .username("trainer1")
                        .firstName("T")
                        .lastName("R")
                        .isActive(true)
                        .trainingDate(LocalDate.now())
                        .trainingDuration(60)
                        .actionType(ActionType.ADD)
                        .build();

        workloadService.updateWorkload(request);

        verify(addHandler).handle(any(WorkloadRequest.class));
    }

    @Test
    void updateWorkload_unsupportedAction_throwsIllegalArgument() {
        WorkloadRequest badRequest =
                WorkloadRequest.builder()
                        .username("trainer1")
                        .firstName("T")
                        .lastName("R")
                        .isActive(true)
                        .trainingDate(LocalDate.now())
                        .trainingDuration(60)
                        .actionType(null)
                        .build();

        assertThrows(
                IllegalArgumentException.class, () -> workloadService.updateWorkload(badRequest));
    }
}
