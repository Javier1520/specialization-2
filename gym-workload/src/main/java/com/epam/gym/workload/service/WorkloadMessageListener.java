package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.entity.ProcessedMessageDocument;
import com.epam.gym.workload.repository.ProcessedMessageRepository;
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
    private final ProcessedMessageRepository processedMessageRepository;

    @JmsListener(destination = "workload.add.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleAddWorkload(@Payload AddWorkloadRequest request) {
        String messageKey = request.username() + ":" + request.trainingDate() + ":" + request.actionType();
        log.info("Received AddWorkloadRequest via JMS for trainer: {}", request.username());

        if (processedMessageRepository.existsByMessageKey(messageKey)) {
            log.warn("Duplicate message detected, skipping: {}", messageKey);
            return;
        }

        workloadService.addWorkload(request);
        processedMessageRepository.save(ProcessedMessageDocument.builder().messageKey(messageKey).build());
    }

    @JmsListener(destination = "workload.delete.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleDeleteWorkload(@Payload DeleteWorkloadRequest request) {
        String messageKey = request.username() + ":" + request.trainingDate() + ":" + request.actionType();
        log.info("Received DeleteWorkloadRequest via JMS for trainer: {}", request.username());

        if (processedMessageRepository.existsByMessageKey(messageKey)) {
            log.warn("Duplicate message detected, skipping: {}", messageKey);
            return;
        }

        workloadService.deleteWorkload(request);
        processedMessageRepository.save(ProcessedMessageDocument.builder().messageKey(messageKey).build());
    }
}
