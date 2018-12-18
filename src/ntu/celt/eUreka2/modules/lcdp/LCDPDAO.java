package ntu.celt.eUreka2.modules.lcdp;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface LCDPDAO extends GenericModuleDAO{
	List<PQuestionSet> getPQuestionSetsByOwner(User u);
	List<PQuestionSet> searchPQuestionSets(String searchText, User owner,  School school); //ignore search field if it is null
	List<PQuestionSet> searchPQuestionSets(Project proj); 
	
	PQuestionSet getPQuestionSetById(long id);
	void addPQuestionSet(PQuestionSet qset);
	void updatePQuestionSet(PQuestionSet qset);
	@CommitAfter
	void deletePQuestionSet(PQuestionSet qset);
	@CommitAfter
	void immediateAddPQuestionSet(PQuestionSet qset);
	
	List<LCDPSurvey> getLCDPSurveysByProject(Project proj);
	LCDPSurvey getLCDPSurveyById(long id);
	LCDPSurveyUser getLCDPSurveyUserById(long id);
	LCDPQuestion getLCDPQuestionById(long id);
	PQuestion getPQuestionById(long id);
	
	List<LCDPSurveyUser> getLCDPSurveyUsersByProject(Project proj);
	
	
	void addLCDPSurvey(LCDPSurvey lcdp);
	void updateLCDPSurvey(LCDPSurvey lcdp);
	void deleteLCDPSurvey(LCDPSurvey lcdp);
	long countLCDPSurveyByPQuestionSet(PQuestionSet qset);
	List<LCDPSurvey> getLCDPSurveysByPQuestionSet(PQuestionSet qset);
	
	
	
	void saveLCDPSurveyUser(LCDPSurveyUser lcdpUser);
	List<LCDPSurveyUser> searchLCDPSurveyUser(LCDPSurvey lcdp, User Assessor, User Assessee,  Boolean finished); //ignore search field if it is null
	List<LCDPSurvey> searchLCDPSurvey(String searchName, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<LCDPSurveyUser> getExpiredLCDPSurveyUsers();
	
	List<User> getNotSubmitedUsersByLCDPSurvey(LCDPSurvey lcdp);
	List<PQuestionScore> getScoresByDimension(LCDPSurveyUser User, Integer PDimensionID);
	
	double getAverageScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID);
	double getSTDEVScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID);
	long getCountScoreByDimension(LCDPSurveyUser lcdpUser, Integer PDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String styleGroup);
	long getCountScoreByDimensionStyleGroup(LCDPSurveyUser lcdpUser, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, Integer PDimensionID);
	double getSTDEVNormsByDimension(Project proj, Integer PDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<LCDPSurvey> getLCDPsToSendReminder();
	List<User> getNotAssessedUsersByLCDPSurvey(LCDPSurvey lcdp);
	
	
	
}
