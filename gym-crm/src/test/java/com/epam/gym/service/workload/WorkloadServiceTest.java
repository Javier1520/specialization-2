package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.util.LogUtils;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock JmsTemplate jmsTemplate;
    @Mock WorkloadClient workloadClient;
    @Mock LogUtils logUtils;

    @InjectMocks
    private WorkloadService workloadService;

    @Test
    void addWorkload_sendsJmsMessage() {
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

        verify(jmsTemplate).convertAndSend(eq("workload.add.queue"), any(AddWorkloadRequest.class));
    }

    @Test
    void deleteWorkload_sendsJmsMessage() {
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

        verify(jmsTemplate).convertAndSend(eq("workload.delete.queue"), any(DeleteWorkloadRequest.class));
    }

    @Test
    void getWorkload_fetchesViaOpenFeign() {
        TrainerWorkloadDto mockDto = new TrainerWorkloadDto("trainer1", "T", "R", true,
                List.of());
        when(workloadClient.getWorkload("trainer1")).thenReturn(mockDto);

        TrainerWorkloadDto result = workloadService.getWorkload("trainer1");

        assertEquals(mockDto, result);
        verify(workloadClient).getWorkload("trainer1");
    }

    @Test
    void getTrainingHours_fetchesViaOpenFeign() {
        TrainingHoursDto mockDto = new TrainingHoursDto("trainer1", 2024, 1, 10L);
        when(workloadClient.getTrainingHours("trainer1", 2024, 1)).thenReturn(mockDto);

        TrainingHoursDto result = workloadService.getTrainingHours("trainer1", 2024, 1);

        assertEquals(mockDto, result);
        verify(workloadClient).getTrainingHours("trainer1", 2024, 1);
    }
}

