package com.epam.gym.workload.service;

import com.epam.gym.workload.entity.MonthEntity;
import org.springframework.stereotype.Component;

@Component
public class AddWorkloadActionHandler implements WorkloadActionHandler {

    @Override
    public void handle(MonthEntity monthEntity, int duration) {
        monthEntity.setTrainingDuration(monthEntity.getTrainingDuration() + duration);
    }
}
