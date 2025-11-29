package com.epam.gym.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("appUpHealth")
public class AppUpHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("app", "Running").build();
    }
}