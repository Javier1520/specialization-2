package com.epam.gym.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AppHealthIndicator implements HealthIndicator {

  private static final String APP = "app";

  @Override
  public Health health() {
    return Health.up().withDetail(APP, "Running").build();
  }
}
