package com.epam.gym.workload.dto;

import java.util.List;

public record YearSummaryDto(Integer yearNumber, List<MonthSummaryDto> months) {}
