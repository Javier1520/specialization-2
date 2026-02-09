package com.epam.gym.workload.dto;

public record TrainingHoursDto(
        String username,
        Integer year,
        Integer month,
        Long trainingHours
) {}
