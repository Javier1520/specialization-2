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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private WorkloadService workloadService;

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
    void handleAddWorkload_exception_propagates() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).addWorkload(addRequest);

        assertThrows(RuntimeException.class, () -> listener.handleAddWorkload(addRequest));
    }

    @Test
    void handleDeleteWorkload_success() {
        listener.handleDeleteWorkload(deleteRequest);
        verify(workloadService).deleteWorkload(deleteRequest);
    }

    @Test
    void handleDeleteWorkload_exception_propagates() {
        doThrow(new RuntimeException("Test Exception")).when(workloadService).deleteWorkload(deleteRequest);

        assertThrows(RuntimeException.class, () -> listener.handleDeleteWorkload(deleteRequest));
    }
}
