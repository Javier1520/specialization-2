package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TrainingServiceImpl;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock TrainingRepository trainingRepository;
    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;

    @InjectMocks TrainingServiceImpl trainingService;

    private Training payload;

    @BeforeEach
    void setUp() {
        Trainee t = Trainee.builder().username("trainee1").build();
        Trainer tr = Trainer.builder().username("trainer1").build();
        payload = Training.builder()
                .name("S1")
                .date(new Date())
                .duration(30)
                .specialization(TrainingType.Type.HIIT)
                .trainee(t)
                .trainer(tr)
                .build();
    }

    @Test
    void addTraining_success_bindsEntitiesAndSaves() {
        // Arrange
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        Trainer persistedTr = Trainer.builder().username("trainer1").id(22L).build();

        // Make the repository return the same payload instance (or one with proper entities)
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(persistedTr));
        when(trainingRepository.save(payload)).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            return Training.builder()
                    .id(100L)
                    .name(training.getName())
                    .trainee(training.getTrainee())  // Copy the entities
                    .trainer(training.getTrainer())  // Copy the entities
                    .build();
        });

        // Act
        Training out = trainingService.addTraining(payload);

        // Assert
        assertEquals(100L, out.getId());
        // Check that the service properly bound the entities
        assertSame(persistedT, payload.getTrainee());
        assertSame(persistedTr, payload.getTrainer());
        verify(trainingRepository).save(payload);
    }

    @Test
    void addTraining_missingPayload_throws() {
        assertThrows(ValidationException.class, () -> trainingService.addTraining(null));
    }

    @Test
    void addTraining_missingTrainee_throwsNotFound() {
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainingService.addTraining(payload));
    }

    @Test
    void addTraining_trainerUsernameProvidedButNotFound_throwsNotFound() {
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.addTraining(payload));
    }

    @Test
    void addTraining_invalidDateTooFar_throwsValidation() {
        // date > 5 years in future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 6);
        payload.setDate(cal.getTime());

        assertThrows(ValidationException.class, () -> trainingService.addTraining(payload));
    }

    @Test
    void addTraining_missingName_throwsValidation() {
        payload.setName("  ");
        assertThrows(ValidationException.class, () -> trainingService.addTraining(payload));
    }

    @Test
    void addTraining_missingSpecialization_throwsValidation() {
        payload.setSpecialization(null);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(payload));
    }
}
