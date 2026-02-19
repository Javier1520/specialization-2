package com.epam.gym.dto.request;

import com.epam.gym.dto.workload.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Date;

public record AddTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotBlank(message = "Training name is required") String trainingName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull(message = "Training date is required") Date trainingDate,
        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive")
        Integer trainingDuration,
        @NotNull(message = "Action type is required") ActionType actionType) {
}
