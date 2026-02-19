package com.epam.gym.workload.service;

import com.epam.gym.workload.entity.MonthEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteWorkloadActionHandlerTest {

    private final DeleteWorkloadActionHandler handler = new DeleteWorkloadActionHandler();

    @Test
    void handle_subtractsDuration() {
        MonthEntity month = MonthEntity.builder().trainingDuration(100).build();

        handler.handle(month, 40);

        assertEquals(60, month.getTrainingDuration());
    }

    @Test
    void handle_subtractsDuration_notBelowZero() {
        MonthEntity month = MonthEntity.builder().trainingDuration(30).build();

        handler.handle(month, 40);

        assertEquals(0, month.getTrainingDuration());
    }
}
