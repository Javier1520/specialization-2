package com.epam.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private Boolean isActive;
    private List<TrainerInfoResponse> trainers;
}


