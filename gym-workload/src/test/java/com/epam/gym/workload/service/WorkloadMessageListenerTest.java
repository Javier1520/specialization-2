package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
                "trainer.username", "Trainer", "Name", true, LocalDate.now(),
                60, ActionType.ADD);
        deleteRequest = new DeleteWorkloadRequest(
                "trainer.username", "Trainer", "Name", true, LocalDate.now(),
                60, ActionType.DELETE);
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
}

