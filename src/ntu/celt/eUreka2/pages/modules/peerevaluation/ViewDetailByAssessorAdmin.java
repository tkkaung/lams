package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ViewDetailByAssessorAdmin extends AbstractPageEvaluation {
	@SuppressWarnings("unused")
	@Property
	private Project project;
	private long eId;
	@Property
	private Evaluation eval;
	private int assessorId;
	@Property
	private User assessor;
	
	@Property
	private Group group;	
	@SuppressWarnings("unused")
	@Property
	private GroupUser groupUser;	
	
	
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private EvaluationUser evalUser;
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriteria tempCrit;
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriterion tempCriterion; 
	@Property
	private int colIndex;
	@Property
	private int rowIndex;
	@Property
	private int quesIndex;
	@Property
	private String oQuestion;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		assessorId = ec.get(Integer.class, 1);
		
		eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evaluation ID", eId));
		
		assessor = userDAO.getUserById(assessorId);
		if(assessor==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evaluator ID", assessorId));
		
		project = eval.getProject();

		group = eval.getGroup();
		if(group!=null){
			groupUser = getGroupUser(group, assessor);
			if(groupUser==null){
				throw new RecordNotFoundException("No Group information for student " + assessor.getDisplayName());
			}
			
			if(eval.getAllowSelfEvaluation()){
				assessees = new ArrayList<User>();
				assessees.add(assessor);
				assessees.addAll(getOtherStudentsInSameGroup(eval.getGroup(), assessor));
			}
			else{
				assessees = getOtherStudentsInSameGroup(eval.getGroup(), assessor);
			}
			evalUserList = eval.getEvalUsersByAssessor(assessor);
		}	
		else {
			evalUserList = eval.getEvalUsersByAssessor(assessor);
			
			assessees = new ArrayList<User>();
			for(int i=0; i< evalUserList.size(); i++){
				assessees.add(evalUserList.get(i).getAssessee());
			}
			Collections.sort(assessees, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
		}
		
		
	}
	Object[] onPassivate() {
		return new Object[] {eId, assessorId};
	}
	
	void setupRender(){
		appState.recordWarningMsg("WARNING: Use this page with care. After deleting the evaluated grades, " +
				"please remember to tell affected students.");
	}
	
	public int getColNum(){
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public String loadEvalUser(User user){
		evalUser = eval.getEvalUserByAssessee(evalUserList, user);
		
		return ""; 
	}
	
	public boolean hasGraded(){
		if(evalUser==null)
			return false;
		return true;
	}
	public boolean hasGradedCrit(EvaluationCriteria eCrit){
		if(evalUser==null)
			return false;
		for(EvaluationCriterion eCrion : evalUser.getSelectedCriterions()){
			if(eCrion.getCriteria().equals(eCrit)){
				return true;
			}
		}
		return false;
	}
	public EvaluationCriterion getGradedCrit(EvaluationCriteria eCrit){
		if(evalUser==null)
			return null;
		for(EvaluationCriterion eCrion : evalUser.getSelectedCriterions()){
			if(eCrion.getCriteria().equals(eCrit)){
				return eCrion;
			}
		}
		return null;
	}
	
	
	

	public float getTotalScore(){
		if(evalUser==null)
			return 0;
		if(evalUser.getAssessee().equals(evalUser.getAssessor()))
			return 0;
		return getTotalScore(evalUser);
	}
	public String getTotalScoreDisplay(){
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if(eval.getUseFixedPoint())
			return null;
		return convertScoreToGradeClass(getTotalScore(), 100);
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i =0;
		
		for(User u : assessees){
			loadEvalUser( u);
			if(!assessor.equals(u)){
				float score = getCritScoreOriginal(crit,evalUser);
				if(score != 0){
					total += score; 
					i++;
				}
			}
		}
		if(i==0)
			return 0;
		
		return total/i;
	}
	public String getAverageScoreDisplay(EvaluationCriteria crit){
		return Util.formatDecimal(getAverageScore(crit));
	}
	public String getAverageScoreClass(EvaluationCriteria crit){
		return convertScoreToGradeClass(getAverageScore(crit), crit.getMaxScore()); 
	}
	
	public float getTotalAverageScore(){
		float total = 0;
		int i =0;
		if(eval.getUseFixedPoint()){
				total = getStudentTotalFixedPoint(evalUserList)  ;
			return total;
		}
		for(User u : assessees){
			loadEvalUser( u);
			//if(evalUser!=null){
			if(!assessor.equals(u)){
				float score = getTotalScore();
				if(score!=0){
					total += getTotalScore();
					i++;
				}
			}
			//}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAverageScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	public String getTotalAverageScoreClass(){
		if(eval.getUseFixedPoint())
			return null;
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	
	
	
	
	private int initWidth = 330;
	private int pageWidth = 974;
	private int padding = 4;
	public int getCritNameWidth(){
		int numColum = eval.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getTableWidth(){
		int numColum = eval.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return "100%";
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return width +"px";
		return "100%";
	}
	
	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
	
	public int getTotalNumCriterions() {
		return eval.getCriterias().size() * eval.getCriterias().get(0).getCriterions().size();
	}
	public List<EvaluationCriterion> getCriterions(EvaluationCriteria crit) {
		return crit.getCriterions();
	}
	public int getNumCriterions() {
		return eval.getCriterias().get(0).getCriterions().size();
	}
	public String getSelectedClass(EvaluationCriteria crit, EvaluationCriterion crion, User theUser){
		EvaluationUser eu = eval.getEvalUserByAssessee(evalUserList, theUser);
		if(eu.getSelectedCriterions().contains(crion))
			if (crion.getCriteria().equals(crit)) return "selected";
		return "";
	}
	public int getSelectedScore(EvaluationCriterion crion, User theUser) {
		EvaluationUser eu = eval.getEvalUserByAssessee(evalUserList, theUser);
		if(eu.getSelectedCriterions().contains(crion)) return crion.getScore();
		return 0;
	}
	
	
	
	public String getDisplayTotal(double dd){
		return Util.formatDecimal(dd); 
	}
	
	@SuppressWarnings("unused")
	@Property
	private String curGroupNumber ="";
	public String loadGroupNum(Group group, User user){
		curGroupNumber = getGroupTypeNumber(group, user);
		return "";
	}
	
	public String getIsSelf(User u){
		if(assessor.equals(u)){
			return messages.get("self");
		}
		return "";
	}
	@CommitAfter
	public void onActionFromDelete(User user){
		EvaluationUser evalUser = eval.getEvalUserByAssessee(evalUserList, user);
		evalUser.getSelectedCriterions().clear();
		evalUser.getSelectedCriterionsByLeader().clear();
		evalDAO.saveEvaluationUser(evalUser);
		eval.getEvalUsers().remove(evalUser);
		evalUserList.remove(evalUser);
		
		evalDAO.updateEvaluation(eval);
	}
}

