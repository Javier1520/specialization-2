package com.epam.gym.workload.repository;

import java.util.Optional;

public interface TrainerWorkloadCustomRepository {
    Optional<Long> findTrainingHours(String username, Integer year, Integer month);
}
