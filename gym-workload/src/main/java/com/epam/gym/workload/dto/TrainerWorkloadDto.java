package com.epam.gym.workload.dto;

import java.util.List;

public record TrainerWorkloadDto(
        String username,
        String firstName,
        String lastName,
        Boolean status,
        List<YearSummaryDto> years
) {}
