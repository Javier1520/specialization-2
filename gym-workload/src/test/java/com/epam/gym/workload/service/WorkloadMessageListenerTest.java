package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.dto.TrainingHoursRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private WorkloadService workloadService;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private WorkloadMessageListener listener;

    private AddWorkloadRequest addRequest;
    private DeleteWorkloadRequest deleteRequest;

    @BeforeEach
    void setUp() {
        addRequest = new AddWorkloadRequest(
                "trainer.username", "Trainer", "Name", true, LocalDate.now(), 60, com.epam.gym.workload.dto.ActionType.ADD);
        deleteRequest = new DeleteWorkloadRequest(
                "trainer.username", "Trainer", "Name", true, LocalDate.now(), 60, com.epam.gym.workload.dto.ActionType.DELETE);
    }

    @Test
    void handleAddWorkload_success() {
        listener.handleAddWorkload(addRequest);
        verify(workloadService).addWorkload(addRequest);
    }

    @Test
    void handleAddWorkload_exception_sendsToDlq() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).addWorkload(addRequest);

        listener.handleAddWorkload(addRequest);

        verify(jmsTemplate).convertAndSend("workload.dlq", addRequest);
    }

    @Test
    void handleDeleteWorkload_success() {
        listener.handleDeleteWorkload(deleteRequest);
        verify(workloadService).deleteWorkload(deleteRequest);
    }

    @Test
    void handleDeleteWorkload_exception_sendsToDlq() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).deleteWorkload(deleteRequest);

        listener.handleDeleteWorkload(deleteRequest);

        verify(jmsTemplate).convertAndSend("workload.dlq", deleteRequest);
    }

    @Test
    void handleGetWorkload_success() {
        TrainerWorkloadDto expected = new TrainerWorkloadDto("trainer.username", "Trainer", "Name", true, java.util.List.of());
        when(workloadService.getWorkload("trainer.username")).thenReturn(expected);

        TrainerWorkloadDto actual = listener.handleGetWorkload("trainer.username");

        assertEquals(expected, actual);
        verify(workloadService).getWorkload("trainer.username");
    }

    @Test
    void handleGetWorkload_exception_throwsRuntimeException() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).getWorkload("trainer.username");

        assertThrows(RuntimeException.class, () -> listener.handleGetWorkload("trainer.username"));
    }

    @Test
    void handleGetTrainingHours_success() {
        TrainingHoursDto expected = new TrainingHoursDto("trainer.username", 2024, 1, 10L);
        when(workloadService.getTrainingHours("trainer.username", 2024, 1)).thenReturn(expected);

        TrainingHoursRequest request = new TrainingHoursRequest("trainer.username", 2024, 1);
        TrainingHoursDto actual = listener.handleGetTrainingHours(request);

        assertEquals(expected, actual);
        verify(workloadService).getTrainingHours("trainer.username", 2024, 1);
    }

    @Test
    void handleGetTrainingHours_exception_throwsRuntimeException() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).getTrainingHours("trainer.username", 2024, 1);

        TrainingHoursRequest request = new TrainingHoursRequest("trainer.username", 2024, 1);
        assertThrows(RuntimeException.class, () -> listener.handleGetTrainingHours(request));
    }
}
