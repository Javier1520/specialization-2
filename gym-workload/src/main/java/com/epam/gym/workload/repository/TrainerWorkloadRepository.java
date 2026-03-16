package com.epam.gym.workload.repository;

import com.epam.gym.workload.entity.TrainerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerEntity, Long> {
    @EntityGraph(attributePaths = {"years", "years.months"})
    Optional<TrainerEntity> findByUsername(String username);
}
