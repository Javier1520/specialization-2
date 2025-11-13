package com.epam.gym.storage;

import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.service.UsernamePasswordGenerator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class TrainerProcessor implements RecordProcessor {
    private final TrainerRepository trainerRepository;
    private final UsernamePasswordGenerator generator;
    private final DataIDMapper idMapper;
    private long csvIdCounter = 1;

    @Autowired
    public TrainerProcessor(
            TrainerRepository trainerRepository,
            UsernamePasswordGenerator generator,
            DataIDMapper idMapper) {
        this.trainerRepository = trainerRepository;
        this.generator = generator;
        this.idMapper = idMapper;
    }

    @Override
    public String getRecordType() {
        return "Trainer";
    }

    private static final int FIRST_NAME_COLUMN = 1;
    private static final int LAST_NAME_COLUMN = 2;
    private static final int SPECIALIZATION_COLUMN = 3;
    private static final int REQUIRED_COLUMNS = 4;

    @Override
    public void process(String[] columns) {
        if (columns.length < REQUIRED_COLUMNS) {
            throw new IllegalArgumentException("Invalid number of columns for Trainer record. Required: " + REQUIRED_COLUMNS + ", Found: " + columns.length);
        }

        // Parse training type enum from name
        TrainingType.Type specialization;
        try {
            specialization = TrainingType.Type.valueOf(columns[SPECIALIZATION_COLUMN]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid training type: " + columns[SPECIALIZATION_COLUMN]);
        }

        // Create trainer using builder pattern
        Trainer trainer = Trainer.builder()
                .firstName(columns[FIRST_NAME_COLUMN])
                .lastName(columns[LAST_NAME_COLUMN])
                .specialization(specialization)
                .isActive(true)
                .build();

        // Generate username and password
        String username = generator.generateUsername(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainerRepository::existsByUsername
        );
        trainer.setUsername(username);
        trainer.setPassword(generator.generatePassword());

        // Save trainer and store ID mapping
        trainer = trainerRepository.save(trainer);
        idMapper.addMapping("Trainer", csvIdCounter++, trainer.getId());
    }
}
