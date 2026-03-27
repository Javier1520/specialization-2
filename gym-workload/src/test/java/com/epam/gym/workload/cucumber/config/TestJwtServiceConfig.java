package com.epam.gym.workload.cucumber.config;

import com.epam.gym.workload.security.JwtService;
import java.util.Date;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestJwtServiceConfig {

    @Bean
    @Primary
    JwtService testJwtService() {
        return new JwtService() {
            @Override
            public String extractUsername(String token) {
                return "cucumber-user";
            }

            @Override
            public Date extractExpiration(String token) {
                return new Date(System.currentTimeMillis() + 60_000);
            }

            @Override
            public Boolean validateToken(String token, String username) {
                return true;
            }
        };
    }
}

