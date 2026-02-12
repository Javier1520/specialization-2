package com.epam.gym.workload.mapper;

import com.epam.gym.workload.dto.MonthSummaryDto;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.YearSummaryDto;
import com.epam.gym.workload.entity.MonthEntity;
import com.epam.gym.workload.entity.TrainerEntity;
import com.epam.gym.workload.entity.YearEntity;
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
        MonthEntity month = MonthEntity.builder().monthNumber(1).trainingDuration(120).build();
        YearEntity year = YearEntity.builder().yearNumber(2025).months(List.of(month)).build();
        TrainerEntity trainer =
                TrainerEntity.builder()
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
        MonthEntity month = MonthEntity.builder().monthNumber(1).trainingDuration(120).build();
        YearEntity year = YearEntity.builder().yearNumber(2025).months(List.of(month)).build();

        YearSummaryDto dto = mapper.toDto(year);

        assertNotNull(dto);
        assertEquals(2025, dto.yearNumber());
        assertEquals(1, dto.months().size());
    }

    @Test
    void toDto_monthEntity_mapsCorrectly() {
        MonthEntity month = MonthEntity.builder().monthNumber(1).trainingDuration(120).build();

        MonthSummaryDto dto = mapper.toDto(month);

        assertNotNull(dto);
        assertEquals(1, dto.monthNumber());
        assertEquals(120L, dto.trainingDuration());
    }

    @Test
    void toDto_nullInputs_returnsNull() {
        assertNull(mapper.toDto((TrainerEntity) null));
        assertNull(mapper.toDto((YearEntity) null));
        assertNull(mapper.toDto((MonthEntity) null));
    }
}
