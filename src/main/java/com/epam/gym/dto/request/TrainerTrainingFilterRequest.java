package com.epam.gym.dto.request;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

public record TrainerTrainingFilterRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
    String traineeName) {}
