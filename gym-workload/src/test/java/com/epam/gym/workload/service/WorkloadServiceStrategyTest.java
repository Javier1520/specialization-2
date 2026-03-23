package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.entity.TrainerWorkload;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadMongoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceStrategyTest {

    @Mock TrainerWorkloadMongoRepository repository;
    @Mock WorkloadMapper mapper;

    @InjectMocks
    WorkloadService workloadService;

    @Test
    void addWorkload_addsDuration() {
        TrainerWorkload trainer =
                TrainerWorkload.builder().username("t1").years(new ArrayList<>()).build();
        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));

        AddWorkloadRequest request =
                new AddWorkloadRequest(
                        "t1", "F", "L", true, LocalDate.of(2025, 1, 10), 60, ActionType.ADD);

        workloadService.addWorkload(request);

        Mockito.verify(repository).save(trainer);

        Optional<TrainerWorkload.YearSummary> year = trainer.getYears().stream().filter(y -> y.getYearNumber().equals(2025)).findFirst();
        assertEquals(true, year.isPresent());
        Optional<TrainerWorkload.MonthSummary> month = year.get().getMonths().stream().filter(m -> m.getMonthNumber().equals(1)).findFirst();
        assertEquals(true, month.isPresent());
        assertEquals(60L, month.get().getTrainingDuration());
    }

    @Test
    void deleteWorkload_removesDuration() {
        TrainerWorkload.MonthSummary monthSummary = TrainerWorkload.MonthSummary.builder().monthNumber(1).trainingDuration(100L).build();
        TrainerWorkload.YearSummary yearSummary = TrainerWorkload.YearSummary.builder().yearNumber(2025).months(new ArrayList<>(List.of(monthSummary))).build();
        TrainerWorkload trainer =
                TrainerWorkload.builder().username("t1").years(new ArrayList<>(List.of(yearSummary))).build();

        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));

        DeleteWorkloadRequest request =
                new DeleteWorkloadRequest(
                        "t1", "F", "L", true, LocalDate.of(2025, 1, 10), 60, ActionType.DELETE);

        workloadService.deleteWorkload(request);

        Mockito.verify(repository).save(trainer);
        assertEquals(40L, monthSummary.getTrainingDuration());
    }

    @Test
    void getWorkload_callsRepositoryAndMapper() {
        TrainerWorkload trainer = new TrainerWorkload();
        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));
        when(mapper.toDto(trainer))
                .thenReturn(
                        new TrainerWorkloadDto(
                                "t1", "F", "L", true, List.of()));

        workloadService.getWorkload("t1");

        Mockito.verify(repository).findByUsername("t1");
        Mockito.verify(mapper).toDto(trainer);
    }

    @Test
    void getWorkload_notFound_throws() {
        when(repository.findByUsername("t1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workloadService.getWorkload("t1"));
    }

    @Test
    void getTrainingHours_returnsCorrectHours() {
        when(repository.findTrainingHours("t1", 2025, 1)).thenReturn(Optional.of(120L));

        var result = workloadService.getTrainingHours("t1", 2025, 1);

        assertEquals(120L, result.trainingHours());
    }

    @Test
    void getTrainingHours_noMatch_returnsZero() {
        when(repository.findTrainingHours("t1", 2025, 1)).thenReturn(Optional.empty());

        var result = workloadService.getTrainingHours("t1", 2025, 1);

        assertEquals(0L, result.trainingHours());
    }

    @Test
    void addWorkload_createsNewTrainer() {
        when(repository.findByUsername("newTrainer")).thenReturn(Optional.empty());

        AddWorkloadRequest request =
                new AddWorkloadRequest(
                        "newTrainer",
                        "New",
                        "User",
                        true,
                        LocalDate.of(2025, 1, 10),
                        60,
                        ActionType.ADD);

        workloadService.addWorkload(request);

        Mockito.verify(repository).save(any(TrainerWorkload.class));
    }
}
