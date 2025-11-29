package com.epam.gym.actuator.metrics;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    public GymMetrics(MeterRegistry registry, TrainerRepository trainerRepository, TraineeRepository traineeRepository) {
        Gauge.builder("gym.trainers.count", trainerRepository, TrainerRepository::count)
                .description("Number of trainers")
                .register(registry);

        Gauge.builder("gym.trainees.count", traineeRepository, TraineeRepository::count)
                .description("Number of trainees")
                .register(registry);
    }
}
