package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.util.LogUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock WorkloadClient workloadClient;
    @Mock LogUtils logUtils;

    @InjectMocks
    private WorkloadService workloadService;

    @Test
    void addWorkload_delegatesToClient() {
        AddWorkloadRequest request =
                AddWorkloadRequest.builder()
                        .username("trainer1")
                        .firstName("T")
                        .lastName("R")
                        .isActive(true)
                        .trainingDate(LocalDate.now())
                        .trainingDuration(60)
                        .actionType(ActionType.ADD)
                        .build();

        workloadService.addWorkload(request);

        verify(workloadClient).addWorkload(any(AddWorkloadRequest.class));
    }

    @Test
    void deleteWorkload_delegatesToClient() {
        DeleteWorkloadRequest request =
                DeleteWorkloadRequest.builder()
                        .username("trainer1")
                        .firstName("T")
                        .lastName("R")
                        .isActive(true)
                        .trainingDate(LocalDate.now())
                        .trainingDuration(60)
                        .actionType(ActionType.DELETE)
                        .build();

        workloadService.deleteWorkload(request);

        verify(workloadClient).deleteWorkload(any(DeleteWorkloadRequest.class));
    }
}
