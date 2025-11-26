package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record UpdateTraineeRequest(
    @NotBlank(message = "Username is required") String username,
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    Date dateOfBirth,
    String address,
    @NotNull(message = "Is Active is required") Boolean isActive
) {}


