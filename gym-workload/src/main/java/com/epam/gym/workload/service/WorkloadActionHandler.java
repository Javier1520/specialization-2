package com.epam.gym.workload.service;

import com.epam.gym.workload.entity.MonthEntity;

public interface WorkloadActionHandler {

    void handle(MonthEntity monthEntity, int duration);
}
