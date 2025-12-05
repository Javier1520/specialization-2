package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;
import java.util.List;

public record TrainerProfileResponse(
    String username,
    String firstName,
    String lastName,
    TrainingType.Type specialization,
    Boolean isActive,
    List<TraineeInfoResponse> trainees) {}
