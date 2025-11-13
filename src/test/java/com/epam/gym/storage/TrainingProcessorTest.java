package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;

class TrainingProcessorTest {
    private TrainingProcessor processor;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private DataIDMapper idMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new TrainingProcessor(trainingRepository, traineeRepository, trainerRepository, idMapper);

        // Setup default ID mapping behavior
        when(idMapper.getMappedId(eq("Trainee"), eq(1L))).thenReturn(100L);
        when(idMapper.getMappedId(eq("Trainer"), eq(2L))).thenReturn(200L);
        when(idMapper.getMappedId(eq("TrainingType"), eq(1L))).thenReturn(300L);

        // Setup default repository behavior
        Trainee mockTrainee = new Trainee();
        mockTrainee.setId(100L);
        when(traineeRepository.findById(100L)).thenReturn(Optional.of(mockTrainee));

        Trainer mockTrainer = new Trainer();
        mockTrainer.setId(200L);
        when(trainerRepository.findById(200L)).thenReturn(Optional.of(mockTrainer));
    }

    @Test
    @DisplayName("Should return correct record type")
    void getRecordType_ShouldReturnTraining() {
        assertEquals("Training", processor.getRecordType());
    }

    @Test
    @DisplayName("Should process training record correctly")
    void process_ShouldCreateTrainingWithCorrectData() throws Exception {
        // Arrange
        String[] columns = {"Training", "1", "2", "Cardio Session", "CARDIO", "2025-01-01", "60"};
        Date expectedDate = dateFormat.parse("2025-01-01");

        // Setup ID mapping
        when(idMapper.getMappedId(eq("Trainee"), eq(1L))).thenReturn(100L);
        when(idMapper.getMappedId(eq("Trainer"), eq(2L))).thenReturn(200L);

        Trainee mockTrainee = new Trainee();
        mockTrainee.setId(100L);
        when(traineeRepository.findById(100L)).thenReturn(Optional.of(mockTrainee));

        Trainer mockTrainer = new Trainer();
        mockTrainer.setId(200L);
        mockTrainer.setSpecialization(TrainingType.Type.CARDIO);
        when(trainerRepository.findById(200L)).thenReturn(Optional.of(mockTrainer));

        Training mockTraining = new Training();
        mockTraining.setId(1L);
        mockTraining.setSpecialization(TrainingType.Type.CARDIO);
        when(trainingRepository.save(any(Training.class))).thenReturn(mockTraining);

        // Act
        processor.process(columns);

        // Assert
        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(trainingCaptor.capture());

        Training saved = trainingCaptor.getValue();
        assertEquals("Cardio Session", saved.getName());
        assertEquals(expectedDate, saved.getDate());
        assertEquals(60, saved.getDuration());
        assertNotNull(saved.getTrainee());
        assertNotNull(saved.getTrainer());
        assertEquals(TrainingType.Type.CARDIO, saved.getSpecialization());
    }

    @Test
    @DisplayName("Should handle multiple training records")
    void process_MultipleRecords_ShouldHandleCorrectly() throws Exception {
        // Arrange - First record
        String[] record1 = {"Training", "1", "2", "Cardio Session", "1", "2025-01-01", "60"};
        when(idMapper.getMappedId(eq("Trainee"), eq(1L))).thenReturn(100L);
        when(idMapper.getMappedId(eq("Trainer"), eq(2L))).thenReturn(200L);
        when(idMapper.getMappedId(eq("TrainingType"), eq(1L))).thenReturn(1L);

        Trainee mockTrainee1 = new Trainee();
        mockTrainee1.setId(100L);
        when(traineeRepository.findById(100L)).thenReturn(Optional.of(mockTrainee1));

        Trainer mockTrainer1 = new Trainer();
        mockTrainer1.setId(200L);
        when(trainerRepository.findById(200L)).thenReturn(Optional.of(mockTrainer1));

        // Arrange - Second record
        String[] record2 = {"Training", "2", "3", "Strength Training", "2", "2025-01-02", "45"};
        when(idMapper.getMappedId(eq("Trainee"), eq(2L))).thenReturn(101L);
        when(idMapper.getMappedId(eq("Trainer"), eq(3L))).thenReturn(201L);
        when(idMapper.getMappedId(eq("TrainingType"), eq(2L))).thenReturn(2L);

        Trainee mockTrainee2 = new Trainee();
        mockTrainee2.setId(101L);
        when(traineeRepository.findById(101L)).thenReturn(Optional.of(mockTrainee2));

        Trainer mockTrainer2 = new Trainer();
        mockTrainer2.setId(201L);
        when(trainerRepository.findById(201L)).thenReturn(Optional.of(mockTrainer2));

        // Mock training saves
        Training mockTraining1 = new Training();
        mockTraining1.setId(1L);
        Training mockTraining2 = new Training();
        mockTraining2.setId(2L);

        when(trainingRepository.save(any(Training.class)))
            .thenReturn(mockTraining1)
            .thenReturn(mockTraining2);

        // Act
        processor.process(record1);
        processor.process(record2);

        // Assert
        verify(trainingRepository, times(2)).save(any(Training.class));
    }

    @Test
    @DisplayName("Should throw exception for insufficient columns")
    void process_InsufficientColumns_ShouldThrowException() {
        // Arrange
        String[] invalidRecord = {"Training", "1", "2", "Cardio Session", "1", "2025-01-01"};

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.process(invalidRecord)
        );

        assertTrue(exception.getMessage().contains("Invalid number of columns"));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void process_InvalidDateFormat_ShouldThrowException() {
        // Arrange
        String[] invalidRecord = {"Training", "1", "2", "Cardio Session", "1", "invalid-date", "60"};

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> processor.process(invalidRecord)
        );
        verify(trainingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for invalid duration format")
    void process_InvalidDurationFormat_ShouldThrowException() {
        // Arrange
        String[] invalidRecord = {"Training", "1", "2", "Cardio Session", "1", "2025-01-01", "invalid"};

        // Act & Assert
        assertThrows(NumberFormatException.class,
            () -> processor.process(invalidRecord)
        );
        verify(trainingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when trainee not found")
    void process_TraineeNotFound_ShouldThrowException() {
        // Arrange
        String[] columns = {"Training", "1", "2", "Cardio Session", "1", "2025-01-01", "60"};

        when(traineeRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> processor.process(columns)
        );
        verify(trainingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when trainer not found")
    void process_TrainerNotFound_ShouldThrowException() {
        // Arrange
        String[] columns = {"Training", "1", "2", "Cardio Session", "1", "2025-01-01", "60"};

        when(trainerRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> processor.process(columns)
        );
        verify(trainingRepository, never()).save(any());
    }
}