package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring Container Entry Point for Gym CRM System
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        log.info("Starting Gym CRM Application...");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            log.info("Spring context initialized successfully");

            // Get the facade from Spring container
            GymFacade gymFacade = context.getBean(GymFacade.class);
            log.info("GymFacade bean retrieved: {}", gymFacade.getClass().getSimpleName());

            log.info("Gym CRM Application completed successfully");
        } catch (Exception e) {
            log.error("Error running Gym CRM Application", e);
        }
    }

}
