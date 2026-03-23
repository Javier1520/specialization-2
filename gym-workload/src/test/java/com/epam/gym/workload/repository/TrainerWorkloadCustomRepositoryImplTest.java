package com.epam.gym.workload.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadCustomRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<Document> aggregationResults;

    @InjectMocks
    private TrainerWorkloadCustomRepositoryImpl repository;

    @Test
    void findTrainingHours_returnsHoursWhenMatchFound() {
        Document resultDoc = new Document("trainingDuration", 120L);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(resultDoc);

        Optional<Long> result = repository.findTrainingHours("trainer1", 2025, 1);

        assertTrue(result.isPresent());
        assertEquals(120L, result.get());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class));
    }

    @Test
    void findTrainingHours_returnsEmptyWhenNoMatch() {
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

        Optional<Long> result = repository.findTrainingHours("trainer1", 2025, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void findTrainingHours_returnsZeroWhenDurationIsNull() {
        Document resultDoc = new Document("trainingDuration", null);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(resultDoc);

        Optional<Long> result = repository.findTrainingHours("trainer1", 2025, 1);

        assertTrue(result.isPresent());
        assertEquals(0L, result.get());
    }

    @Test
    void findTrainingHours_buildsCorrectAggregationPipeline() {
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

        repository.findTrainingHours("trainer1", 2025, 3);

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(captor.capture(), eq("trainers"), eq(Document.class));

        Aggregation captured = captor.getValue();
        // Verify the pipeline has 6 stages: match, unwind, match, unwind, match, project
        assertEquals(6, captured.toPipeline(Aggregation.DEFAULT_CONTEXT).size());
    }

    @Test
    void findTrainingHours_handlesIntegerDuration() {
        // MongoDB may store numbers as Integer instead of Long
        Document resultDoc = new Document("trainingDuration", 90);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("trainers"), eq(Document.class)))
                .thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(resultDoc);

        Optional<Long> result = repository.findTrainingHours("trainer1", 2025, 6);

        assertTrue(result.isPresent());
        assertEquals(90L, result.get());
    }
}
