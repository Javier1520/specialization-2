package com.epam.gym.repository;

import com.epam.gym.model.Trainee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    boolean existsByUsername(String username);
    Optional<Trainee> findByUsername(String username);
    void deleteByUsername(String username);
}
