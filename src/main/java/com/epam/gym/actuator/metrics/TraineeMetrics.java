package com.epam.gym.actuator.metrics;

import com.epam.gym.repository.TraineeRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TraineeMetrics {

    public static final String GYM_TRAINEES_COUNT = "gym.trainees.count";

    public TraineeMetrics(MeterRegistry registry, TraineeRepository traineeRepository) {
        Gauge.builder(GYM_TRAINEES_COUNT, traineeRepository, TraineeRepository::count)
                .description("Number of trainees")
                .register(registry);
    }
}
