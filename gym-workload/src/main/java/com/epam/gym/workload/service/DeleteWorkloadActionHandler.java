package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.entity.MonthEntity;
import org.springframework.stereotype.Component;

@Component
public class DeleteWorkloadActionHandler implements WorkloadActionHandler {

    @Override
    public ActionType getSupportedAction() {
        return ActionType.DELETE;
    }

    @Override
    public void handle(MonthEntity monthEntity, int duration) {
        long newDuration = monthEntity.getTrainingDuration() - duration;
        if (newDuration < 0) {
            newDuration = 0;
        }
        monthEntity.setTrainingDuration(newDuration);
    }
}

