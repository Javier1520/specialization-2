package com.epam.gym.dto.request;

import com.epam.gym.model.TrainingType;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

public record TrainingFilterRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
        String trainerName,
        TrainingType.Type trainingType) {
}
