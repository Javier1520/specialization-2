package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTraineeTrainersRequest {
    @NotNull(message = "Trainers list is required")
    @NotEmpty(message = "Trainers list cannot be empty")
    private List<@NotNull TrainerUsernameRequest> trainers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerUsernameRequest {
        @NotBlank(message = "Trainer username is required")
        private String trainerUsername;
    }
}

