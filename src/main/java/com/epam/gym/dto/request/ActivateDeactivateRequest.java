package com.epam.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateDeactivateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Is Active is required")
    private Boolean isActive;
}


