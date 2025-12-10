package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotNull(message = "Trainers list is required")
                List<@NotNull TrainerUsernameRequest> trainers) {
    public record TrainerUsernameRequest(
            @NotBlank(message = "Trainer username is required") String trainerUsername) {}
}
