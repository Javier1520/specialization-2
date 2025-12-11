package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public record TrainingResponse(
        String trainingName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") Date trainingDate,
        TrainingType.Type trainingType,
        Integer trainingDuration,
        String trainerName,
        String traineeName) {
}
