package com.epam.gym.storage;

import com.epam.gym.model.Trainee;
import com.epam.gym.service.UsernamePasswordGenerator;
import com.epam.gym.repository.TraineeRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.text.SimpleDateFormat;
import java.text.ParseException;

@Component
public class TraineeProcessor implements RecordProcessor {
    private final TraineeRepository traineeRepository;
    private final UsernamePasswordGenerator generator;
    private final DataIDMapper idMapper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private long csvIdCounter = 1;

    @Autowired
    public TraineeProcessor(TraineeRepository traineeRepository, UsernamePasswordGenerator generator, DataIDMapper idMapper) {
        this.traineeRepository = traineeRepository;
        this.generator = generator;
        this.idMapper = idMapper;
    }

    @Override
    public String getRecordType() {
        return "Trainee";
    }

    private static final int FIRST_NAME_COLUMN = 1;
    private static final int LAST_NAME_COLUMN = 2;
    private static final int DATE_OF_BIRTH_COLUMN = 3;
    private static final int ADDRESS_COLUMN = 4;
    private static final int REQUIRED_COLUMNS = 5;

    @Override
    public void process(String[] columns) {
        if (columns.length < REQUIRED_COLUMNS) {
            throw new IllegalArgumentException("Invalid number of columns for Trainee record. Required: " + REQUIRED_COLUMNS + ", Found: " + columns.length);
        }

        try {
            Trainee trainee = Trainee.builder()
                .firstName(columns[FIRST_NAME_COLUMN])
                .lastName(columns[LAST_NAME_COLUMN])
                .dateOfBirth(dateFormat.parse(columns[DATE_OF_BIRTH_COLUMN]))
                .address(columns[ADDRESS_COLUMN])
                .isActive(true)
                .build();

            String username = generator.generateUsername(
                trainee.getFirstName(),
                trainee.getLastName(),
                traineeRepository::existsByUsername
            );
            trainee.setUsername(username);
            trainee.setPassword(generator.generatePassword());

            trainee = traineeRepository.save(trainee);
            idMapper.addMapping("Trainee", csvIdCounter++, trainee.getId());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format in trainee record: " + columns[DATE_OF_BIRTH_COLUMN], e);
        }
    }
}