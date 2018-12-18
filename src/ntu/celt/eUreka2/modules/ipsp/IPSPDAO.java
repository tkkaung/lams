package ntu.celt.eUreka2.modules.ipsp;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface IPSPDAO extends GenericModuleDAO{
	List<IQuestionSet> getIQuestionSetsByOwner(User u);
	List<IQuestionSet> searchIQuestionSets(String searchText, User owner,  School school); //ignore search field if it is null
	List<IQuestionSet> searchIQuestionSets(Project proj); 
	
	
	IQuestionSet getIQuestionSetById(long id);
	void addIQuestionSet(IQuestionSet qset);
	void updateIQuestionSet(IQuestionSet qset);
	@CommitAfter
	void deleteIQuestionSet(IQuestionSet qset);
	@CommitAfter
	void immediateAddIQuestionSet(IQuestionSet qset);
	
	List<IPSPSurvey> getIPSPSurveysByProject(Project proj);
	IPSPSurvey getIPSPSurveyById(long id);
	IPSPSurveyUser getIPSPSurveyUserById(long id);
	IPSPQuestion getBIGQuestionById(long id);
	IPSPQuestion getIQuestionById(long id);
	
	List<IPSPSurveyUser> getIPSPSurveyUsersByProject(Project proj);
	
	
	void addIPSPSurvey(IPSPSurvey ipsp);
	void updateIPSPSurvey(IPSPSurvey ipsp);
	void deleteIPSPSurvey(IPSPSurvey ipsp);
	long countIPSPSurveyByIQuestionSet(IQuestionSet qset);
	List<IPSPSurvey> getIPSPSurveysByIQuestionSet(IQuestionSet qset);
	
	
	
	void saveIPSPSurveyUser(IPSPSurveyUser ipspUser);
	List<IPSPSurveyUser> searchIPSPSurveyUser(IPSPSurvey ipsp, User Assessor, User Assessee, IQuestionType qtype,  Boolean finished); //ignore search field if it is null
	List<IPSPSurveyUser> searchIPSPSurveyUser(Project proj, User Assessor, User Assessee, IQuestionType qtype,  Boolean finished); //ignore search field if it is null
	List<IPSPSurvey> searchIPSPSurvey(String searchName, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<IPSPSurveyUser> getExpiredIPSPSurveyUsers();
	
	List<User> getNotSubmitedUsersByIPSPSurvey(IPSPSurvey ipsp);
	List<IQuestionScore> getScoresByDimension(IPSPSurveyUser User, Integer PDimensionID);
	
	double getAverageScoreByDimension(IPSPSurveyUser ipspUser, Integer PDimensionID);
	double getSTDEVScoreByDimension(IPSPSurveyUser ipspUser, Integer PDimensionID);
	long getCountScoreByDimension(IPSPSurveyUser ipspUser, Integer PDimensionID);
	double getSumScoreByDimension(IPSPSurveyUser ipspUser, Integer PDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String styleGroup);
	long getCountScoreByDimensionStyleGroup(IPSPSurveyUser ipspUser, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, Integer PDimensionID);
	double getSTDEVNormsByDimension(Project proj, Integer PDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<IPSPSurvey> getIPSPsToSendReminder();
	List<User> getNotAssessedUsersByIPSPSurvey(IPSPSurvey ipsp);
	List<IDimension> getAllIDimensions();
	void addIDimension(IDimension r) ;
	void updateIDimension(IDimension r);
	
	
	
}
