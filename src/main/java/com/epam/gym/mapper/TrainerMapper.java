package com.epam.gym.mapper;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.TraineeInfoResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toEntity(TrainerRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    void updateEntityFromRequest(UpdateTrainerRequest request, @MappingTarget Trainer entity);

    @Mapping(target = "trainees", expression =
            "java(trainer.getTrainees() != null ? traineesToInfoResponseList(trainer.getTrainees()) : null)")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(target = "trainerUsername", source = "username")
    @Mapping(target = "trainerFirstName", source = "firstName")
    @Mapping(target = "trainerLastName", source = "lastName")
    @Mapping(target = "trainerSpecialization", source = "specialization")
    TrainerInfoResponse toInfoResponse(Trainer trainer);

    default List<TraineeInfoResponse> traineesToInfoResponseList(List<Trainee> trainees) {
        if (trainees == null) {
            return null;
        }
        return trainees.stream()
                .map(this::traineeToInfoResponse)
                .toList();
    }

    @Mapping(target = "traineeUsername", source = "username")
    @Mapping(target = "traineeFirstName", source = "firstName")
    @Mapping(target = "traineeLastName", source = "lastName")
    TraineeInfoResponse traineeToInfoResponse(Trainee trainee);
}

