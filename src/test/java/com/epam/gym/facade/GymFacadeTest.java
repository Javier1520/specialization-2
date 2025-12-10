package com.epam.gym.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerInfoResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TrainingTypeService trainingTypeService;

    @InjectMocks private GymFacade facade;

    private TraineeRegistrationRequest traineeRegistrationRequest;
    private TrainerRegistrationRequest trainerRegistrationRequest;
    private AddTrainingRequest addTrainingRequest;
    private RegistrationResponse registrationResponse;
    private TraineeProfileResponse traineeProfileResponse;
    private TrainerProfileResponse trainerProfileResponse;
    private TrainingResponse trainingResponse;
    private TrainingTypeResponse trainingTypeResponse;

    @BeforeEach
    void setUp() {
        traineeRegistrationRequest =
                new TraineeRegistrationRequest("T", "One", new Date(0), "Address");
        trainerRegistrationRequest =
                new TrainerRegistrationRequest("R", "Two", TrainingType.Type.CARDIO);
        addTrainingRequest = new AddTrainingRequest("t.one", "r.two", "Sesh", new Date(), 60);
        registrationResponse = new RegistrationResponse("t.one", "password123");
        traineeProfileResponse =
                new TraineeProfileResponse(
                        "t.one", "T", "One", new Date(0), "Address", true, List.of());
        trainerProfileResponse =
                new TrainerProfileResponse(
                        "r.two", "R", "Two", TrainingType.Type.CARDIO, true, List.of());
        trainingResponse =
                new TrainingResponse(
                        "Sesh", new Date(), TrainingType.Type.CARDIO, 60, "r.two", "t.one");
        trainingTypeResponse = new TrainingTypeResponse(1L, "CARDIO");
    }

    @Test
    void createTrainee_delegatesToService_andReturnsSaved() {
        when(traineeService.createTrainee(traineeRegistrationRequest))
                .thenReturn(registrationResponse);

        RegistrationResponse out = facade.createTrainee(traineeRegistrationRequest);

        assertSame(registrationResponse, out);
        verify(traineeService).createTrainee(traineeRegistrationRequest);
    }

    @Test
    void updateTrainee_delegatesAndReturns() {
        UpdateTraineeRequest request =
                new UpdateTraineeRequest("t.one", "T", "One", new Date(0), "Address", true);
        when(traineeService.updateTrainee("t.one", request)).thenReturn(traineeProfileResponse);

        TraineeProfileResponse out = facade.updateTrainee("t.one", request);

        assertSame(traineeProfileResponse, out);
        verify(traineeService).updateTrainee("t.one", request);
    }

    @Test
    void changeTraineePassword_delegates() {
        doNothing().when(traineeService).changePassword("t.one", "newpass");

        facade.changeTraineePassword("t.one", "newpass");

        verify(traineeService).changePassword("t.one", "newpass");
    }

    @Test
    void getTraineeByUsername_delegatesAndReturns() {
        when(traineeService.getByUsername("t.one")).thenReturn(traineeProfileResponse);

        TraineeProfileResponse out = facade.getTraineeByUsername("t.one");

        assertSame(traineeProfileResponse, out);
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
        Date from = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)); // 1 day ago
        Date to = new Date(); // current date

        TrainingFilterRequest filter =
                new TrainingFilterRequest(from, to, "r", TrainingType.Type.CARDIO);

        when(traineeService.getTraineeTrainings("t.one", filter))
                .thenReturn(List.of(trainingResponse));

        List<TrainingResponse> out = facade.getTraineeTrainings("t.one", filter);

        assertEquals(1, out.size());
        assertSame(trainingResponse, out.get(0));
        verify(traineeService).getTraineeTrainings("t.one", filter);
    }

    @Test
    void getTrainersNotAssignedToTrainee_delegatesAndReturns() {
        TrainerInfoResponse trainerInfo =
                new TrainerInfoResponse("r.two", "R", "Two", TrainingType.Type.CARDIO);
        when(traineeService.getTrainersNotAssignedToTrainee("t.one"))
                .thenReturn(List.of(trainerInfo));

        List<TrainerInfoResponse> out = facade.getTrainersNotAssignedToTrainee("t.one");

        assertEquals(1, out.size());
        assertSame(trainerInfo, out.get(0));
        verify(traineeService).getTrainersNotAssignedToTrainee("t.one");
    }

    @Test
    void updateTraineeTrainers_delegates() {
        UpdateTraineeTrainersRequest request =
                new UpdateTraineeTrainersRequest(
                        List.of(new UpdateTraineeTrainersRequest.TrainerUsernameRequest("r.two")));
        TrainerInfoResponse trainerInfo =
                new TrainerInfoResponse("r.two", "R", "Two", TrainingType.Type.CARDIO);
        when(traineeService.updateTraineeTrainers("t.one", request))
                .thenReturn(List.of(trainerInfo));

        List<TrainerInfoResponse> out = facade.updateTraineeTrainers("t.one", request);

        assertEquals(1, out.size());
        verify(traineeService).updateTraineeTrainers("t.one", request);
    }

    @Test
    void createTrainer_delegatesAndReturns() {
        when(trainerService.createTrainer(trainerRegistrationRequest))
                .thenReturn(registrationResponse);

        RegistrationResponse out = facade.createTrainer(trainerRegistrationRequest);

        assertSame(registrationResponse, out);
        verify(trainerService).createTrainer(trainerRegistrationRequest);
    }

    @Test
    void updateTrainer_delegatesAndReturns() {
        UpdateTrainerRequest request =
                new UpdateTrainerRequest("r.two", "R", "Two", TrainingType.Type.CARDIO, true);
        when(trainerService.updateTrainer("r.two", request)).thenReturn(trainerProfileResponse);

        TrainerProfileResponse out = facade.updateTrainer("r.two", request);

        assertSame(trainerProfileResponse, out);
        verify(trainerService).updateTrainer("r.two", request);
    }

    @Test
    void changeTrainerPassword_delegates() {
        doNothing().when(trainerService).changePassword("r.two", "pwd");

        facade.changeTrainerPassword("r.two", "pwd");

        verify(trainerService).changePassword("r.two", "pwd");
    }

    @Test
    void getTrainerByUsername_delegatesAndReturns() {
        when(trainerService.getByUsername("r.two")).thenReturn(trainerProfileResponse);

        TrainerProfileResponse out = facade.getTrainerByUsername("r.two");

        assertSame(trainerProfileResponse, out);
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

        TrainerTrainingFilterRequest filter = new TrainerTrainingFilterRequest(from, to, "t");

        when(trainerService.getTrainerTrainings("r.two", filter))
                .thenReturn(List.of(trainingResponse));

        List<TrainingResponse> out = facade.getTrainerTrainings("r.two", filter);

        assertEquals(1, out.size());
        verify(trainerService).getTrainerTrainings("r.two", filter);
    }

    @Test
    void createTraining_delegatesAndReturns() {
        doNothing().when(trainingService).addTraining(addTrainingRequest);

        facade.createTraining(addTrainingRequest);

        verify(trainingService).addTraining(addTrainingRequest);
    }

    @Test
    void listTrainingTypes_delegatesAndReturns() {
        when(trainingTypeService.listAll()).thenReturn(List.of(trainingTypeResponse));

        List<TrainingTypeResponse> out = facade.listTrainingTypes();

        assertEquals(1, out.size());
        assertSame(trainingTypeResponse, out.get(0));
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

        verifyNoMoreInteractions(
                traineeService, trainerService, trainingService, trainingTypeService);
    }
}
