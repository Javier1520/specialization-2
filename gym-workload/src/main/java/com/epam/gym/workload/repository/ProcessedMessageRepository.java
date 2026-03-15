package com.epam.gym.workload.repository;

import com.epam.gym.workload.entity.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, Long> {
    boolean existsByMessageKey(String messageKey);
}
