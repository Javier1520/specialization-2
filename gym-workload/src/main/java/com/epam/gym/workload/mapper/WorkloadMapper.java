package com.epam.gym.workload.mapper;

import com.epam.gym.workload.dto.MonthSummaryDto;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.YearSummaryDto;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkloadMapper {

    @Mapping(source = "active", target = "status")
    TrainerWorkloadDto toDto(TrainerEntity entity);

    YearSummaryDto toDto(YearEntity entity);

    MonthSummaryDto toDto(MonthEntity entity);
}
