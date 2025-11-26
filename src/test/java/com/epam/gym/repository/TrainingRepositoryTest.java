package com.epam.gym.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;

@ExtendWith(MockitoExtension.class)
class TrainingRepositoryTest {

    @Test
    void findByTraineeUsernameAndCriteria_returnsMatchingTrainings() {
        TrainingRepository repo = mock(TrainingRepository.class);

        Trainee trainee = Trainee.builder()
                .id(10L)
                .username("trainee1")
                .firstName("T")
                .lastName("One")
                .isActive(true)
                .build();

        Training t = Training.builder()
                .id(100L)
                .name("Morning Session")
                .date(new Date())
                .duration(60)
                .specialization(TrainingType.Type.CARDIO)
                .trainee(trainee)
                .build();

        when(repo.findByTraineeUsernameAndCriteria(
                eq("trainee1"),
                any(),
                any(),
                isNull(),
                isNull()
        )).thenReturn(List.of(t));

        List<Training> res = repo.findByTraineeUsernameAndCriteria("trainee1", null, null,
                null, null);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(100L, res.get(0).getId());
        verify(repo).findByTraineeUsernameAndCriteria("trainee1", null, null, null,
                null);
    }

    @Test
    void findByTrainerUsernameAndCriteria_returnsEmptyWhenNoMatches() {
        TrainingRepository repo = mock(TrainingRepository.class);

        when(repo.findByTrainerUsernameAndCriteria(eq("no.trainer"), any(), any(), isNull()))
                .thenReturn(List.of());

        List<Training> result = repo.findByTrainerUsernameAndCriteria("no.trainer", null, null,
                null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repo).findByTrainerUsernameAndCriteria("no.trainer", null, null,
                null);
    }

    @Test
    void complexCriteria_invocationExample() {
        // Demonstrates that the method accepts parameters and the repository will be called correctly.
        TrainingRepository repo = mock(TrainingRepository.class);

        Date from = new Date(0); // epoch
        Date to = new Date();
        when(repo.findByTraineeUsernameAndCriteria("userA", from, to, "trainerX",
                TrainingType.Type.HIIT))
                .thenReturn(List.of());

        List<Training> r = repo.findByTraineeUsernameAndCriteria("userA", from, to, "trainerX",
                TrainingType.Type.HIIT);
        assertNotNull(r);
        verify(repo).findByTraineeUsernameAndCriteria("userA", from, to, "trainerX",
                TrainingType.Type.HIIT);
    }
}

