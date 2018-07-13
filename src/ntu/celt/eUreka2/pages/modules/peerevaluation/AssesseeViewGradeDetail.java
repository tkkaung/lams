package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
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
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AssesseeViewGradeDetail extends AbstractPageEvaluation{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	@Property
	private Group group;	
	@Property
	private GroupUser groupUser;	
	
	private long eId;
	@SuppressWarnings("unused")
	private int uId;
	@Property
	private Evaluation eval;
	@Property
	private List<User> assessors ;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private User selectedUser;
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
	@SuppressWarnings("unused")
	@Property
	private int quesIndex;
	@SuppressWarnings("unused")
	@Property
	private String oQuestion;
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		selectedUser = getCurUser();
		
		project = eval.getProject();
		group = eval.getGroup();
		if(group!=null){
			groupUser = getGroupUser(group, selectedUser);
			if(groupUser==null){
				throw new RecordNotFoundException("No Group information for student " + selectedUser.getDisplayName());
			}
		}
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		loadEvalUserList(eval, selectedUser);
		if(evalUserList==null || evalUserList.size()==0)
			throw new RecordNotFoundException(messages.format("err-no-graded-data-for-x", getCurUser().getUsername()));
		
		if(!canViewGradeDetail(eval) )
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		
		if(group!=null){
			/*if(eval.getAllowSelfEvaluation()){
				assessors = new ArrayList<User>();
				assessors.add(selectedUser);
				assessors.addAll(getOtherStudentsInSameGroup(group, selectedUser));
			}
			else{
				assessors = getOtherStudentsInSameGroup(group, selectedUser);
			}*/
			assessors = getOtherStudentsInSameGroup(group, selectedUser);
			
		}
		else{
			assessors = new ArrayList<User>();
			for(int i=0; i<evalUserList.size(); i++){
				assessors.add(evalUserList.get(i).getAssessor());
			}
			Collections.sort(assessors, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});

		}
			
	}
	
	
	public int getColNum(){
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public String loadEvalUserList(Evaluation eval, User assessee){
		evalUserList = eval.getEvalUsersByAssessee( assessee);
		for(int i=evalUserList.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserList.get(i);
			if(!eu.isSubmited()){
				evalUserList.remove(i);
				continue;
			}
			if( assessee.equals(eu.getAssessor())){ //noted this different from other pages
				evalUserList.remove(i);
			}
		}
		return ""; //to return empty string
	}

	public String loadEvalUser(Evaluation eval, User assessor){
		List<EvaluationUser> evalUserList1 = evalUserList;
		if(selectedUser.equals(assessor)){
			evalUserList1 = eval.getEvalUsersByAssessee(selectedUser);
		}
		
		EvaluationUser eu = eval.getEvalUserByAssessor(evalUserList1, assessor);
		if(eu==null || !eu.isSubmited()){
			evalUser = null;
			return "";
		}
		
		evalUser = eu;
		return ""; //to return empty string
	}
	
	public boolean hasGradedList(){
		if(evalUserList==null)
			return false;
		return true;
	}
	
	
	public boolean hasGraded(){
		if(evalUser==null)
			return false;
		return true;
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
		
		return convertScoreToGradeClass(getTotalScore(), 100);
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i =0;
		
		for(User u : assessors){
			loadEvalUser(eval, u);
			if(!selectedUser.equals(u)){
				float score = getCritScore(crit,evalUser);
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
		
		for(User u : assessors){
			loadEvalUser(eval, u);
			//if(evalUser!=null){
			if(!selectedUser.equals(u)){
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
	
	public String getIsSelf(User u){
		if(selectedUser.equals(u)){
			return messages.get("self");
		}
		return "";
	}
	public String getIsSelfClass(User u){
		if(selectedUser.equals(u)){
			return "self";
		}
		return "";
	}
	
	
	
}
