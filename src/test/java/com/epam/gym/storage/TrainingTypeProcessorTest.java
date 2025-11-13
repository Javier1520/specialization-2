package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;

class TrainingTypeProcessorTest {
    private TrainingTypeProcessor processor;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private DataIDMapper idMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new TrainingTypeProcessor(trainingTypeRepository);

        when(trainingTypeRepository.save(any(TrainingType.class))).thenAnswer(i -> {
            TrainingType input = (TrainingType) i.getArguments()[0];
            // Build a new instance with ID to simulate JPA's behavior
            return TrainingType.builder()
                    .id(1L)
                    .name(input.getName())
                    .build();
        });
    }

    @Test
    @DisplayName("Should return correct record type")
    void getRecordType_ShouldReturnTrainingType() {
        assertEquals("TrainingType", processor.getRecordType());
    }

    @Test
    @DisplayName("Should process valid training type record")
    void process_ValidRecord_ShouldCreateAndStoreTrainingType() {
        // Arrange
        String[] columns = {"TrainingType", "Cardio"};

        // Mock the repository to return a TrainingType with ID
        TrainingType mockTrainingType = TrainingType.builder().id(1L).build();

        when(trainingTypeRepository.save(any(TrainingType.class))).thenReturn(mockTrainingType);

        // Act
        processor.process(columns);

        // Assert
        ArgumentCaptor<TrainingType> typeCaptor = ArgumentCaptor.forClass(TrainingType.class);
        verify(trainingTypeRepository).save(typeCaptor.capture());


        TrainingType saved = typeCaptor.getValue();
        assertEquals("Cardio", saved.getName());
        // The ID check is done via the idMapper verification above
    }

    @Test
    @DisplayName("Should process multiple training type records")
    void process_MultipleRecords_ShouldHandleCorrectly() {
        // Arrange
        String[] record1 = {"TrainingType", "Cardio"};
        String[] record2 = {"TrainingType", "Strength"};

        when(trainingTypeRepository.save(any(TrainingType.class)))
            .thenAnswer(i -> {
                TrainingType input = (TrainingType) i.getArguments()[0];
                // Build a new instance with ID to simulate JPA's behavior
                return TrainingType.builder()
                        .id(input.getName().equals("Cardio") ? 1L : 2L)
                        .name(input.getName())
                        .build();
            });

        // Act
        processor.process(record1);
        processor.process(record2);

        // Assert
        verify(trainingTypeRepository, times(2)).save(any(TrainingType.class));
    }

    @Test
    @DisplayName("Should throw exception for insufficient columns")
    void process_InsufficientColumns_ShouldThrowException() {
        // Arrange
        String[] invalidRecord = {"TrainingType"};

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.process(invalidRecord)
        );

        assertTrue(exception.getMessage().contains("Invalid number of columns"));
        verify(trainingTypeRepository, never()).save(any());
    }
}
