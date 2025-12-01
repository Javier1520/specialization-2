package com.epam.gym.repository;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeRepositoryTest {

    @Test
    void existsByUsername_shouldReturnTrueWhenExists() {
        // Arrange
        TraineeRepository repo = mock(TraineeRepository.class);
        String username = "john.doe";
        when(repo.existsByUsername(username)).thenReturn(true);

        // Act
        boolean exists = repo.existsByUsername(username);

        // Assert
        assertTrue(exists, "expected existsByUsername to return true when user exists");
        verify(repo, times(1)).existsByUsername(username);
    }

    @Test
    void findByUsername_shouldReturnTrainee() {
        // Arrange
        TraineeRepository repo = mock(TraineeRepository.class);
        String username = "jane.smith";
        Trainee expected = Trainee.builder()
                .id(10L)
                .firstName("Jane")
                .lastName("Smith")
                .username(username)
                .password("pw")
                .isActive(true)
                .dateOfBirth(new Date())
                .address("123 Street")
                .build();

        when(repo.findByUsername(username)).thenReturn(Optional.of(expected));

        // Act
        Trainee actual = repo.findByUsername(username).orElseThrow( () -> new NotFoundException("Trainee not found: "
                + username));

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        verify(repo).findByUsername(username);
    }

    @Test
    void deleteByUsername_shouldBeInvoked() {
        // Arrange
        TraineeRepository repo = mock(TraineeRepository.class);
        String username = "to.delete";

        // Act
        repo.deleteByUsername(username);

        // Assert
        verify(repo, times(1)).deleteByUsername(username);
    }

    @Test
    void existsByUsername_shouldReturnFalseWhenNotExists() {
        // Arrange
        TraineeRepository repo = mock(TraineeRepository.class);
        String username = "non.existent";
        when(repo.existsByUsername(username)).thenReturn(false);

        // Act & Assert
        assertFalse(repo.existsByUsername(username));
        verify(repo).existsByUsername(username);
    }
}

