package com.epam.gym.mapper;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.response.TraineeInfoResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(TraineeRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    void updateEntityFromRequest(UpdateTraineeRequest request, @MappingTarget Trainee entity);

    @Mapping(target = "trainers", qualifiedByName = "trainersToInfoResponseList")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(target = "traineeUsername", source = "username")
    @Mapping(target = "traineeFirstName", source = "firstName")
    @Mapping(target = "traineeLastName", source = "lastName")
    TraineeInfoResponse toInfoResponse(Trainee trainee);

    @Named("trainersToInfoResponseList")
    default List<TrainerInfoResponse> trainersToInfoResponseList(List<Trainer> trainers) {
        return trainers == null ? null :
                trainers.stream()
                        .map(this::trainerToInfoResponse)
                        .toList();
    }

    @Mapping(target = "trainerUsername", source = "username")
    @Mapping(target = "trainerFirstName", source = "firstName")
    @Mapping(target = "trainerLastName", source = "lastName")
    @Mapping(target = "trainerSpecialization", source = "specialization")
    TrainerInfoResponse trainerToInfoResponse(Trainer trainer);
}

