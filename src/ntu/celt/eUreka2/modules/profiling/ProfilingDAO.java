package ntu.celt.eUreka2.modules.profiling;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface ProfilingDAO extends GenericModuleDAO{
	List<LQuestionSet> getLQuestionSetsByOwner(User u);
	List<LQuestionSet> searchLQuestionSets(String searchText, User owner,  School school, LQuestionType qType); //ignore search field if it is null
	
	LQuestionSet getLQuestionSetById(long id);
	void addLQuestionSet(LQuestionSet qset);
	void updateLQuestionSet(LQuestionSet qset);
	@CommitAfter
	void deleteLQuestionSet(LQuestionSet qset);
	@CommitAfter
	void immediateAddLQuestionSet(LQuestionSet qset);
	
	List<Profiling> getProfilingsByProject(Project proj);
	Profiling getProfilingById(long id);
	ProfileUser getProfileUserById(long id);
	ProfileQuestion getProfileQuestionById(long id);
	LQuestion getLQuestionById(long id);
	LDimension getLDimensionById(String id);
	List<LDimension> getAllLDimensions();
	List<LDimension> getLDimensionsByStyleGroup(String StyleGroup);
	
	List<ProfileUser> getProfileUsersByProject(Project proj);
	
	void addLDimension(LDimension d);
	void updateLDimension(LDimension d);
	
	void addProfiling(Profiling prof);
	void updateProfiling(Profiling prof);
	void deleteProfiling(Profiling prof);
	long countProfilingByLQuestionSet(LQuestionSet qset);
	List<Profiling> getProfilingsByLQuestionSet(LQuestionSet qset);
	
	
	
	void saveProfileUser(ProfileUser profUser);
	List<ProfileUser> searchProfileUser(Profiling prof, User Assessor, User Assessee, LQuestionType qType, Boolean finished); //ignore search field if it is null
	List<Profiling> searchProfiling(String searchName, LQuestionType qType, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<ProfileUser> getExpiredProfUsers();
	
	List<User> getNotSubmitedUsersByProfiling(Profiling prof);
	List<QuestionScore> getScoresByDimension(ProfileUser profUser, String LDimensionID);
	
	double getAverageScoreByDimension(ProfileUser profUser, String LDimensionID);
	double getSTDEVScoreByDimension(ProfileUser profUser, String LDimensionID);
	long getCountScoreByDimension(ProfileUser profUser, String LDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(ProfileUser profUser, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(ProfileUser profUser, String styleGroup);
	long getCountScoreByDimensionStyleGroup(ProfileUser profUser, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, String LDimensionID);
	double getSTDEVNormsByDimension(Project proj, String LDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<Profiling> getProfsToSendReminder();
	List<User> getNotAssessedUsersByProfiling(Profiling prof);
	
	
	
}
