package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    @Mapping(target = "trainingTypeId", source = "id")
    @Mapping(target = "trainingType", source = "name")
    TrainingTypeResponse toResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toResponseList(List<TrainingType> trainingTypes);
}

