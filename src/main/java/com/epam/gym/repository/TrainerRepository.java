package com.epam.gym.repository;

import com.epam.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    boolean existsByUsername(String username);

    Optional<Trainer> findByUsername(String username);

    @Query("SELECT t FROM Trainer t LEFT JOIN FETCH t.trainees WHERE t.username = :username")
    Optional<Trainer> findByUsernameWithTrainees(@Param("username") String username);

    @Query("""
            SELECT t FROM Trainer t
            WHERE t.id NOT IN (
                SELECT tr.id FROM Trainer tr JOIN tr.trainees te WHERE te.id = :traineeId
            )
            """)
    List<Trainer> findNotAssignedToTrainee(@Param("traineeId") Long traineeId);
}
