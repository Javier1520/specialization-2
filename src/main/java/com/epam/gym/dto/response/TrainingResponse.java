package com.epam.gym.dto.response;

import com.epam.gym.model.TrainingType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {
    private String trainingName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;
    private TrainingType.Type trainingType;
    private Integer trainingDuration;
    private String trainerName;
    private String traineeName;
}


