package com.epam.gym.dto.workload;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkloadRequest {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate trainingDate;

    private int trainingDuration;
    private ActionType actionType;
}
