package com.epam.gym.client;

import com.epam.gym.dto.workload.TrainerWorkloadDto;
import com.epam.gym.dto.workload.TrainingHoursDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gym-workload")
public interface WorkloadClient {

    @GetMapping("/api/workload/{username}")
    TrainerWorkloadDto getWorkload(@PathVariable String username);

    @GetMapping("/api/workload/hours")
    TrainingHoursDto getTrainingHours(
            @RequestParam("username") String username,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month);
}
