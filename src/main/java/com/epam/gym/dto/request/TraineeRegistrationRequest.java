package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.util.Date;

public record TraineeRegistrationRequest(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @PastOrPresent(message = "Date of birth cannot be in the future") Date dateOfBirth,
    String address
) {}

