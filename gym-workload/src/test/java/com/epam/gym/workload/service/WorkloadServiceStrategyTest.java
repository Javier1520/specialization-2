package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.AddWorkloadRequest;
import com.epam.gym.workload.dto.DeleteWorkloadRequest;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
import com.epam.gym.workload.mapper.WorkloadMapper;
import com.epam.gym.workload.repository.TrainerWorkloadRepository;
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

    @Mock TrainerWorkloadRepository repository;
    @Mock WorkloadMapper mapper;

    @InjectMocks
    WorkloadService workloadService;

    @Test
    void addWorkload_addsDuration() {
        TrainerEntity trainer =
                TrainerEntity.builder().username("t1").years(new ArrayList<>()).build();
        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));

        AddWorkloadRequest request =
                new AddWorkloadRequest(
                        "t1", "F", "L", true, LocalDate.of(2025, 1, 10), 60, ActionType.ADD);

        workloadService.addWorkload(request);

        Mockito.verify(repository).save(trainer);

        Optional<YearEntity> year = trainer.getYears().stream().filter(y -> y.getYearNumber() == 2025).findFirst();
        assertEquals(true, year.isPresent());
        Optional<MonthEntity> month = year.get().getMonths().stream().filter(m -> m.getMonthNumber() == 1).findFirst();
        assertEquals(true, month.isPresent());
        assertEquals(60L, month.get().getTrainingDuration());
    }

    @Test
    void deleteWorkload_removesDuration() {
        MonthEntity monthEntity = MonthEntity.builder().monthNumber(1).trainingDuration(100L).build();
        YearEntity yearEntity = YearEntity.builder().yearNumber(2025).months(List.of(monthEntity)).build();
        TrainerEntity trainer =
                TrainerEntity.builder().username("t1").years(List.of(yearEntity)).build();
        monthEntity.setYear(yearEntity);

        // Need mutable list for months if our logic adds to it, but here we read
        // The service logic calls resolveMonthEntity which might try to add to year's month list if not found
        // Let's make the list mutable just in case
        yearEntity.setMonths(new ArrayList<>(List.of(monthEntity)));
        trainer.setYears(new ArrayList<>(List.of(yearEntity)));

        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));

        DeleteWorkloadRequest request =
                new DeleteWorkloadRequest(
                        "t1", "F", "L", true, LocalDate.of(2025, 1, 10), 60, ActionType.DELETE);

        workloadService.deleteWorkload(request);

        Mockito.verify(repository).save(trainer);
        assertEquals(40L, monthEntity.getTrainingDuration());
    }

    @Test
    void getWorkload_callsRepositoryAndMapper() {
        TrainerEntity trainer = new TrainerEntity();
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
        MonthEntity month = MonthEntity.builder().monthNumber(1).trainingDuration(120).build();
        YearEntity year = YearEntity.builder().yearNumber(2025).months(List.of(month)).build();
        TrainerEntity trainer = TrainerEntity.builder().years(List.of(year)).build();

        when(repository.findByUsername("t1")).thenReturn(Optional.of(trainer));

        var result = workloadService.getTrainingHours("t1", 2025, 1);

        assertEquals(120L, result.trainingHours());
    }

    @Test
    void getTrainingHours_notFound_throws() {
        when(repository.findByUsername("t1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workloadService.getTrainingHours("t1", 2025, 1));
    }

    @Test
    void addWorkload_createsNewTrainer() {
        when(repository.findByUsername("newTrainer")).thenReturn(Optional.empty());
        // Mock save to return the argument or do nothing
        // Implicitly verified by verify(repository).save(any())

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

        Mockito.verify(repository).save(any(TrainerEntity.class));
    }
}
