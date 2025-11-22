package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TraineeServiceImpl;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @Mock TrainingRepository trainingRepository;
    @Mock UsernamePasswordGenerator usernamePasswordGenerator;

    @InjectMocks TraineeServiceImpl traineeService;

    private Trainee samplePayload;

    @BeforeEach
    void setUp() {
        samplePayload = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(new Date(0))
                .address("Addr")
                .build();
    }

    @Test
    void createTrainee_success_generatesUsernameAndSaves() {
        when(usernamePasswordGenerator.generateUsername(eq("John"), eq("Doe"), any()))
                .thenReturn("john.doe");
        when(usernamePasswordGenerator.generatePassword()).thenReturn("pass1234");
        Trainee saved = Trainee.builder()
                .id(1L)
                .firstName("John").lastName("Doe")
                .username("john.doe").password("pass1234").isActive(true)
                .build();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(saved);

        Trainee result = traineeService.createTrainee(samplePayload);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.doe", result.getUsername());
        assertTrue(result.getIsActive());
        verify(usernamePasswordGenerator).generateUsername(eq("John"), eq("Doe"), any());
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void createTrainee_validation_nullPayload_throws() {
        assertThrows(ValidationException.class, () -> traineeService.createTrainee(null));
    }

    @Test
    void createTrainee_futureDate_throwsValidationException() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();

        Trainee futurePayload = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(tomorrow)
                .address("Addr")
                .build();

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> traineeService.createTrainee(futurePayload)
        );

        assertEquals("dateOfBirth cannot be in the future", ex.getMessage());
        verifyNoInteractions(usernamePasswordGenerator);
        verify(traineeRepository, never()).save(any());
    }


    @Test
    void getByUsername_found_returnsTrainee() {
        Trainee t = Trainee.builder().username("u1").build();
        when(traineeRepository.findByUsername("u1")).thenReturn(Optional.of(t));

        Trainee result = traineeService.getByUsername("u1");
        assertSame(t, result);
        verify(traineeRepository).findByUsername("u1");
    }

    @Test
    void getByUsername_notFound_throws() {
        when(traineeRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.getByUsername("x"));
    }

    @Test
    void changePassword_valid_updatesPasswordAndSaves() {
        Trainee t = Trainee.builder().username("u2").password("old4567890").build();
        when(traineeRepository.findByUsername("u2")).thenReturn(Optional.of(t));
        when(traineeRepository.save(any())).thenReturn(t);

        traineeService.changePassword("u2", "newpass890");

        assertEquals("newpass890", t.getPassword());
        verify(traineeRepository).save(t);
    }

    @Test
    void changePassword_shortPassword_throwsValidation() {
        assertThrows(ValidationException.class, () -> traineeService.changePassword("u", "123"));
    }

    @Test
    void changePassword_userNotFound_throwsNotFound() {
        when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.changePassword("no", "password90"));
    }

    @Test
    void updateTrainee_happyPath_updatesFields() {
        Trainee existing = Trainee.builder()
                .username("u").firstName("A").lastName("B").address("old").dateOfBirth(new Date(0)).build();
        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any())).thenReturn(existing);

        Trainee update = Trainee.builder().firstName("X").lastName("Y").address("new").dateOfBirth(new Date(1)).build();
        Trainee result = traineeService.updateTrainee("u", update);

        assertEquals("X", result.getFirstName());
        assertEquals("Y", result.getLastName());
        assertEquals("new", result.getAddress());
        verify(traineeRepository).save(existing);
    }

    @Test
    void updateTrainee_notFound_throwsNotFound() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.updateTrainee("missing", new Trainee()));
    }

    @Test
    void setActive_whenSameState_throwsValidation() {
        Trainee t = Trainee.builder().username("u").isActive(true).build();
        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        assertThrows(ValidationException.class, () -> traineeService.setActive("u", true));
    }

    @Test
    void setActive_success_callsSave() {
        Trainee t = Trainee.builder().username("u").isActive(false).build();
        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        when(traineeRepository.save(any())).thenReturn(t);

        traineeService.setActive("u", true);
        assertTrue(t.getIsActive());
        verify(traineeRepository).save(t);
    }

    @Test
    void deleteByUsername_removesAssociationsAndDeletes() {
        Trainee t = Trainee.builder().username("u").id(10L).trainers(new ArrayList<>()).build();
        // create two trainers that reference the trainee
        Trainer trainer1 = Trainer.builder().id(1L).username("tr1").trainees(new ArrayList<>()).build();
        Trainer trainer2 = Trainer.builder().id(2L).username("tr2").trainees(new ArrayList<>()).build();
        // set both to have trainee in their trainees list
        trainer1.getTrainees().add(t);
        trainer2.getTrainees().add(t);
        t.getTrainers().add(trainer1);
        t.getTrainers().add(trainer2);

        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        when(trainerRepository.saveAll(anyIterable())).thenReturn(List.of(trainer1, trainer2));

        traineeService.deleteByUsername("u");

        // verify trainers had trainee removed
        assertFalse(trainer1.getTrainees().contains(t));
        assertFalse(trainer2.getTrainees().contains(t));
        verify(trainerRepository).saveAll(argThat(list -> ((List<?>)list).size() == 2));
        verify(traineeRepository).deleteByUsername("u");
    }

    @Test
    void deleteByUsername_notFound_throwsNotFound() {
        when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.deleteByUsername("no"));
    }

    @Test
    void getTraineeTrainings_convertsDatesAndCallsRepository() {
        Training t = Training.builder().id(99L).build();
        when(trainingRepository.findByTraineeUsernameAndCriteria(eq("u"), any(Date.class), any(Date.class), isNull(), isNull()))
                .thenReturn(List.of(t));

        // Convert LocalDate to Date
        Date fromDate = new Date(0); // epoch start (Jan 1, 1970)
        Date toDate = new Date(); // current date

        List<Training> result = traineeService.getTraineeTrainings("u", fromDate, toDate, null, null);
        assertEquals(1, result.size());
        verify(trainingRepository).findByTraineeUsernameAndCriteria(eq("u"), any(Date.class), any(Date.class), isNull(), isNull());
    }

    @Test
    void getTrainersNotAssignedToTrainee_found_callsTrainerRepo() {
        Trainee t = Trainee.builder().id(15L).username("u").build();
        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        when(trainerRepository.findNotAssignedToTrainee(15L)).thenReturn(Collections.emptyList());

        List<Trainer> out = traineeService.getTrainersNotAssignedToTrainee("u");
        assertNotNull(out);
        verify(trainerRepository).findNotAssignedToTrainee(15L);
    }

    @Test
    void getTrainersNotAssignedToTrainee_notFound_throws() {
        when(traineeRepository.findByUsername("bad")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.getTrainersNotAssignedToTrainee("bad"));
    }

    @Test
    void updateTraineeTrainers_success_synchronizesAssociations() {
        Trainee t = Trainee.builder().username("u").id(20L).trainers(new ArrayList<>()).build();
        // existing trainer
        Trainer old = Trainer.builder().id(1L).username("old").trainees(new ArrayList<>()).build();
        old.getTrainees().add(t); // old has trainee
        t.getTrainers().add(old);

        // new trainers returned by repo
        Trainer new1 = Trainer.builder().id(2L).username("n1").trainees(new ArrayList<>()).build();
        Trainer new2 = Trainer.builder().id(3L).username("n2").trainees(new ArrayList<>()).build();

        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        when(trainerRepository.findAllById(List.of(2L,3L))).thenReturn(List.of(new1, new2));
        when(traineeRepository.save(any())).thenReturn(t);

        traineeService.updateTraineeTrainers("u", List.of(2L,3L));

        // old trainer should no longer reference trainee
        assertFalse(old.getTrainees().contains(t));
        // new trainers should reference trainee
        assertTrue(new1.getTrainees().contains(t));
        assertTrue(new2.getTrainees().contains(t));
        verify(traineeRepository).save(t);
    }

    @Test
    void updateTraineeTrainers_missingTrainerIds_throwsValidation() {
        Trainee t = Trainee.builder().username("u").id(20L).trainers(new ArrayList<>()).build();
        when(traineeRepository.findByUsername("u")).thenReturn(Optional.of(t));
        // repo returns only one but requested two ids -> mismatch
        when(trainerRepository.findAllById(List.of(2L,3L))).thenReturn(List.of(Trainer.builder().id(2L).build()));

        assertThrows(ValidationException.class, () -> traineeService.updateTraineeTrainers("u", List.of(2L,3L)));
    }

    @Test
    void updateTraineeTrainers_traineeNotFound_throws() {
        when(traineeRepository.findByUsername("no")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.updateTraineeTrainers("no", List.of(1L)));
    }
}
