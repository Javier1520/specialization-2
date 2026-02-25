package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.dto.TrainingHoursRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private final WorkloadService workloadService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "workload.add.queue")
    public void handleAddWorkload(@Payload AddWorkloadRequest request) {
        log.info("Received AddWorkloadRequest via JMS for trainer: {}", request.username());
        try {
            workloadService.addWorkload(request);
        } catch (Exception e) {
            log.error("Error processing AddWorkloadRequest. Sending to DLQ.", e);
            jmsTemplate.convertAndSend("workload.dlq", request);
        }
    }

    @JmsListener(destination = "workload.delete.queue")
    public void handleDeleteWorkload(@Payload DeleteWorkloadRequest request) {
        log.info("Received DeleteWorkloadRequest via JMS for trainer: {}", request.username());
        try {
            workloadService.deleteWorkload(request);
        } catch (Exception e) {
            log.error("Error processing DeleteWorkloadRequest. Sending to DLQ.", e);
            jmsTemplate.convertAndSend("workload.dlq", request);
        }
    }

    @JmsListener(destination = "workload.get.queue")
    public TrainerWorkloadDto handleGetWorkload(@Payload String username) {
        log.info("Received GetWorkload request via JMS for trainer: {}", username);
        try {
            return workloadService.getWorkload(username);
        } catch (Exception e) {
            log.error("Error processing GetWorkload request.", e);
            throw new RuntimeException("Failed to get workload", e);
        }
    }

    @JmsListener(destination = "workload.hours.queue")
    public TrainingHoursDto handleGetTrainingHours(@Payload TrainingHoursRequest request) {
        log.info("Received GetTrainingHours request via JMS for trainer: {}, {}/{}", request.username(), request.month(), request.year());
        try {
            return workloadService.getTrainingHours(request.username(), request.year(), request.month());
        } catch (Exception e) {
            log.error("Error processing GetTrainingHours request.", e);
            throw new RuntimeException("Failed to get training hours", e);
        }
    }
}
