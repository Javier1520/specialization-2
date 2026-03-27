package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.entity.ProcessedMessageDocument;
import com.epam.gym.workload.repository.ProcessedMessageRepository;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class WorkloadMessageListener {

    private final WorkloadService workloadService;
    private final ProcessedMessageRepository processedMessageRepository;

    @JmsListener(destination = "workload.add.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleAddWorkload(@Payload AddWorkloadRequest request,
                                   Message rawMessage) {
        setupMdc(rawMessage, "addWorkload");
        try {
            String messageKey = request.username() + ":" + request.trainingDate() + ":" + request.actionType();
            log.info("[TXN] Received AddWorkloadRequest for trainer: {}", request.username());

            if (processedMessageRepository.existsByMessageKey(messageKey)) {
                log.warn("[TXN] Duplicate message detected, skipping: {}", messageKey);
                return;
            }

            workloadService.addWorkload(request);
            processedMessageRepository.save(
                ProcessedMessageDocument.builder().messageKey(messageKey).build());
            log.info("[TXN] Completed AddWorkloadRequest for trainer: {}", request.username());
        } finally {
            MDC.clear();
        }
    }

    @JmsListener(destination = "workload.delete.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleDeleteWorkload(@Payload DeleteWorkloadRequest request,
                                      Message rawMessage) {
        setupMdc(rawMessage, "deleteWorkload");
        try {
            String messageKey = request.username() + ":" + request.trainingDate() + ":" + request.actionType();
            log.info("[TXN] Received DeleteWorkloadRequest for trainer: {}", request.username());

            if (processedMessageRepository.existsByMessageKey(messageKey)) {
                log.warn("[TXN] Duplicate message detected, skipping: {}", messageKey);
                return;
            }

            workloadService.deleteWorkload(request);
            processedMessageRepository.save(
                ProcessedMessageDocument.builder().messageKey(messageKey).build());
            log.info("[TXN] Completed DeleteWorkloadRequest for trainer: {}", request.username());
        } finally {
            MDC.clear();
        }
    }

    private void setupMdc(Message rawMessage, String operation) {
        String transactionId = null;
        try {
            if (rawMessage != null) {
                transactionId = rawMessage.getStringProperty("transactionId");
            }
        } catch (JMSException e) {
            log.warn("Could not extract transactionId from JMS header", e);
        }
        MDC.put("transactionId",
                "[" + (transactionId != null ? transactionId : UUID.randomUUID().toString()) + "]");
        MDC.put("operation", operation);
    }
}
