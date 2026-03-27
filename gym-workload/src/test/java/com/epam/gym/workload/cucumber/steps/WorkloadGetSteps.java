package com.epam.gym.workload.cucumber.steps;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.gym.workload.repository.TrainerWorkloadMongoRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;

public class WorkloadGetSteps {

    @Autowired private MockMvc mockMvc;

    @Autowired private TrainerWorkloadMongoRepository repository;

    private ResultActions lastResult;

    @Given("no trainer exists with username {string}")
    public void noTrainerExistsWithUsername(String username) {
        when(repository.findByUsername(username)).thenReturn(Optional.empty());
    }

    @When("I GET Workload service workload for username {string} without token")
    public void iGetWorkloadServiceWorkloadWithoutToken(String username) throws Exception {
        lastResult = mockMvc.perform(get("/api/workload/{username}", username));
    }

    @When("I GET Workload service workload for username {string} with a valid token")
    public void iGetWorkloadServiceWorkloadWithToken(String username) throws Exception {
        lastResult =
                mockMvc.perform(
                        get("/api/workload/{username}", username)
                                .header("Authorization", "Bearer test-token"));
    }

    @Then("the Workload response status should be {int}")
    public void theWorkloadResponseStatusShouldBe(int statusCode) throws Exception {
        lastResult.andExpect(status().is(statusCode));
    }

    @Then("the Workload error message should contain {string}")
    public void theWorkloadErrorMessageShouldContain(String text) throws Exception {
        lastResult.andExpect(jsonPath("$.error", containsString(text)));
    }
}

