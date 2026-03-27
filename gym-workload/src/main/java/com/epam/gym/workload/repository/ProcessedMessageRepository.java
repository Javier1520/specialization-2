package com.epam.gym.workload.repository;

import com.epam.gym.workload.entity.ProcessedMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedMessageRepository extends MongoRepository<ProcessedMessageDocument, String> {
    boolean existsByMessageKey(String messageKey);
}