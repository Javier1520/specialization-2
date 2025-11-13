package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

            // Demonstrate the system working
            demonstrateSystem(gymFacade);

            log.info("Gym CRM Application completed successfully");
        } catch (Exception e) {
            log.error("Error running Gym CRM Application", e);
        }
    }

    private static void demonstrateSystem(GymFacade gymFacade) {
        log.info("=== Demonstrating Gym CRM System ===");

        List<TrainingType> types = gymFacade.listTrainingTypes();
        log.info("Available training types: {}", types.stream().map(Object::toString).collect(Collectors.joining(", ")));

        log.info("=== Creating Trainer profile ===");
        Trainer trainerPayload = Trainer.builder()
                .firstName("Alice")
                .lastName("Coach")
                .specialization(TrainingType.Type.CARDIO)
                .build();

        Trainer createdTrainer = gymFacade.createTrainer(trainerPayload);
        log.info("Created trainer: id={} username={}", createdTrainer.getId(), createdTrainer.getUsername());

        log.info("=== Creating Trainee profile ===");
        Trainee traineePayload = Trainee.builder()
                .firstName("Bob")
                .lastName("Student")
                .dateOfBirth(Date.from(LocalDate.of(1995, 5, 15).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .address("123 Main St")
                .build();

        Trainee createdTrainee = gymFacade.createTrainee(traineePayload);
        log.info("Created trainee: id={} username={} password={}",
                createdTrainee.getId(), createdTrainee.getUsername(), createdTrainee.getPassword());

        log.info("=== Selecting trainer by username ===");
        Trainer foundTrainer = gymFacade.getTrainerByUsername(createdTrainer.getUsername());
        log.info("Found trainer: {} {} username={}", foundTrainer.getFirstName(), foundTrainer.getLastName(), foundTrainer.getUsername());

        log.info("=== Selecting trainee by username ===");
        Trainee foundTrainee = gymFacade.getTraineeByUsername(createdTrainee.getUsername());
        log.info("Found trainee: {} {} username={}", foundTrainee.getFirstName(), foundTrainee.getLastName(), foundTrainee.getUsername());

        log.info("=== Changing trainee password ===");
        gymFacade.changeTraineePassword(createdTrainee.getUsername(), "newSecret1");
        log.info("Trainee password changed for {}", createdTrainee.getUsername());

        log.info("=== Changing trainer password ===");
        gymFacade.changeTrainerPassword(createdTrainer.getUsername(), "coachSecre");
        log.info("Trainer password changed for {}", createdTrainer.getUsername());

        log.info("=== Updating trainer profile ===");
        Trainer trainerUpdate = Trainer.builder()
                .firstName(foundTrainer.getFirstName())
                .lastName("UpdatedCoach")
                .specialization(foundTrainer.getSpecialization()) // keep same
                .build();
        Trainer updatedTrainer = gymFacade.updateTrainer(foundTrainer.getUsername(), trainerUpdate);
        log.info("Updated trainer: {} {} username={}", updatedTrainer.getFirstName(), updatedTrainer.getLastName(), updatedTrainer.getUsername());

        log.info("=== Updating trainee profile ===");
        Trainee traineeUpdate = Trainee.builder()
                .firstName(foundTrainee.getFirstName())
                .lastName(foundTrainee.getLastName())
                .address("456 Other St")
                .dateOfBirth(foundTrainee.getDateOfBirth())
                .build();
        Trainee updatedTrainee = gymFacade.updateTrainee(foundTrainee.getUsername(), traineeUpdate);
        log.info("Updated trainee: {} {} address={}", updatedTrainee.getFirstName(), updatedTrainee.getLastName(), updatedTrainee.getAddress());

        log.info("=== Deactivating trainee ===");
        try {
            gymFacade.setTraineeActive(foundTrainee.getUsername(), false);
            log.info("Trainee deactivated: {}", foundTrainee.getUsername());
        } catch (Exception e) {
            log.warn("Could not change active state for trainee: {}", e.getMessage());
        }


        log.info("=== Deactivating trainer ===");
        try {
            gymFacade.setTrainerActive(foundTrainer.getUsername(), false);
            log.info("Trainer deactivated: {}", foundTrainer.getUsername());
        } catch (Exception e) {
            log.warn("Could not change active state for trainer: {}", e.getMessage());
        }

        log.info("=== Adding a Training ===");
        Training trainingPayload = Training.builder()
                .name("Intro Session")
                .date(new Date())
                .duration(60) // minutes
                .specialization(TrainingType.Type.PILATES)
                .trainee(Trainee.builder().username(createdTrainee.getUsername()).build())
                .trainer(Trainer.builder().username(createdTrainer.getUsername()).build())
                .build();

        Training createdTraining = gymFacade.createTraining(trainingPayload);
        log.info("Created training: id={} name={} date={} duration={}",
                createdTraining.getId(), createdTraining.getName(), createdTraining.getDate(), createdTraining.getDuration());

        log.info("=== Query trainee trainings ===");
        List<Training> traineeTrainings = gymFacade.getTraineeTrainings(createdTrainee.getUsername(),
                LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), createdTrainer.getUsername(), TrainingType.Type.CARDIO);
        log.info("Found {} trainings for trainee {}", traineeTrainings.size(), createdTrainee.getUsername());

        log.info("=== Query trainer trainings ===");
        List<Training> trainerTrainings = gymFacade.getTrainerTrainings(createdTrainer.getUsername(),
                LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), createdTrainee.getUsername());
        log.info("Found {} trainings for trainer {}", trainerTrainings.size(), createdTrainer.getUsername());

        log.info("=== Find unassigned trainers for trainee ===");
        List<Trainer> notAssigned = gymFacade.getTrainersNotAssignedToTrainee(createdTrainee.getUsername());
        log.info("Found {} trainers not assigned to trainee {}", notAssigned.size(), createdTrainee.getUsername());

        log.info("=== Update trainee's trainers list ===");
        List<Long> trainerIds = List.of(createdTrainer.getId());
        gymFacade.updateTraineeTrainers(createdTrainee.getUsername(), trainerIds);
        log.info("Trainee {} trainers updated to {}", createdTrainee.getUsername(), trainerIds);

        log.info("=== Deleting trainee ===");
        try {
            gymFacade.deleteTraineeByUsername(createdTrainee.getUsername());
            log.info("Deleted trainee {}", createdTrainee.getUsername());
        } catch (Exception e) {
            log.warn("Failed to delete trainee: {}", e.getMessage());
        }

        log.info("=== System demonstration completed ===");
    }
}
