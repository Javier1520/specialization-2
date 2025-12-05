package com.epam.gym.actuator.metrics;

import com.epam.gym.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TrainerMetrics {

  private static final String GYM_TRAINERS_COUNT = "gym.trainers.count";

  public TrainerMetrics(MeterRegistry registry, TrainerRepository trainerRepository) {
    Gauge.builder(GYM_TRAINERS_COUNT, trainerRepository, TrainerRepository::count)
        .description("Number of trainers")
        .register(registry);
  }
}
