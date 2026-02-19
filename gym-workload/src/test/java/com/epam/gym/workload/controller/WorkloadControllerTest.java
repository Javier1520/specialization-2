package com.epam.gym.workload.controller;

import com.epam.gym.workload.dto.ActionType;
import com.epam.gym.workload.dto.TrainerWorkloadDto;
import com.epam.gym.workload.dto.TrainingHoursDto;
import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.security.JwtService;
import com.epam.gym.workload.service.WorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkloadController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simplicity in unit tests
class WorkloadControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private WorkloadService workloadService;

    @MockBean private JwtService jwtService;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void addWorkload_success() throws Exception {
        WorkloadRequest request =
                new WorkloadRequest(
                        "trainer1", "John", "Doe", true, LocalDate.now(), 60, ActionType.ADD);

        doNothing().when(workloadService).addWorkload(any(WorkloadRequest.class));

        mockMvc.perform(
                        post("/api/workload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteWorkload_success() throws Exception {
        WorkloadRequest request =
                new WorkloadRequest(
                        "trainer1", "John", "Doe", true, LocalDate.now(), 60, ActionType.DELETE);

        doNothing().when(workloadService).deleteWorkload(any(WorkloadRequest.class));

        mockMvc.perform(
                        delete("/api/workload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getWorkload_success() throws Exception {
        TrainerWorkloadDto dto = new TrainerWorkloadDto("trainer1", "John", "Doe", true, List.of());

        when(workloadService.getWorkload("trainer1")).thenReturn(dto);

        mockMvc.perform(get("/api/workload/trainer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("trainer1"));
    }

    @Test
    @WithMockUser
    void getTrainingHours_success() throws Exception {
        TrainingHoursDto dto = new TrainingHoursDto("trainer1", 2025, 1, 120L);

        when(workloadService.getTrainingHours("trainer1", 2025, 1)).thenReturn(dto);

        mockMvc.perform(
                        get("/api/workload/hours")
                                .param("username", "trainer1")
                                .param("year", "2025")
                                .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingHours").value(120));
    }
}
