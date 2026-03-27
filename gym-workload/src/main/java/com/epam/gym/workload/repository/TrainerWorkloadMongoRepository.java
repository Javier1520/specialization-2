package com.epam.gym.workload.repository;

import com.epam.gym.workload.entity.TrainerWorkload;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerWorkloadMongoRepository extends MongoRepository<TrainerWorkload, String>, TrainerWorkloadCustomRepository {
    Optional<TrainerWorkload> findByUsername(String username);
}
