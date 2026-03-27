package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.util.LogUtils;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);

    private final JmsTemplate jmsTemplate;
    private final WorkloadClient workloadClient;
    private final LogUtils logUtils;

    public WorkloadService(JmsTemplate jmsTemplate, WorkloadClient workloadClient, LogUtils logUtils) {
        this.jmsTemplate = jmsTemplate;
        this.workloadClient = workloadClient;
        this.logUtils = logUtils;
    }

    public void addWorkload(AddWorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        logUtils.info(log, "[TXN] Sending addWorkload via JMS for trainer: {}", request.username());
        jmsTemplate.convertAndSend("workload.add.queue", request, message -> {
            if (transactionId != null) {
                message.setStringProperty("transactionId", transactionId);
            }
            return message;
        });
    }

    public void deleteWorkload(DeleteWorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        logUtils.info(log, "[TXN] Sending deleteWorkload via JMS for trainer: {}", request.username());
        jmsTemplate.convertAndSend("workload.delete.queue", request, message -> {
            if (transactionId != null) {
                message.setStringProperty("transactionId", transactionId);
            }
            return message;
        });
    }

    public TrainerWorkloadDto getWorkload(String username) {
        logUtils.info(log, "Fetching workload for trainer via OpenFeign: {}", username);
        try {
            return workloadClient.getWorkload(username);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Trainer not found: " + username);
        }
    }

    public TrainingHoursDto getTrainingHours(String username, Integer year, Integer month) {
        logUtils.info(log, "Fetching training hours for trainer via OpenFeign: {}", username);
        return workloadClient.getTrainingHours(username, year, month);
    }
}

