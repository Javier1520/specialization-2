package com.epam.gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainingTypeService;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    private List<TrainingType> trainingTypes;
    private List<TrainingTypeResponse> trainingTypeResponses;

    @BeforeEach
    void setUp() {
        TrainingType cardio = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();

        TrainingType strength = TrainingType.builder()
                .id(2L)
                .name("Strength")
                .build();

        trainingTypes = List.of(cardio, strength);

        TrainingTypeResponse cardioResponse = new TrainingTypeResponse();
        cardioResponse.setTrainingTypeId(1L);
        cardioResponse.setTrainingType("Cardio");

        TrainingTypeResponse strengthResponse = new TrainingTypeResponse();
        strengthResponse.setTrainingTypeId(2L);
        strengthResponse.setTrainingType("Strength");

        trainingTypeResponses = List.of(cardioResponse, strengthResponse);
    }

    @Test
    void getTrainingTypes_success_returnsOk() {
        // Given
        when(trainingTypeService.listAll()).thenReturn(trainingTypes);
        when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

        // When
        ResponseEntity<List<TrainingTypeResponse>> response = trainingTypeController.getTrainingTypes();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(trainingTypeService).listAll();
        verify(trainingTypeMapper).toResponseList(trainingTypes);
    }

    @Test
    void getTrainingTypes_emptyList_returnsOk() {
        // Given
        when(trainingTypeService.listAll()).thenReturn(List.of());
        when(trainingTypeMapper.toResponseList(List.of())).thenReturn(List.of());

        // When
        ResponseEntity<List<TrainingTypeResponse>> response = trainingTypeController.getTrainingTypes();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(trainingTypeService).listAll();
        verify(trainingTypeMapper).toResponseList(List.of());
    }
}

