package com.epam.gym.workload.mapper;

import com.epam.gym.workload.dto.MonthSummaryDto;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.YearSummaryDto;
import com.epam.gym.workload.entity.TrainerWorkload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkloadMapper {

    @Mapping(source = "isActive", target = "status")
    TrainerWorkloadDto toDto(TrainerWorkload entity);

    YearSummaryDto toDto(TrainerWorkload.YearSummary entity);

    MonthSummaryDto toDto(TrainerWorkload.MonthSummary entity);
}
