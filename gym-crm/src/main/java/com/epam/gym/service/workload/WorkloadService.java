package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        logUtils.info(log, "Adding workload for trainer via JMS: {}", request.username());
        jmsTemplate.convertAndSend("workload.add.queue", request);
    }

    public void deleteWorkload(DeleteWorkloadRequest request) {
        logUtils.info(log, "Deleting workload for trainer via JMS: {}", request.username());
        jmsTemplate.convertAndSend("workload.delete.queue", request);
    }

    public TrainerWorkloadDto getWorkload(String username) {
        logUtils.info(log, "Fetching workload for trainer via OpenFeign: {}", username);
        return workloadClient.getWorkload(username);
    }

    public TrainingHoursDto getTrainingHours(String username, Integer year, Integer month) {
        logUtils.info(log, "Fetching training hours for trainer via OpenFeign: {}", username);
        return workloadClient.getTrainingHours(username, year, month);
    }
}

