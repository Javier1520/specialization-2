package com.epam.gym.storage;

import com.epam.gym.model.Training;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class TrainingProcessor implements RecordProcessor {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final DataIDMapper idMapper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    public TrainingProcessor(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            DataIDMapper idMapper) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.idMapper = idMapper;
    }

    @Override
    public String getRecordType() {
        return "Training";
    }

    private static final int TRAINEE_ID_COLUMN = 1;
    private static final int TRAINER_ID_COLUMN = 2;
    private static final int TRAINING_NAME_COLUMN = 3;
    private static final int TRAINING_DATE_COLUMN = 5;
    private static final int TRAINING_DURATION_COLUMN = 6;
    private static final int REQUIRED_COLUMNS = 7;

    @Override
    public void process(String[] columns) {
        if (columns.length < REQUIRED_COLUMNS) {
            throw new IllegalArgumentException("Invalid number of columns for Training record. Required: " + REQUIRED_COLUMNS + ", Found: " + columns.length);
        }

        try {
            Long csvTraineeId = Long.parseLong(columns[TRAINEE_ID_COLUMN]);
            Long csvTrainerId = Long.parseLong(columns[TRAINER_ID_COLUMN]);

            // Map CSV IDs to actual database IDs
            Long traineeId = idMapper.getMappedId("Trainee", csvTraineeId);
            Long trainerId = idMapper.getMappedId("Trainer", csvTrainerId);

            Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with ID: " + traineeId));

            Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + trainerId));

            Training training = Training.builder()
                .name(columns[TRAINING_NAME_COLUMN])
                .date(dateFormat.parse(columns[TRAINING_DATE_COLUMN]))
                .duration(Integer.parseInt(columns[TRAINING_DURATION_COLUMN]))
                .trainee(trainee)
                .trainer(trainer)
                .specialization(trainer.getSpecialization()) // Set specialization from trainer
                .build();

            trainingRepository.save(training);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format in training record: " + columns[TRAINING_DATE_COLUMN], e);
        }
    }
}