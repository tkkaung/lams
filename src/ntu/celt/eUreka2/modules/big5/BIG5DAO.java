package ntu.celt.eUreka2.modules.big5;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;

public interface BIG5DAO extends GenericModuleDAO{
	List<BQuestionSet> getBQuestionSetsByOwner(User u);
	List<BQuestionSet> searchBQuestionSets(String searchText, User owner,  School school); //ignore search field if it is null
	List<BQuestionSet> searchBQuestionSets(Project proj); //ignore search field if it is null
	
	BQuestionSet getBQuestionSetById(long id);
	void addBQuestionSet(BQuestionSet qset);
	void updateBQuestionSet(BQuestionSet qset);
	@CommitAfter
	void deleteBQuestionSet(BQuestionSet qset);
	@CommitAfter
	void immediateAddBQuestionSet(BQuestionSet qset);
	
	List<BIG5Survey> getBIG5SurveysByProject(Project proj);
	BIG5Survey getBIG5SurveyById(long id);
	BIG5SurveyUser getBIG5SurveyUserById(long id);
	BIG5Question getBIGQuestionById(long id);
	BIG5Question getBQuestionById(long id);
	
	List<BIG5SurveyUser> getBIG5SurveyUsersByProject(Project proj);
	
	
	void addBIG5Survey(BIG5Survey big5);
	void updateBIG5Survey(BIG5Survey big5);
	void deleteBIG5Survey(BIG5Survey big5);
	long countBIG5SurveyByBQuestionSet(BQuestionSet qset);
	List<BIG5Survey> getBIG5SurveysByBQuestionSet(BQuestionSet qset);
	
	
	
	void saveBIG5SurveyUser(BIG5SurveyUser big5User);
	List<BIG5SurveyUser> searchBIG5SurveyUser(BIG5Survey big5, User Assessor, User Assessee,  Boolean finished); //ignore search field if it is null
	List<BIG5Survey> searchBIG5Survey(String searchName, School searchSchl, String searchTerm, String searchCourseCode); //ignore search field if it is null
	
	
	
	boolean isGroupBeingUse(Group group);
	List<BIG5SurveyUser> getExpiredBIG5SurveyUsers();
	
	List<User> getNotSubmitedUsersByBIG5Survey(BIG5Survey big5);
	List<BQuestionScore> getScoresByDimension(BIG5SurveyUser User, Integer PDimensionID);
	
	double getAverageScoreByDimension(BIG5SurveyUser big5User, Integer PDimensionID);
	double getSTDEVScoreByDimension(BIG5SurveyUser big5User, Integer PDimensionID);
	long getCountScoreByDimension(BIG5SurveyUser big5User, Integer PDimensionID);
	
	double getAverageScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String styleGroup);
	double getSTDEVScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String styleGroup);
	long getCountScoreByDimensionStyleGroup(BIG5SurveyUser big5User, String styleGroup);
	
	double getAverageNormsByDimension(Project proj, Integer PDimensionID);
	double getSTDEVNormsByDimension(Project proj, Integer PDimensionID);
	
	double getAverageNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	double getSTDEVNormsByDimensionStyleGroup(Project proj, String StyleGroup);
	
	List<BIG5Survey> getBIG5sToSendReminder();
	List<User> getNotAssessedUsersByBIG5Survey(BIG5Survey big5);
	List<BDimension> getAllBDimensions();
	void addBDimension(BDimension r) ;
	void updateBDimension(BDimension r);
	
	
	
}
