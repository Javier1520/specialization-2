package com.epam.gym.service.workload;

import com.epam.gym.client.WorkloadClient;
import com.epam.gym.dto.workload.WorkloadRequest;
import com.epam.gym.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);

    private final WorkloadClient workloadClient;
    private final LogUtils logUtils;

    public WorkloadService(WorkloadClient workloadClient, LogUtils logUtils) {
        this.workloadClient = workloadClient;
        this.logUtils = logUtils;
    }

    public void addWorkload(WorkloadRequest request) {
        logUtils.info(log, "Adding workload for trainer: {}", request.username());
        workloadClient.addWorkload(request);
    }

    public void deleteWorkload(WorkloadRequest request) {
        logUtils.info(log, "Deleting workload for trainer: {}", request.username());
        workloadClient.deleteWorkload(request);
    }
}
