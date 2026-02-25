package com.epam.gym.config;

import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import com.epam.gym.dto.workload.TrainingHoursRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("AddWorkloadRequest", AddWorkloadRequest.class);
        typeIdMappings.put("DeleteWorkloadRequest", DeleteWorkloadRequest.class);
        typeIdMappings.put("TrainerWorkloadDto", TrainerWorkloadDto.class);
        typeIdMappings.put("TrainingHoursDto", TrainingHoursDto.class);
        typeIdMappings.put("TrainingHoursRequest", TrainingHoursRequest.class);
        converter.setTypeIdMappings(typeIdMappings);

        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
