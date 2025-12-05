package com.epam.gym.mapper;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.model.Training;
import com.epam.gym.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
  @Mapping(target = "trainingName", source = "name")
  @Mapping(target = "trainingDate", source = "date")
  @Mapping(target = "trainingType", source = "specialization")
  @Mapping(target = "trainingDuration", source = "duration")
  @Mapping(target = "trainerName", source = "trainer", qualifiedByName = "userToFullName")
  @Mapping(target = "traineeName", source = "trainee", qualifiedByName = "userToFullName")
  TrainingResponse toResponse(Training training);

  List<TrainingResponse> toResponseList(List<Training> trainings);

  @Named("userToFullName")
  default String userToFullName(User user) {
    return user == null ? null : user.getFirstName() + " " + user.getLastName();
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "trainingName")
  @Mapping(target = "date", source = "trainingDate")
  @Mapping(target = "duration", source = "trainingDuration")
  @Mapping(target = "specialization", ignore = true)
  @Mapping(target = "trainee", ignore = true)
  @Mapping(target = "trainer", ignore = true)
  Training toEntity(AddTrainingRequest request);
}
