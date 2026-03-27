package com.epam.gym.cucumber;

import com.epam.gym.controller.AuthController;
import com.epam.gym.controller.TraineeController;
import com.epam.gym.controller.TrainerController;
import com.epam.gym.controller.TrainingController;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.security.JwtService;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.impl.TrainingServiceImpl;
import com.epam.gym.service.workload.WorkloadService;
import com.epam.gym.client.WorkloadClient;
import com.epam.gym.util.LogUtils;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;

@CucumberContextConfiguration
@WebMvcTest(
        controllers = {
            TrainingController.class,
            TraineeController.class,
            TrainerController.class,
            AuthController.class
        },
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import({TrainingServiceImpl.class, WorkloadService.class})
public class CucumberSpringConfiguration {
    @MockBean TrainingRepository trainingRepository;
    @MockBean TraineeRepository traineeRepository;
    @MockBean TrainerRepository trainerRepository;
    @MockBean TrainingMapper trainingMapper;
    @MockBean WorkloadClient workloadClient;
    @MockBean JmsTemplate jmsTemplate;
    @MockBean LogUtils logUtils;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean TraineeService traineeService;
    @MockBean TrainerService trainerService;
    @MockBean AuthenticationService authenticationService;
}

