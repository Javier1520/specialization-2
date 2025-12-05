package com.epam.gym.dto.request;

import com.epam.gym.model.TrainingType;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public record TrainingFilterRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
    String trainerName,
    TrainingType.Type trainingType) {}
