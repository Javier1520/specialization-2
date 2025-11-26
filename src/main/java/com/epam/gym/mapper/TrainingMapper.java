package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "trainingName", source = "name")
    @Mapping(target = "trainingDate", source = "date")
    @Mapping(target = "trainingType", source = "specialization")
    @Mapping(target = "trainingDuration", source = "duration")
    @Mapping(target = "trainerName",
            expression = "java(training.getTrainer() != null ? training.getTrainer().getFirstName() + " +
                    "\" \" + training.getTrainer().getLastName() : null)")
    @Mapping(target = "traineeName",
            expression = "java(training.getTrainee() != null ? training.getTrainee().getFirstName() + " +
                    "\" \" + training.getTrainee().getLastName() : null)")
    TrainingResponse toResponse(Training training);

    List<TrainingResponse> toResponseList(List<Training> trainings);
}


