package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record TraineeRegistrationRequest(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    Date dateOfBirth,
    String address
) {}

