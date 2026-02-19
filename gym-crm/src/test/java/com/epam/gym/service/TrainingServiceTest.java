package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.DeleteTrainingRequest;
import com.epam.gym.dto.workload.ActionType;
import com.epam.gym.dto.workload.AddWorkloadRequest;
import com.epam.gym.dto.workload.DeleteWorkloadRequest;
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
import com.epam.gym.service.workload.WorkloadService;
import com.epam.gym.util.LogUtils;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock TrainingRepository trainingRepository;
    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @Mock TrainingMapper trainingMapper;
    @Mock LogUtils logUtils;
    @Mock WorkloadService workloadService;

    @InjectMocks TrainingServiceImpl trainingService;

    private AddTrainingRequest addRequest;

    @BeforeEach
    void setUp() {
        Date trainingDate = new Date();
        addRequest = new AddTrainingRequest(
        "trainee1", "trainer1", "S1", trainingDate, 30,
                        ActionType.ADD);
    }

    @Test
    void addTraining_bindsEntitiesAndSavesAndUpdatesWorkload() {
        // Arrange
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        Trainer persistedTr =
                Trainer.builder()
                        .username("trainer1")
                        .id(22L)
                        .specialization(TrainingType.Type.HIIT)
                        .build();
        Training mappedTraining =
                Training.builder().name("S1").date(addRequest.trainingDate()).duration(30).build();

        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(persistedTr));
        when(trainingMapper.toEntity(addRequest)).thenReturn(mappedTraining);
        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(
                        invocation -> {
                            Training training = invocation.getArgument(0);
                            return Training.builder()
                                    .id(100L)
                                    .name(training.getName())
                                    .date(training.getDate())
                                    .duration(training.getDuration())
                                    .trainee(training.getTrainee())
                                    .trainer(training.getTrainer())
                                    .build();
                        });

        // Act
        trainingService.addTraining(addRequest);

        // Assert
        verify(traineeRepository).findByUsername("trainee1");
        verify(trainerRepository).findByUsername("trainer1");
        verify(trainingMapper).toEntity(addRequest);
        verify(trainingRepository).save(any(Training.class));
        verify(workloadService).addWorkload(any(AddWorkloadRequest.class));
    }

    @Test
    void addTraining_missingTrainee_throwsNotFound() {
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainingService.addTraining(addRequest));
    }

    @Test
    void addTraining_trainerNotFound_throwsNotFound() {
        Trainee persistedT = Trainee.builder().username("trainee1").id(11L).build();
        when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(persistedT));
        when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.addTraining(addRequest));
    }

    @Test
    void deleteTraining_withTrainer_deletesAndUpdatesWorkload() {
        // Arrange
        Date trainingDate = new Date();
        DeleteTrainingRequest deleteRequest =
                new DeleteTrainingRequest(
                        "trainee1", "trainer1", "S1", trainingDate,
                        30, ActionType.DELETE);

        Trainee trainee = Trainee.builder().username("trainee1").id(11L).build();
        Trainer trainer =
                Trainer.builder()
                        .username("trainer1")
                        .id(22L)
                        .firstName("T")
                        .lastName("R")
                        .isActive(true)
                        .specialization(TrainingType.Type.HIIT)
                        .build();
        Training training =
                Training.builder()
                        .id(100L)
                        .name("S1")
                        .date(trainingDate)
                        .duration(30)
                        .trainee(trainee)
                        .trainer(trainer)
                        .build();

        when(trainingRepository.findByTraineeAndTrainerAndNameAndDate(
                        "trainee1", "trainer1", "S1", trainingDate))
                .thenReturn(Optional.of(training));

        // Act
        trainingService.deleteTraining(deleteRequest);

        // Assert
        verify(trainingRepository)
                .findByTraineeAndTrainerAndNameAndDate("trainee1", "trainer1",
                        "S1", trainingDate);
        verify(trainingRepository).delete(training);
        verify(workloadService).deleteWorkload(any(DeleteWorkloadRequest.class));
    }

    @Test
    void deleteTraining_withoutTrainer_deletesAndSkipsWorkload() {
        Date trainingDate = new Date();
        DeleteTrainingRequest deleteRequest =
                new DeleteTrainingRequest(
                        "trainee1", "trainer1", "S1", trainingDate,
                        30, ActionType.DELETE);

        Trainee trainee = Trainee.builder().username("trainee1").id(11L).build();
        Training training =
                Training.builder()
                        .id(100L)
                        .name("S1")
                        .date(trainingDate)
                        .duration(30)
                        .trainee(trainee)
                        .trainer(null)
                        .build();

        when(trainingRepository.findByTraineeAndTrainerAndNameAndDate(
                        "trainee1", "trainer1", "S1", trainingDate))
                .thenReturn(Optional.of(training));

        trainingService.deleteTraining(deleteRequest);

        verify(trainingRepository).delete(training);
        // no workload update when trainer is null
        verify(workloadService, org.mockito.Mockito.never()).deleteWorkload(any(DeleteWorkloadRequest.class));
    }

    @Test
    void deleteTraining_missingTraining_throwsNotFound() {
        Date trainingDate = new Date();
        DeleteTrainingRequest deleteRequest =
                new DeleteTrainingRequest(
                        "trainee1", "trainer1", "S1", trainingDate,
                        30, ActionType.DELETE);

        when(trainingRepository.findByTraineeAndTrainerAndNameAndDate(
                        "trainee1", "trainer1", "S1", trainingDate))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.deleteTraining(deleteRequest));
    }
}
