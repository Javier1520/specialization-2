package com.epam.gym.workload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainingHoursRequest(
        @NotBlank(message = "Username is required") String username,
        @NotNull(message = "Year is required") Integer year,
        @NotNull(message = "Month is required") Integer month) {}
