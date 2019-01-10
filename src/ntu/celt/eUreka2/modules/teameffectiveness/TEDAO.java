package ntu.celt.eUreka2.modules.teameffectiveness;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface TEDAO extends GenericModuleDAO{
	List<TEQuestionSet> getTEQuestionSetsByOwner(User u);
	List<TEQuestionSet> searchTEQuestionSets(String searchText, User owner,  School school); //ignore search field if it is null
	List<TEQuestionSet> searchTEQuestionSets(Project proj); //ignore search field if it is null
	
	TEQuestionSet getTEQuestionSetById(long id);
	void addTEQuestionSet(TEQuestionSet qset);
	void updateTEQuestionSet(TEQuestionSet qset);
	@CommitAfter
	void deleteTEQuestionSet(TEQuestionSet qset);
	@CommitAfter
	void immediateAddTEQuestionSet(TEQuestionSet qset);
	
	List<TESurvey> getTESurveysByProject(Project proj);
	TESurvey getTESurveyById(long id);
	TESurveyUser getTESurveyUserById(long id);
	TEQuestion getTEQuestionById(long id);
	TEQuestion getTQuestionById(long id);
	
	List<TESurveyUser> getTESurveyUsersByProject(Project proj);
	
	
	void addTESurvey(TESurvey TE);
	void updateTESurvey(TESurvey TE);
	void deleteTESurvey(TESurvey TE);
	long countTESurveyByTEQuestionSet(TEQuestionSet qset);
	List<TESurvey> getTESurveysByTEQuestionSet(TEQuestionSet qset);
	
	
	
	void saveTESurveyUser(TESurveyUser TEUser);
	List<TESurveyUser> searchTESurveyUser(TESurvey TE, User Assessor, User Assessee,  Boolean finished); //ignore search field if it is null
	List<TESurvey> searchTESurvey(String searchName, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<TESurveyUser> getExpiredTESurveyUsers();
	
	List<User> getNotSubmitedUsersByTESurvey(TESurvey TE);
	List<TEQuestionScore> getScoresByDimension(TESurveyUser User, Integer PDimensionID);
	
	double getAverageScoreByDimension(TESurveyUser TEUser, Integer PDimensionID);
	double getSTDEVScoreByDimension(TESurveyUser TEUser, Integer PDimensionID);
	long getCountScoreByDimension(TESurveyUser TEUser, Integer PDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(TESurveyUser TEUser, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(TESurveyUser TEUser, String styleGroup);
	long getCountScoreByDimensionStyleGroup(TESurveyUser TEUser, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, Integer PDimensionID);
	double getSTDEVNormsByDimension(Project proj, Integer PDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<TESurvey> getTEsToSendReminder();
	List<User> getNotAssessedUsersByTESurvey(TESurvey TE);
	List<TEDimension> getAllTEDimensions();
	void addTEDimension(TEDimension r) ;
	void updateTEDimension(TEDimension r);
	
	
	
}
