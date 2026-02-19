package com.epam.gym.workload.config;

import com.epam.gym.workload.security.JwtAuthenticationFilter;
import com.epam.gym.workload.security.JwtService;
import com.epam.gym.workload.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;

    @MockBean private WorkloadService workloadService;

    @Test
    void protectedEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/workload"))
                .andExpect(status().isForbidden());
    }
}
