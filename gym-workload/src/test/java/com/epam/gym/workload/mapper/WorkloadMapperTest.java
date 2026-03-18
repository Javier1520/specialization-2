package com.epam.gym.workload.mapper;

import com.epam.gym.workload.dto.MonthSummaryDto;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.YearSummaryDto;
import com.epam.gym.workload.entity.TrainerWorkload;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadMapperTest {

    private final WorkloadMapper mapper = Mappers.getMapper(WorkloadMapper.class);

    @Test
    void toDto_trainerEntity_mapsCorrectly() {
        TrainerWorkload.MonthSummary month = TrainerWorkload.MonthSummary.builder().monthNumber(1).trainingDuration(120L).build();
        TrainerWorkload.YearSummary year = TrainerWorkload.YearSummary.builder().yearNumber(2025).months(List.of(month)).build();
        TrainerWorkload trainer =
                TrainerWorkload.builder()
                        .username("trainer1")
                        .firstName("John")
                        .lastName("Doe")
                        .isActive(true)
                        .years(List.of(year))
                        .build();

        TrainerWorkloadDto dto = mapper.toDto(trainer);

        assertNotNull(dto);
        assertEquals("trainer1", dto.username());
        assertEquals("John", dto.firstName());
        assertEquals("Doe", dto.lastName());
        assertTrue(dto.status());
        assertEquals(1, dto.years().size());
        assertEquals(2025, dto.years().get(0).yearNumber());
    }

    @Test
    void toDto_yearEntity_mapsCorrectly() {
        TrainerWorkload.MonthSummary month = TrainerWorkload.MonthSummary.builder().monthNumber(1).trainingDuration(120L).build();
        TrainerWorkload.YearSummary year = TrainerWorkload.YearSummary.builder().yearNumber(2025).months(List.of(month)).build();

        YearSummaryDto dto = mapper.toDto(year);

        assertNotNull(dto);
        assertEquals(2025, dto.yearNumber());
        assertEquals(1, dto.months().size());
    }

    @Test
    void toDto_monthEntity_mapsCorrectly() {
        TrainerWorkload.MonthSummary month = TrainerWorkload.MonthSummary.builder().monthNumber(1).trainingDuration(120L).build();

        MonthSummaryDto dto = mapper.toDto(month);

        assertNotNull(dto);
        assertEquals(1, dto.monthNumber());
        assertEquals(120L, dto.trainingDuration());
    }

    @Test
    void toDto_nullInputs_returnsNull() {
        assertNull(mapper.toDto((TrainerWorkload) null));
        assertNull(mapper.toDto((TrainerWorkload.YearSummary) null));
        assertNull(mapper.toDto((TrainerWorkload.MonthSummary) null));
    }
}
