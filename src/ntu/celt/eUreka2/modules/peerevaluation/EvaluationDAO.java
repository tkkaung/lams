package ntu.celt.eUreka2.modules.peerevaluation;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.group.Group;

public interface EvaluationDAO extends GenericModuleDAO{
	
	List<Evaluation> getEvaluationsByProject(Project proj);
	List<Evaluation> getVisibleEvaluations(Project proj);
	Evaluation getEvaluationById(long id);
	EvaluationCriterion getEvaluationCriterionById(long id);
	long countEvaluationsByProject(Project proj);
	EvaluationUser getEvaluationUserById(long id);
	
	void saveEvaluationCriterion(EvaluationCriterion acrion);
	void addEvaluation(Evaluation eval);
	void updateEvaluation(Evaluation eval);
	@CommitAfter
	void deleteEvaluation(Evaluation eval);
	int getNextOrderNumber(Project project);
	void reorderEvaluationNumber(Project project);
	long countEvaluationByRubric(Rubric rubric);
	long countEvaluationByProject(Project proj);
	
	void saveEvaluationUser(EvaluationUser evalUser);
	List<Evaluation> searchEvaluation(String searchName, School searchSchl, 
			String searchTerm, String searchCourseCode);
	List<Evaluation> searchEvaluation(String searchName, School searchSchool,
			String searchTerm, String searchCourseCode, Integer searchRubricID);
	List<Evaluation> searchEvaluation(String searchName, School searchSchool,
			String searchTerm, String searchCourseCode, Integer searchRubricID, String ProjName);
	List<Evaluation> getEvalsToSendReminder();
	List<Evaluation> getEvalsToSendReminderInstructor();
	List<Evaluation> getEvalsToSendLaunchReminder();
	
	boolean isGroupBeingUse(Group group);

	List<Evaluation> getOldEvaluations(String dateStr, int firtResult, int maxResult) ;
	
	void deleteEvaluationUsersByUserProject(User user, Project proj, Group group);
	
	
}
