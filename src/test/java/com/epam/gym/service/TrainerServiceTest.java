package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.service.impl.TrainerServiceImpl;
import com.epam.gym.util.LogUtils;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock TrainerRepository trainerRepository;
    @Mock TrainingRepository trainingRepository;
    @Mock UsernamePasswordGenerator usernamePasswordGenerator;
    @Mock LogUtils logUtils;

    @InjectMocks TrainerServiceImpl trainerService;

    private Trainer payload;

    @BeforeEach
    void setUp() {
        payload = Trainer.builder()
                .firstName("Tr")
                .lastName("Ai")
                .specialization(TrainingType.Type.CARDIO)
                .build();
    }

    @Test
    void createTrainer_success_generatesUsernameAndSaves() {
        when(usernamePasswordGenerator.generateUsername(eq("Tr"), eq("Ai"), any()))
                .thenReturn("tr.ai");
        when(usernamePasswordGenerator.generatePassword()).thenReturn("pw");
        Trainer saved = Trainer.builder().id(5L).username("tr.ai").isActive(true).build();
        when(trainerRepository.save(any())).thenReturn(saved);

        Trainer out = trainerService.createTrainer(payload);

        assertEquals(5L, out.getId());
        assertEquals("tr.ai", out.getUsername());
        verify(trainerRepository).save(any());
    }

    @Test
    void createTrainer_validation_missingSpecialization_throws() {
        Trainer bad = Trainer.builder().firstName("A").lastName("B").specialization(null).build();
        assertThrows(ValidationException.class, () -> trainerService.createTrainer(bad));
    }

    @Test
    void getByUsername_notFound_throws() {
        when(trainerRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainerService.getByUsername("x"));
    }

    @Test
    void changePassword_shortPassword_throws() {
        assertThrows(ValidationException.class, () -> trainerService.changePassword("u", "123"));
    }

    @Test
    void changePassword_userNotFound_throws() {
        when(trainerRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainerService.changePassword("u",
                "strongpass"));
    }

    @Test
    void changePassword_success_updatesAndSaves() {
        Trainer t = Trainer.builder().username("u").password("old4567890").build();
        when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));

        trainerService.changePassword("u", "newstrong_gt_10_chars");

        assertEquals("newstrong_gt_10_chars", t.getPassword());
        verify(trainerRepository).save(t);
    }

    @Test
    void updateTrainer_notFound_throws() {
        when(trainerRepository.findByUsernameWithTrainees("no")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainerService.updateTrainer("no", new Trainer()));
    }

    @Test
    void updateTrainer_whenTrainerExists_updatesAndReturnsTrainer() {
        // Arrange
        Trainer existing = Trainer.builder()
                .id(1L)
                .firstName("Old")
                .lastName("Name")
                .specialization(TrainingType.Type.STRENGTH)
                .isActive(true)
                .build();

        Trainer update = Trainer.builder()
                .firstName("New")
                .lastName("Name")
                .specialization(TrainingType.Type.STRENGTH)
                .build();

        when(trainerRepository.findByUsernameWithTrainees("trainer.user")).thenReturn(Optional.of(existing));
        when(trainerRepository.save(existing)).thenReturn(existing);

        // Act
        Trainer result = trainerService.updateTrainer("trainer.user", update);

        // Assert
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(trainerRepository).save(existing);
    }

    @Test
    void setActive_sameState_throwsValidation() {
        Trainer t = Trainer.builder().username("u").isActive(true).build();
        when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));
        assertThrows(ValidationException.class, () -> trainerService.setActive("u", true));
    }

    @Test
    void setActive_success_saves() {
        Trainer t = Trainer.builder().username("u").isActive(false).build();
        when(trainerRepository.findByUsername("u")).thenReturn(Optional.of(t));

        trainerService.setActive("u", true);

        assertTrue(t.getIsActive());
        verify(trainerRepository).save(t);
    }

    @Test
    void getTrainerTrainings_callsRepositoryWithConvertedDates() {
        Training tr = Training.builder().id(1L).build();
        when(trainingRepository.findByTrainerUsernameAndCriteria(eq("t1"), any(Date.class), any(Date.class),
                isNull()))
                .thenReturn(List.of(tr));

        com.epam.gym.dto.request.TrainerTrainingFilterRequest filter =
                new com.epam.gym.dto.request.TrainerTrainingFilterRequest(new Date(0), new Date(), null);
        List<Training> out = trainerService.getTrainerTrainings("t1", filter);
        assertEquals(1, out.size());
        verify(trainingRepository).findByTrainerUsernameAndCriteria(eq("t1"), any(Date.class), any(Date.class),
                isNull());
    }
}
