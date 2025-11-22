package com.epam.gym.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TrainingTypeService trainingTypeService;

    @InjectMocks private GymFacade facade;

    private Trainee sampleTrainee;
    private Trainer sampleTrainer;
    private Training sampleTraining;
    private TrainingType sampleType;

    @BeforeEach
    void setUp() {
        sampleTrainee = Trainee.builder()
                .id(10L)
                .firstName("T")
                .lastName("One")
                .username("t.one")
                .build();

        sampleTrainer = Trainer.builder()
                .id(20L)
                .firstName("R")
                .lastName("Two")
                .username("r.two")
                .build();

        sampleTraining = Training.builder()
                .id(100L)
                .name("Sesh")
                .build();

        sampleType = new TrainingType(1L, "Cardio", null, null);
    }

    @Test
    void createTrainee_delegatesToService_andReturnsSaved() {
        when(traineeService.createTrainee(sampleTrainee)).thenReturn(sampleTrainee);

        Trainee out = facade.createTrainee(sampleTrainee);

        assertSame(sampleTrainee, out);
        verify(traineeService).createTrainee(sampleTrainee);
    }

    @Test
    void updateTrainee_delegatesAndReturns() {
        when(traineeService.updateTrainee("t.one", sampleTrainee)).thenReturn(sampleTrainee);

        Trainee out = facade.updateTrainee("t.one", sampleTrainee);

        assertSame(sampleTrainee, out);
        verify(traineeService).updateTrainee("t.one", sampleTrainee);
    }

    @Test
    void changeTraineePassword_delegates() {
        doNothing().when(traineeService).changePassword("t.one", "newpass");

        facade.changeTraineePassword("t.one", "newpass");

        verify(traineeService).changePassword("t.one", "newpass");
    }

    @Test
    void getTraineeByUsername_delegatesAndReturns() {
        when(traineeService.getByUsername("t.one")).thenReturn(sampleTrainee);

        Trainee out = facade.getTraineeByUsername("t.one");

        assertSame(sampleTrainee, out);
        verify(traineeService).getByUsername("t.one");
    }

    @Test
    void setTraineeActive_delegates() {
        doNothing().when(traineeService).setActive("t.one", true);

        facade.setTraineeActive("t.one", true);

        verify(traineeService).setActive("t.one", true);
    }

    @Test
    void deleteTrainee_delegates() {
        doNothing().when(traineeService).deleteByUsername("t.one");

        facade.deleteTraineeByUsername("t.one");

        verify(traineeService).deleteByUsername("t.one");
    }

    @Test
    void getTraineeTrainings_delegatesAndReturnsList() {
        // Convert LocalDate to Date
        Date from = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)); // 1 day ago
        Date to = new Date(); // current date

        when(traineeService.getTraineeTrainings("t.one", from, to, "r", TrainingType.Type.CARDIO))
                .thenReturn(List.of(sampleTraining));

        List<Training> out = facade.getTraineeTrainings("t.one", from, to, "r", TrainingType.Type.CARDIO);

        assertEquals(1, out.size());
        assertSame(sampleTraining, out.get(0));
        verify(traineeService).getTraineeTrainings("t.one", from, to, "r", TrainingType.Type.CARDIO);
    }

    @Test
    void getTrainersNotAssignedToTrainee_delegatesAndReturns() {
        when(traineeService.getTrainersNotAssignedToTrainee("t.one"))
                .thenReturn(List.of(sampleTrainer));

        List<Trainer> out = facade.getTrainersNotAssignedToTrainee("t.one");

        assertEquals(1, out.size());
        assertSame(sampleTrainer, out.get(0));
        verify(traineeService).getTrainersNotAssignedToTrainee("t.one");
    }

    @Test
    void updateTraineeTrainers_delegates() {
        doNothing().when(traineeService).updateTraineeTrainers("t.one", List.of(1L, 2L));

        facade.updateTraineeTrainers("t.one", List.of(1L, 2L));

        verify(traineeService).updateTraineeTrainers("t.one", List.of(1L, 2L));
    }

    @Test
    void createTrainer_delegatesAndReturns() {
        when(trainerService.createTrainer(sampleTrainer)).thenReturn(sampleTrainer);

        Trainer out = facade.createTrainer(sampleTrainer);

        assertSame(sampleTrainer, out);
        verify(trainerService).createTrainer(sampleTrainer);
    }

    @Test
    void updateTrainer_delegatesAndReturns() {
        when(trainerService.updateTrainer("r.two", sampleTrainer)).thenReturn(sampleTrainer);

        Trainer out = facade.updateTrainer("r.two", sampleTrainer);

        assertSame(sampleTrainer, out);
        verify(trainerService).updateTrainer("r.two", sampleTrainer);
    }

    @Test
    void changeTrainerPassword_delegates() {
        doNothing().when(trainerService).changePassword("r.two", "pwd");

        facade.changeTrainerPassword("r.two", "pwd");

        verify(trainerService).changePassword("r.two", "pwd");
    }

    @Test
    void getTrainerByUsername_delegatesAndReturns() {
        when(trainerService.getByUsername("r.two")).thenReturn(sampleTrainer);

        Trainer out = facade.getTrainerByUsername("r.two");

        assertSame(sampleTrainer, out);
        verify(trainerService).getByUsername("r.two");
    }

    @Test
    void setTrainerActive_delegates() {
        doNothing().when(trainerService).setActive("r.two", false);

        facade.setTrainerActive("r.two", false);

        verify(trainerService).setActive("r.two", false);
    }

    @Test
    void getTrainerTrainings_delegatesAndReturns() {
        Date from = new Date(System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)); // 2 days ago
        Date to = new Date(); // current date and time

        when(trainerService.getTrainerTrainings("r.two", from, to, "t"))
                .thenReturn(List.of(sampleTraining));

        List<Training> out = facade.getTrainerTrainings("r.two", from, to, "t");

        assertEquals(1, out.size());
        verify(trainerService).getTrainerTrainings("r.two", from, to, "t");
    }

    @Test
    void createTraining_delegatesAndReturns() {
        when(trainingService.addTraining(sampleTraining)).thenReturn(sampleTraining);

        Training out = facade.createTraining(sampleTraining);

        assertSame(sampleTraining, out);
        verify(trainingService).addTraining(sampleTraining);
    }

    @Test
    void listTrainingTypes_delegatesAndReturns() {
        when(trainingTypeService.listAll()).thenReturn(List.of(sampleType));

        List<TrainingType> out = facade.listTrainingTypes();

        assertEquals(1, out.size());
        assertSame(sampleType, out.get(0));
        verify(trainingTypeService).listAll();
    }

    @Test
    void exceptionsFromServices_propagateThroughFacade() {
        when(traineeService.getByUsername("missing")).thenThrow(new NotFoundException("x"));
        assertThrows(NotFoundException.class, () -> facade.getTraineeByUsername("missing"));
        verify(traineeService).getByUsername("missing");
    }

    @Test
    void noUnexpectedInteractions() {
        // quick smoke: call a method and ensure no other service is touched
        when(trainingTypeService.listAll()).thenReturn(Collections.emptyList());
        facade.listTrainingTypes();
        verify(trainingTypeService).listAll();

        verifyNoMoreInteractions(traineeService, trainerService, trainingService, trainingTypeService);
    }
}
