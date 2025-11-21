package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType.Type specialization;
    private Boolean isActive;
    private List<TraineeInfoResponse> trainees;
}


