package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.service.impl.TrainingTypeServiceImpl;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock TrainingTypeRepository repo;
    @InjectMocks TrainingTypeServiceImpl service;

    @Test
    void listAll_callsRepoAndReturns() {
        TrainingType t1 = new TrainingType(1L, "Cardio", null, null);
        when(repo.findAll()).thenReturn(List.of(t1));

        List<TrainingType> out = service.listAll();
        assertEquals(1, out.size());
        verify(repo).findAll();
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
    void getById_returnsEntityWhenFound() {
        TrainingType t = new TrainingType(2L, "Y", null, null);
        when(repo.findById(2L)).thenReturn(Optional.of(t));
        assertSame(t, service.getById(2L));
    }
}

