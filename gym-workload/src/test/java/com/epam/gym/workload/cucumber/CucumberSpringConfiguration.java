package com.epam.gym.workload.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.epam.gym.workload.repository.TrainerWorkloadMongoRepository;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(com.epam.gym.workload.cucumber.config.TestJwtServiceConfig.class)
public class CucumberSpringConfiguration {

    @MockBean TrainerWorkloadMongoRepository repository;
}

