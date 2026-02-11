package com.epam.gym.service.workload;

import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.WorkloadRequest;

public interface WorkloadActionHandler {

    ActionType getSupportedAction();

    void handle(WorkloadRequest request);
}

