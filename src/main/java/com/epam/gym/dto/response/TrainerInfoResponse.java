package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;

public record TrainerInfoResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        TrainingType.Type trainerSpecialization) {
}
