package ntu.celt.eUreka2.modules.scheduling;

import java.util.List;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface SchedulingDAO extends GenericModuleDAO {
  	
  	Schedule getScheduleById(Long id); 
  	Schedule getActiveSchedule(Project project); 
  	List<Schedule> getInactiveSchedules(Project project); 
  	List<Schedule> getInactiveSchedules(); 
  	
  	void saveSchedule(Schedule schedule) ;
  	void deleteSchedule(Schedule schedule) ;
  	
  	List<Milestone> getAllMilestone();
  	Milestone getMilestoneById(Long id);
  	void saveMilestone(Milestone milestone) ;
  	void deleteMilestone(Milestone milestone) ;
  	
  	Phase getPhaseById(Long id);
  	void savePhase(Phase phase);
  	void deletePhase(Phase phase);
  	
  	Task getTaskById(Long id);
  	void saveTask(Task task);
  	void deleteTask(Task task);
  	List<Task> getTasksToSendReminder();
  	List<Task> getUnfinishedTasks(Schedule s, User u);
	long countMilestones(Project proj);
	long countPhases(Project proj);
	long countTasks(Project proj);
	long countMilestones(Project proj, User creator);
	long countPhases(Project proj, User creator);
	long countTasks(Project proj, User creator);
	long countTasksAssigned(Project proj, User creator);
	List<Milestone> getMilestones(Project proj, User creator);
	List<Phase> getPhases(Project proj, User creator);
	List<Task> getTasks(Project proj, User creator);
	List<Task> getTasksAssigned(Project proj, User creator);
} 
