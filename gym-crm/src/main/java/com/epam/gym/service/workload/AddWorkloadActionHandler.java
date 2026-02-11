package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.WorkloadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddWorkloadActionHandler implements WorkloadActionHandler {

    private final WorkloadClient workloadClient;

    @Override
    public ActionType getSupportedAction() {
        return ActionType.ADD;
    }

    @Override
    public void handle(WorkloadRequest request) {
        workloadClient.updateWorkload(request);
    }
}

