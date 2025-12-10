package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TrainingServiceImpl;
import com.epam.gym.util.LogUtils;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock TrainingRepository trainingRepository;
    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @Mock TrainingMapper trainingMapper;
    @Mock LogUtils logUtils;

    @InjectMocks TrainingServiceImpl trainingService;

    private AddTrainingRequest request;

    @BeforeEach
    void setUp() {
        Date trainingDate = new Date();
        request = new AddTrainingRequest("trainee1", "trainer1", "S1", trainingDate, 30);
    }

    @Test
    void addTraining_success_bindsEntitiesAndSaves() {
        // Arrange
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        Trainer persistedTr =
                Trainer.builder()
                        .username("trainer1")
                        .id(22L)
                        .specialization(TrainingType.Type.HIIT)
                        .build();
        Training mappedTraining =
                Training.builder().name("S1").date(request.trainingDate()).duration(30).build();

        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(persistedTr));
        when(trainingMapper.toEntity(request)).thenReturn(mappedTraining);
        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(
                        invocation -> {
                            Training training = invocation.getArgument(0);
                            return Training.builder()
                                    .id(100L)
                                    .name(training.getName())
                                    .trainee(training.getTrainee())
                                    .trainer(training.getTrainer())
                                    .build();
                        });

        // Act
        trainingService.addTraining(request);

        // Assert
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainingMapper).toEntity(request);
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTraining_missingTrainee_throwsNotFound() {
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainingService.addTraining(request));
    }

    @Test
    void addTraining_trainerUsernameProvidedButNotFound_throwsNotFound() {
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.addTraining(request));
    }
}
