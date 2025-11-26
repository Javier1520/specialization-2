package com.epam.gym.dto.response;

import java.util.Date;
import java.util.List;

public record TraineeProfileResponse(
    String username,
    String firstName,
    String lastName,
    Date dateOfBirth,
    String address,
    Boolean isActive,
    List<TrainerInfoResponse> trainers
) {}


