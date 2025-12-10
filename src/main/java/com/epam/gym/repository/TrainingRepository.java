package com.epam.gym.repository;

import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query(
            """
                SELECT tr FROM Training tr
                JOIN FETCH tr.trainee te
                LEFT JOIN FETCH tr.trainer trn
                WHERE te.username = :username
                  AND (:fromDate IS NULL OR tr.date >= :fromDate)
                  AND (:toDate IS NULL OR tr.date <= :toDate)
                  AND (:trainerName IS NULL OR trn.username LIKE %:trainerName%)
                  AND (:trainingType IS NULL OR tr.specialization = :trainingType)
                ORDER BY tr.date DESC
            """)
    List<Training> findByTraineeUsernameWithOptionalFilters(
            @Param("username") String username,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") TrainingType.Type trainingType);

    @Query(
            """
                SELECT tr FROM Training tr
                JOIN FETCH tr.trainer t
                LEFT JOIN FETCH tr.trainee te
                WHERE t.username = :username
                  AND (:fromDate IS NULL OR tr.date >= :fromDate)
                  AND (:toDate IS NULL OR tr.date <= :toDate)
                  AND (:traineeName IS NULL OR te.username LIKE %:traineeName%)
                ORDER BY tr.date DESC
            """)
    List<Training> findByTrainerUsernameWithOptionalFilters(
            @Param("username") String username,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("traineeName") String traineeName);
}
