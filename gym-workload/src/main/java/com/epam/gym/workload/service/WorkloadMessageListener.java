package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class WorkloadMessageListener {

    private final WorkloadService workloadService;

    @JmsListener(destination = "workload.add.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleAddWorkload(@Payload AddWorkloadRequest request) {
        log.info("Received AddWorkloadRequest via JMS for trainer: {}", request.username());
        workloadService.addWorkload(request);
    }

    @JmsListener(destination = "workload.delete.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleDeleteWorkload(@Payload DeleteWorkloadRequest request) {
        log.info("Received DeleteWorkloadRequest via JMS for trainer: {}", request.username());
        workloadService.deleteWorkload(request);
    }
}

