package com.epam.gym.storage;

import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class TrainingTypeProcessor implements RecordProcessor {
    private final TrainingTypeRepository trainingTypeRepository;

    private static final int TYPE_NAME_COLUMN = 1;
    private static final int REQUIRED_COLUMNS = 2; // type + 1 field

    @Autowired
    public TrainingTypeProcessor(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public String getRecordType() {
        return "TrainingType";
    }

    @Override
    public void process(String[] columns) {
        if (columns.length < REQUIRED_COLUMNS) {
            throw new IllegalArgumentException("Invalid number of columns for TrainingType record. Required: " + REQUIRED_COLUMNS + ", Found: " + columns.length);
        }

        TrainingType type = TrainingType.builder()
                .name(columns[TYPE_NAME_COLUMN])
                .build();

        trainingTypeRepository.save(type);
    }
}