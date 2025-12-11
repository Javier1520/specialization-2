package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Old password is required") String oldPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 10, message = "New password must be at least 10 characters")
        String newPassword) {
}
