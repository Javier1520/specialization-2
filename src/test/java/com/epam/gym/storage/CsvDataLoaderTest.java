package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

class CsvDataLoaderTest {
    private CsvDataLoader loader;

    @Mock
    private Environment env;

    @Mock
    private RecordProcessor trainingTypeProcessor;

    @Mock
    private RecordProcessor traineeProcessor;

    @Mock
    private RecordProcessor trainerProcessor;

    @Mock
    private RecordProcessor trainingProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup processors
        when(trainingTypeProcessor.getRecordType()).thenReturn("TrainingType");
        when(traineeProcessor.getRecordType()).thenReturn("Trainee");
        when(trainerProcessor.getRecordType()).thenReturn("Trainer");
        when(trainingProcessor.getRecordType()).thenReturn("Training");

        List<RecordProcessor> processors = Arrays.asList(
            trainingTypeProcessor,
            traineeProcessor,
            trainerProcessor,
            trainingProcessor
        );

        // Default to classpath:data.csv
        when(env.getProperty(eq("seed.data.csv"), eq("classpath:data.csv"))).thenReturn("classpath:data.csv");

        loader = new CsvDataLoader(env, processors);
    }

    @Test
    @DisplayName("Should load and process CSV data in correct order")
    void afterPropertiesSet_ShouldProcessRecordsInOrder() throws Exception {
        // Mock the file reading process
        when(env.getProperty(eq("seed.data.csv"), eq("classpath:data.csv")))
            .thenReturn("classpath:data.csv");

        // Execute
        assertDoesNotThrow(() -> loader.afterPropertiesSet());

        // Verify processing order using inOrder verifier
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(
            traineeProcessor,
            trainerProcessor,
            trainingProcessor
        );

        inOrder.verify(traineeProcessor, atLeastOnce()).process(any());
        inOrder.verify(trainerProcessor, atLeastOnce()).process(any());
        inOrder.verify(trainingProcessor, atLeastOnce()).process(any());
    }
}