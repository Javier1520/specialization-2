package com.epam.gym.facade;

import com.epam.gym.dto.request.TrainerTrainingFilterRequest;
import com.epam.gym.dto.request.TrainingFilterRequest;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class GymFacade {

	private final TraineeService traineeService;
	private final TrainerService trainerService;
	private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

	public GymFacade(TraineeService traineeService,
	                 TrainerService trainerService,
	                 TrainingService trainingService,
                     TrainingTypeService trainingTypeService) {
		this.traineeService = traineeService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
	}

	// --- Trainee operations ---
	public Trainee createTrainee(Trainee t) { return traineeService.createTrainee(t); }
	public Trainee updateTrainee(String username, Trainee t) { return traineeService.updateTrainee(username, t); }
	public void changeTraineePassword(String username, String newPassword) { traineeService.changePassword(username, newPassword); }
	public Trainee getTraineeByUsername(String username) { return traineeService.getByUsername(username); }
	public void setTraineeActive(String username, boolean active) { traineeService.setActive(username, active); }
	public void deleteTraineeByUsername(String username) { traineeService.deleteByUsername(username); }

	// trainees trainings & trainers
	public List<Training> getTraineeTrainings(String username, TrainingFilterRequest filter) {
	    return traineeService.getTraineeTrainings(username, filter);
	}
	public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
	    return traineeService.getTrainersNotAssignedToTrainee(traineeUsername);
	}
	public void updateTraineeTrainers(String traineeUsername, List<Long> trainerIds) {
	    traineeService.updateTraineeTrainers(traineeUsername, trainerIds);
	}

	// --- Trainer operations ---
	public Trainer createTrainer(Trainer t) { return trainerService.createTrainer(t); }
	public Trainer updateTrainer(String username, Trainer t) { return trainerService.updateTrainer(username, t); }
	public void changeTrainerPassword(String username, String newPassword) { trainerService.changePassword(username, newPassword); }
	public Trainer getTrainerByUsername(String username) { return trainerService.getByUsername(username); }
	public void setTrainerActive(String username, boolean active) { trainerService.setActive(username, active); }

	// trainer trainings
	public List<Training> getTrainerTrainings(String username, Date from, Date to, String traineeName) {
	    return trainerService.getTrainerTrainings(username, new TrainerTrainingFilterRequest(from, to, traineeName));
	}

	// --- Training operations ---
	public Training createTraining(Training t) { return trainingService.addTraining(t); }

    public List<TrainingType> listTrainingTypes() { return trainingTypeService.listAll(); }
}
