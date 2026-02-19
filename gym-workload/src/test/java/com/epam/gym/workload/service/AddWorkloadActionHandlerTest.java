package com.epam.gym.workload.service;

import com.epam.gym.workload.entity.MonthEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddWorkloadActionHandlerTest {

    private final AddWorkloadActionHandler handler = new AddWorkloadActionHandler();

    @Test
    void handle_addsDuration() {
        MonthEntity month = MonthEntity.builder().trainingDuration(100).build();

        handler.handle(month, 50);

        assertEquals(150, month.getTrainingDuration());
    }
}
