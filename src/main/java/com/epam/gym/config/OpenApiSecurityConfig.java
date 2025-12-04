package com.epam.gym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiSecurityConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(getInfo())
            .components(getComponents())
            .addSecurityItem(getSecurityItem()
            );
    }

    private Info getInfo() {
        return new Info()
                .title("Gym CRM")
                .description("REST API for managing gym trainers, trainees and training sessions")
                .version("2.5.0")
                .contact(new Contact()
                        .name("Javier Guerrero")
                        .email("javier.guerrero@email.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication")
                );
    }

    private SecurityRequirement getSecurityItem() {
        return new SecurityRequirement()
                .addList("bearer-jwt");
    }
}