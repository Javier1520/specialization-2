package com.epam.gym.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryTest {

  @Test
  void existsByUsername_shouldReturnTrue() {
    TrainerRepository repo = mock(TrainerRepository.class);
    when(repo.existsByUsername("trainer1")).thenReturn(true);

    assertTrue(repo.existsByUsername("trainer1"));
    verify(repo).existsByUsername("trainer1");
  }

  @Test
  void findByUsername_shouldReturnTrainer() {
    TrainerRepository repo = mock(TrainerRepository.class);
    Trainer expected =
        Trainer.builder()
            .id(1L)
            .firstName("Tony")
            .lastName("Trainer")
            .username("tony.t")
            .password("pw")
            .isActive(true)
            .specialization(TrainingType.Type.STRENGTH)
            .build();

    when(repo.findByUsername("tony.t")).thenReturn(Optional.of(expected));

    Optional<Trainer> actualOptional = repo.findByUsername("tony.t");
    assertTrue(actualOptional.isPresent());

    Trainer actual = actualOptional.get();
    assertNotNull(actual);
    assertEquals(expected.getUsername(), actual.getUsername());
    verify(repo).findByUsername("tony.t");
  }

  @Test
  void findNotAssignedToTrainee_shouldReturnEmptyListWhenNone() {
    TrainerRepository repo = mock(TrainerRepository.class);
    when(repo.findNotAssignedToTrainee(99L)).thenReturn(Collections.emptyList());

    List<Trainer> result = repo.findNotAssignedToTrainee(99L);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(repo).findNotAssignedToTrainee(99L);
  }
}
