package com.epam.gym.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.UsernamePasswordGenerator;

class TrainerProcessorTest {
    private TrainerProcessor processor;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UsernamePasswordGenerator generator;

    @Mock
    private DataIDMapper idMapper;

    @BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
    processor = new TrainerProcessor(trainerRepository, generator, idMapper);
    when(generator.generateUsername(eq("John"), eq("Doe"), argThat(predicate -> {
        return true;
    }))).thenReturn("john.doe");

    when(generator.generateUsername(eq("Jane"), eq("Smith"), argThat(predicate -> {
        return true;
    }))).thenReturn("jane.smith");

    // Default password generation - will return different values for multiple calls
    when(generator.generatePassword())
        .thenReturn("password123")
        .thenReturn("password456");
}

    @Test
    @DisplayName("Should return correct record type")
    void getRecordType_ShouldReturnTrainer() {
        assertEquals("Trainer", processor.getRecordType());
    }

    @Test
    @DisplayName("Should process valid trainer record")
    void process_ValidRecord_ShouldCreateAndStoreTrainer() {
        String[] columns = {"Trainer", "John", "Doe", "CARDIO"};

        // Mock the save operation to return a Trainer with ID
        Trainer mockTrainer = new Trainer();
        mockTrainer.setId(1L);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(mockTrainer);

        processor.process(columns);

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        verify(idMapper).addMapping(eq("Trainer"), eq(1L), eq(1L));

        Trainer saved = trainerCaptor.getValue();
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals(TrainingType.Type.CARDIO, saved.getSpecialization());
        assertEquals("john.doe", saved.getUsername());
        assertEquals("password123", saved.getPassword());
    }

    @Test
    @DisplayName("Should process multiple trainer records with incremental IDs")
    void process_MultipleRecords_ShouldAssignIncrementalIds() {
        // Setup different trainers for multiple saves
        Trainer mockTrainer1 = new Trainer();
        mockTrainer1.setId(1L);

        Trainer mockTrainer2 = new Trainer();
        mockTrainer2.setId(2L);

        when(trainerRepository.save(any(Trainer.class)))
            .thenReturn(mockTrainer1)  // First call returns ID 1
            .thenReturn(mockTrainer2); // Second call returns ID 2

        String[] record1 = {"Trainer", "John", "Doe", "CARDIO"};
        String[] record2 = {"Trainer", "Jane", "Smith", "STRENGTH"};

        processor.process(record1);
        processor.process(record2);

        verify(trainerRepository, times(2)).save(any(Trainer.class));
        verify(idMapper).addMapping(eq("Trainer"), eq(1L), eq(1L));
        verify(idMapper).addMapping(eq("Trainer"), eq(2L), eq(2L));
    }

    @Test
    @DisplayName("Should throw exception for insufficient columns")
    void process_InsufficientColumns_ShouldThrowException() {
        String[] invalidRecord = {"Trainer", "John", "Doe"};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.process(invalidRecord)
        );

        assertTrue(exception.getMessage().contains("Invalid number of columns"));
        verify(trainerRepository, never()).save(any());
    }
}