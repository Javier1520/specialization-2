package com.epam.gym.workload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddWorkloadRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @JsonProperty("active") @NotNull(message = "Is Active is required") Boolean isActive,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                @NotNull(message = "Training date is required")
                LocalDate trainingDate,
        @NotNull(message = "Training duration is required") Integer trainingDuration,
        @NotNull(message = "Action type is required") ActionType actionType) {}
