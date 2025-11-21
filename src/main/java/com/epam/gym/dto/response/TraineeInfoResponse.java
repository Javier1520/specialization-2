package com.epam.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeInfoResponse {
    private String traineeUsername;
    private String traineeFirstName;
    private String traineeLastName;
}


