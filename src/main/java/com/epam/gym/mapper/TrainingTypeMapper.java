package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.model.TrainingType;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {
    @Mapping(target = "trainingTypeId", source = "id")
    @Mapping(target = "trainingType", source = "name")
    TrainingTypeResponse toResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toResponseList(List<TrainingType.Type> trainingTypes);

    default TrainingTypeResponse toResponse(TrainingType.Type type) {
        return type == null
                ? null
                : new TrainingTypeResponse((long) (type.ordinal() + 1), type.name());
    }
}
