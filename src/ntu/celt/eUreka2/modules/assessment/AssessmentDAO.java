package ntu.celt.eUreka2.modules.assessment;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface AssessmentDAO extends GenericModuleDAO{
	List<Rubric> getRubricsByOwner(User u);
	List<Rubric> getSharedRubrics();
	List<Rubric> getMasterRubrics();
	List<Rubric> getMasterRubricsAllSchool();
	List<Rubric> getMasterRubrics(School school);
	List<Rubric> searchRubrics(String searchText, User owner, Boolean shared, Boolean master, School school); //ignore search field if it is null
	List<Rubric> searchRubricsViaAssmtBySchool(School school);
	List<Rubric> searchRubricsViaEvalBySchool(School school);
	
	Rubric getRubricById(int id);
	void addRubric(Rubric r);
	void updateRubric(Rubric r);
	@CommitAfter
	void deleteRubric(Rubric r);
	@CommitAfter
	void immediateAddRubric(Rubric r);
	void deleteRubricOpenQuestion(RubricOpenQuestion rq);
	
	List<Assessment> getAssessmentsByProject(Project proj);
	List<Assessment> getVisibleAssessments(Project proj);
	Assessment getAssessmentById(int id);
	AssessmentUser getAssessmentUserById(long id);
	AssessCriterion getAssessCriterionById(long id);
	AssessCriteria getAssessCriteriaById(int id);
	
	long countAssessmentsByProject(Project proj);
	
	void saveAssessCriterion(AssessCriterion acrion);
	void addAssessment(Assessment assess);
	void updateAssessment(Assessment assess);
	void deleteAssessment(Assessment assess);
	int getNextOrderNumber(Project project);
	void reorderAssessmentNumber(Project project);
	long countAssessmentByRubric(Rubric rubric);
	long countEvaluationByRubric(Rubric rubric);
	List<Assessment> getAssessmentsByRubric(Rubric rubric);
	
	void saveAssessmentUser(AssessmentUser assmtUser);
	List<Assessment> searchAssessment(String searchName, School searchSchl, String searchTerm, String searchCourseCode);
	List<Assessment> searchAssessment(String searchName, School searchSchl, String searchTerm, String searchCourseCode, Integer searchRubricID);
	List<Assessment> searchAssessment(String searchName, School searchSchl, String searchTerm, String searchCourseCode, Integer searchRubricID, String projName);
	AssmtFile getAssmtFileById(String id);
	AssmtInstructorFile getAssmtInstructorFileById(String id);
	
	boolean isGroupBeingUse(Group group);
	RubricOpenQuestion getRubricOpenQuestion(long id);
	
}
