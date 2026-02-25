package com.epam.gym.service.workload;

import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.util.LogUtils;
import jakarta.jms.Message;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock JmsTemplate jmsTemplate;
    @Mock LogUtils logUtils;
    @Mock MessageConverter messageConverter;
    @Mock Message message;

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
    void getWorkload_fetchesFromJms() throws Exception {
        TrainerWorkloadDto mockDto = new TrainerWorkloadDto("trainer1", "T", "R", true, java.util.List.of());

        when(jmsTemplate.sendAndReceive(eq("workload.get.queue"), any(MessageCreator.class))).thenReturn(message);
        when(jmsTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.fromMessage(message)).thenReturn(mockDto);

        TrainerWorkloadDto result = workloadService.getWorkload("trainer1");

        assertEquals(mockDto, result);
        verify(jmsTemplate).sendAndReceive(eq("workload.get.queue"), any(MessageCreator.class));
    }

    @Test
    void getTrainingHours_fetchesFromJms() throws Exception {
        TrainingHoursDto mockDto = new TrainingHoursDto("trainer1", 2024, 1, 10L);
        Map<String, Object> reqMap = Map.of(
            "username", "trainer1",
            "year", 2024,
            "month", 1
        );

        when(jmsTemplate.sendAndReceive(eq("workload.hours.queue"), any(MessageCreator.class))).thenReturn(message);
        when(jmsTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.fromMessage(message)).thenReturn(mockDto);

        TrainingHoursDto result = workloadService.getTrainingHours("trainer1", 2024, 1);

        assertEquals(mockDto, result);
        verify(jmsTemplate).sendAndReceive(eq("workload.hours.queue"), any(MessageCreator.class));
    }
}
