package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.epam.gym.model.Trainee;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.service.UsernamePasswordGenerator;

class TraineeProcessorTest {
    private TraineeProcessor processor;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernamePasswordGenerator generator;

    @Mock
    private DataIDMapper idMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new TraineeProcessor(traineeRepository, generator, idMapper);

        // Setup default mock behavior
        when(generator.generateUsername(
            eq("John"),
            eq("Doe"),
            any()
        )).thenReturn("john.doe");
        when(generator.generatePassword()).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> {
            Trainee trainee = (Trainee) i.getArguments()[0];
            trainee.setId(1L); // Simulate DB auto-generation
            return trainee;
        });
    }

    @Test
    @DisplayName("Should return correct record type")
    void getRecordType_ShouldReturnTrainee() {
        assertEquals("Trainee", processor.getRecordType());
    }

    @Test
    @DisplayName("Should process trainee record correctly")
    void process_ShouldCreateTraineeWithCorrectData() throws Exception {
        // Arrange
        String[] columns = {"Trainee", "John", "Doe", "1990-01-01", "123 Main St"};
        Date expectedDate = dateFormat.parse("1990-01-01");

        // Act
        processor.process(columns);

        // Assert
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(traineeCaptor.capture());
        verify(idMapper).addMapping(eq("Trainee"), eq(1L), eq(1L));

        Trainee saved = traineeCaptor.getValue();
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("123 Main St", saved.getAddress());
        assertEquals(expectedDate, saved.getDateOfBirth());
        assertEquals("john.doe", saved.getUsername());
        assertEquals("password123", saved.getPassword());
        assertTrue(saved.getIsActive());
    }

    @Test
    @DisplayName("Should throw exception when insufficient columns")
    void process_WithInsufficientColumns_ShouldThrowException() {
        // Arrange
        String[] columns = {"Trainee", "John", "Doe", "1990-01-01"};

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> processor.process(columns));
    }

    @Test
    @DisplayName("Should throw exception when invalid date format")
    void process_WithInvalidDateFormat_ShouldThrowException() {
        // Arrange
        String[] columns = {"Trainee", "John", "Doe", "invalid-date", "123 Main St"};

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> processor.process(columns));
    }

    @Test
    @DisplayName("Should handle multiple trainee records")
    void process_MultipleRecords_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String[] record1 = {"Trainee", "John", "Doe", "1990-01-01", "123 Main St"};
        String[] record2 = {"Trainee", "Jane", "Smith", "1991-02-02", "456 Oak Ave"};

        when(generator.generateUsername(eq("Jane"), eq("Smith"), any()))
            .thenReturn("jane.smith");
        when(traineeRepository.save(any(Trainee.class)))
            .thenAnswer(i -> {
                Trainee trainee = (Trainee) i.getArguments()[0];
                trainee.setId(trainee.getUsername().equals("john.doe") ? 1L : 2L);
                return trainee;
            });

        // Act
        processor.process(record1);
        processor.process(record2);

        // Assert
        verify(traineeRepository, times(2)).save(any(Trainee.class));
        verify(idMapper).addMapping(eq("Trainee"), eq(1L), eq(1L));
        verify(idMapper).addMapping(eq("Trainee"), eq(2L), eq(2L));
    }
}
