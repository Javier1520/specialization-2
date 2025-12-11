package com.epam.gym.repository;

import com.epam.gym.model.TrainingType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRepositoryTest {

    @Test
    void findByName_shouldReturnOptionalWhenFound() {
        TrainingTypeRepository repo = mock(TrainingTypeRepository.class);

        TrainingType tt = new TrainingType(1L, "Cardio", null, null);
        when(repo.findByName("Cardio")).thenReturn(Optional.of(tt));

        Optional<TrainingType> maybe = repo.findByName("Cardio");
        assertTrue(maybe.isPresent());
        assertEquals("Cardio", maybe.get().getName());
        verify(repo).findByName("Cardio");
    }

    @Test
    void findByName_shouldReturnEmptyOptionalWhenNotFound() {
        TrainingTypeRepository repo = mock(TrainingTypeRepository.class);
        when(repo.findByName("Unknown")).thenReturn(Optional.empty());

        Optional<TrainingType> maybe = repo.findByName("Unknown");
        assertFalse(maybe.isPresent());
        verify(repo).findByName("Unknown");
    }
}
