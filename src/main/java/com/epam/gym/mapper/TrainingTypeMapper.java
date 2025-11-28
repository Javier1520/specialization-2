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

    List<TrainingTypeResponse> toResponseList(List<TrainingType.Type> trainingTypes);

    default TrainingTypeResponse toResponse(TrainingType.Type type) {
        if (type == null) {
            return null;
        }
        // Map enum ordinal + 1 as ID (since DB IDs start at 1) and enum name as the type
        return new TrainingTypeResponse((long) (type.ordinal() + 1), type.name());
    }
}

