package com.epam.gym.dto.workload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WorkloadRequest(
    String username,
    String firstName,
    String lastName,

    @JsonProperty("active")
    boolean isActive,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate trainingDate,

    int trainingDuration,
    ActionType actionType
) {}