package com.epam.gym.workload.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrainerWorkloadCustomRepositoryImpl implements TrainerWorkloadCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<Long> findTrainingHours(String username, Integer year, Integer month) {
        MatchOperation matchUsername =
                Aggregation.match(Criteria.where("username").is(username));
        UnwindOperation unwindYears = Aggregation.unwind("years");
        MatchOperation matchYear =
                Aggregation.match(Criteria.where("years.yearNumber").is(year));
        UnwindOperation unwindMonths = Aggregation.unwind("years.months");
        MatchOperation matchMonth =
                Aggregation.match(Criteria.where("years.months.monthNumber").is(month));
        ProjectionOperation project =
                Aggregation.project()
                        .andExclude("_id")
                        .and("years.months.trainingDuration")
                        .as("trainingDuration");

        Aggregation aggregation =
                Aggregation.newAggregation(
                        matchUsername,
                        unwindYears,
                        matchYear,
                        unwindMonths,
                        matchMonth,
                        project);

        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, "trainers", Document.class);

        Document result = results.getUniqueMappedResult();
        if (result == null) {
            return Optional.empty();
        }

        Number duration = result.get("trainingDuration", Number.class);
        return Optional.of(duration != null ? duration.longValue() : 0L);
    }
}
