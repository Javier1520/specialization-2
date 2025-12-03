package com.epam.gym.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final String DATABASE = "databaseState";

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return Health.up().withDetail(DATABASE, "Available").build();
        } catch (Exception e) {
            return Health.down().withDetail(DATABASE, "Not Available").withException(e).build();
        }
    }
}
