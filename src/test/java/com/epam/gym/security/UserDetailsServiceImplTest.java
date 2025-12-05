package com.epam.gym.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private TraineeRepository traineeRepository;

  @Mock private TrainerRepository trainerRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  private Trainee testTrainee;
  private Trainer testTrainer;

  @BeforeEach
  void setUp() {
    testTrainee =
        Trainee.builder()
            .username("trainee.user")
            .password("password123")
            .isActive(true)
            .firstName("John")
            .lastName("Doe")
            .build();

    testTrainer =
        Trainer.builder()
            .username("trainer.user")
            .password("password456")
            .isActive(true)
            .firstName("Jane")
            .lastName("Smith")
            .specialization(TrainingType.Type.CARDIO)
            .build();
  }

  @Test
  void loadUserByUsername_traineeExists_shouldReturnUserDetails() {
    // Given
    when(traineeRepository.findByUsername("trainee.user")).thenReturn(Optional.of(testTrainee));

    // When
    UserDetails userDetails = userDetailsService.loadUserByUsername("trainee.user");

    // Then
    assertNotNull(userDetails);
    assertEquals("trainee.user", userDetails.getUsername());
    assertEquals("password123", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());
    assertEquals(1, userDetails.getAuthorities().size());
    assertTrue(
        userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

    verify(traineeRepository).findByUsername("trainee.user");
  }

  @Test
  void loadUserByUsername_trainerExists_shouldReturnUserDetails() {
    // Given
    when(traineeRepository.findByUsername("trainer.user")).thenReturn(Optional.empty());
    when(trainerRepository.findByUsername("trainer.user")).thenReturn(Optional.of(testTrainer));

    // When
    UserDetails userDetails = userDetailsService.loadUserByUsername("trainer.user");

    // Then
    assertNotNull(userDetails);
    assertEquals("trainer.user", userDetails.getUsername());
    assertEquals("password456", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());

    verify(traineeRepository).findByUsername("trainer.user");
    verify(trainerRepository).findByUsername("trainer.user");
  }

  @Test
  void loadUserByUsername_userNotFound_shouldThrowException() {
    // Given
    when(traineeRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    when(trainerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // When/Then
    UsernameNotFoundException exception =
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent"));

    assertTrue(exception.getMessage().contains("User not found"));
    assertTrue(exception.getMessage().contains("nonexistent"));

    verify(traineeRepository).findByUsername("nonexistent");
    verify(trainerRepository).findByUsername("nonexistent");
  }

  @Test
  void loadUserByUsername_inactiveTrainee_shouldReturnDisabledUser() {
    // Given
    testTrainee.setIsActive(false);
    when(traineeRepository.findByUsername("trainee.user")).thenReturn(Optional.of(testTrainee));

    // When
    UserDetails userDetails = userDetailsService.loadUserByUsername("trainee.user");

    // Then
    assertNotNull(userDetails);
    assertFalse(userDetails.isEnabled());
  }

  @Test
  void loadUserByUsername_inactiveTrainer_shouldReturnDisabledUser() {
    // Given
    testTrainer.setIsActive(false);
    when(traineeRepository.findByUsername("trainer.user")).thenReturn(Optional.empty());
    when(trainerRepository.findByUsername("trainer.user")).thenReturn(Optional.of(testTrainer));

    // When
    UserDetails userDetails = userDetailsService.loadUserByUsername("trainer.user");

    // Then
    assertNotNull(userDetails);
    assertFalse(userDetails.isEnabled());
  }

  @Test
  void loadUserByUsername_traineeTakesPrecedenceOverTrainer() {
    // Given - Both trainee and trainer exist with same username (edge case)
    String sharedUsername = "shared.user";
    testTrainee.setUsername(sharedUsername);
    when(traineeRepository.findByUsername(sharedUsername)).thenReturn(Optional.of(testTrainee));

    // When
    UserDetails userDetails = userDetailsService.loadUserByUsername(sharedUsername);

    // Then
    assertNotNull(userDetails);
    assertEquals(sharedUsername, userDetails.getUsername());
    assertEquals("password123", userDetails.getPassword()); // Trainee's password

    verify(traineeRepository).findByUsername(sharedUsername);
    // Trainer repository should not be called since trainee was found
  }
}
