package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.entity.MonthEntity;

public interface WorkloadActionHandler {

    ActionType getSupportedAction();

    void handle(MonthEntity monthEntity, int duration);
}
