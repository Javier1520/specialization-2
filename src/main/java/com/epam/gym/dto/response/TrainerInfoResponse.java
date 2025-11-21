package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerInfoResponse {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private TrainingType.Type trainerSpecialization;
}


