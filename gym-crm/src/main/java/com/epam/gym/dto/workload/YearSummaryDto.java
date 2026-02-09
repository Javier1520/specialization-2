package com.epam.gym.dto.workload;

import java.util.List;

public record YearSummaryDto(
        Integer yearNumber,
        List<MonthSummaryDto> months
) {}
