package com.epam.gym.service.workload;

import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.dto.workload.TrainingHoursRequest;
import com.epam.gym.util.LogUtils;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

@Service
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);

    private final JmsTemplate jmsTemplate;
    private final LogUtils logUtils;

    public WorkloadService(JmsTemplate jmsTemplate, LogUtils logUtils) {
        this.jmsTemplate = jmsTemplate;
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
        logUtils.info(log, "Fetching workload for trainer via JMS: {}", username);
        return doSendAndReceive("workload.get.queue", username, TrainerWorkloadDto.class);
    }

    public TrainingHoursDto getTrainingHours(String username, Integer year, Integer month) {
        logUtils.info(log, "Fetching training hours for trainer via JMS: {}", username);
        TrainingHoursRequest request = new TrainingHoursRequest(username, year, month);
        return doSendAndReceive("workload.hours.queue", request, TrainingHoursDto.class);
    }

    private <T> T doSendAndReceive(String destination, Object request, Class<T> responseType) {
        Message replyMsg = jmsTemplate.sendAndReceive(destination, session -> {
            try {
                return jmsTemplate.getMessageConverter().toMessage(request, session);
            } catch (JMSException e) {
                throw new MessageConversionException("Could not convert message", e);
            }
        });
        if (replyMsg == null) {
            throw new RuntimeException("No reply received from " + destination);
        }
        try {
            return responseType.cast(jmsTemplate.getMessageConverter().fromMessage(replyMsg));
        } catch (JMSException e) {
            throw new MessageConversionException("Could not convert reply", e);
        }
    }
}
