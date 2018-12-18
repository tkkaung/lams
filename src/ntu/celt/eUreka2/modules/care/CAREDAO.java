package ntu.celt.eUreka2.modules.care;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface CAREDAO extends GenericModuleDAO{
	List<CQuestionSet> getCQuestionSetsByOwner(User u);
	List<CQuestionSet> searchCQuestionSets(String searchText, User owner,  School school); //ignore search field if it is null
	List<CQuestionSet> searchCQuestionSets(Project proj); 
	
	
	CQuestionSet getCQuestionSetById(long id);
	void addCQuestionSet(CQuestionSet qset);
	void updateCQuestionSet(CQuestionSet qset);
	@CommitAfter
	void deleteCQuestionSet(CQuestionSet qset);
	@CommitAfter
	void immediateAddCQuestionSet(CQuestionSet qset);
	
	List<CARESurvey> getCARESurveysByProject(Project proj);
	CARESurvey getCARESurveyById(long id);
	CARESurveyUser getCARESurveyUserById(long id);
	CAREQuestion getCAREQuestionById(long id);
	CQuestion getCQuestionById(long id);
	
	List<CARESurveyUser> getCARESurveyUsersByProject(Project proj);
	
	
	void addCARESurvey(CARESurvey care);
	void updateCARESurvey(CARESurvey care);
	void deleteCARESurvey(CARESurvey care);
	long countCARESurveyByCQuestionSet(CQuestionSet qset);
	List<CARESurvey> getCARESurveysByCQuestionSet(CQuestionSet qset);
	
	
	
	void saveCARESurveyUser(CARESurveyUser careUser);
	List<CARESurveyUser> searchCARESurveyUser(CARESurvey care, User Assessor, User Assessee,  Boolean finished); //ignore search field if it is null
	List<CARESurvey> searchCARESurvey(String searchName, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<CARESurveyUser> getExpiredCARESurveyUsers();
	
	List<User> getNotSubmitedUsersByCARESurvey(CARESurvey care);
	List<CQuestionScore> getScoresByDimension(CARESurveyUser User, Integer PDimensionID);
	
	double getAverageScoreByDimension(CARESurveyUser careUser, Integer PDimensionID);
	double getSTDEVScoreByDimension(CARESurveyUser careUser, Integer PDimensionID);
	long getCountScoreByDimension(CARESurveyUser careUser, Integer PDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(CARESurveyUser careUser, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(CARESurveyUser careUser, String styleGroup);
	long getCountScoreByDimensionStyleGroup(CARESurveyUser careUser, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, Integer PDimensionID);
	double getSTDEVNormsByDimension(Project proj, Integer PDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<CARESurvey> getCAREsToSendReminder();
	List<User> getNotAssessedUsersByCARESurvey(CARESurvey care);
	List<CDimension> getAllCDimensions();
	void addCDimension(CDimension r) ;
	void updateCDimension(CDimension r);
	
	CAREParticular getParticularByUser(User curUser);
	void saveOrUpdateParticular(CAREParticular p);
	
	
}
