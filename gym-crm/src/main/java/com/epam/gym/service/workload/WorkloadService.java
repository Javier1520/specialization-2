package com.epam.gym.service.workload;

import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.WorkloadRequest;
import com.epam.gym.util.LogUtils;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);

    private final Map<ActionType, WorkloadActionHandler> handlerMap = new EnumMap<>(ActionType.class);
    private final LogUtils logUtils;

    public WorkloadService(List<WorkloadActionHandler> handlers, LogUtils logUtils) {
        handlers.forEach(handler -> handlerMap.put(handler.getSupportedAction(), handler));
        this.logUtils = logUtils;
    }

    public void updateWorkload(WorkloadRequest request) {
        ActionType actionType = request.actionType();
        WorkloadActionHandler handler = handlerMap.get(actionType);

        if (handler == null) {
            logUtils.error(log, "No workload handler found for action type {}", actionType);
            throw new IllegalArgumentException("Unsupported action type: " + actionType);
        }

        handler.handle(request);
    }
}

