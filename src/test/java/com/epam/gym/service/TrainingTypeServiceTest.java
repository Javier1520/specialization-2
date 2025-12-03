package com.epam.gym.service;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock TrainingTypeRepository repo;
    @Mock TrainingTypeMapper mapper;
    @InjectMocks TrainingTypeServiceImpl service;

    @Test
    void listAll_returnsAllTrainingTypeResponses() {
        TrainingType type1 = new TrainingType(1L, "CARDIO", null, null);
        TrainingType type2 = new TrainingType(2L, "STRENGTH", null, null);
        TrainingTypeResponse response1 = new TrainingTypeResponse(1L, "CARDIO");
        TrainingTypeResponse response2 = new TrainingTypeResponse(2L, "STRENGTH");

        when(repo.findAll()).thenReturn(List.of(type1, type2));
        when(mapper.toResponse(type1)).thenReturn(response1);
        when(mapper.toResponse(type2)).thenReturn(response2);

        List<TrainingTypeResponse> out = service.listAll();
        assertEquals(2, out.size());
        assertEquals("CARDIO", out.get(0).trainingType());
        assertEquals("STRENGTH", out.get(1).trainingType());
    }

    @Test
    void getById_throwsNotFoundExceptionWhenNotFound() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(
                NotFoundException.class,
                () -> service.getById(5L)
        );
        verify(repo).findById(5L);
    }

    @Test
    void getById_returnsTrainingTypeResponseWhenFound() {
        TrainingType t = new TrainingType(2L, "Y", null, null);
        TrainingTypeResponse response = new TrainingTypeResponse(2L, "Y");
        when(repo.findById(2L)).thenReturn(Optional.of(t));
        when(mapper.toResponse(t)).thenReturn(response);
        TrainingTypeResponse result = service.getById(2L);
        assertSame(response, result);
        verify(repo).findById(2L);
        verify(mapper).toResponse(t);
    }
}
