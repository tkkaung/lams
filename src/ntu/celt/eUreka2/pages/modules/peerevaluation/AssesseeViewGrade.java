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
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AssesseeViewGrade extends AbstractPageEvaluation{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	private long eId;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private EvaluationUser evalUser;
	@Property
	private List<EvaluationUser> evalUserListAssessee = new ArrayList<EvaluationUser>();
	@Property
	private List<User> assessors;
	@SuppressWarnings("unused")
	@Property
	private User user;
	
	
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private EvaluationCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	
	void setupRender(){
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
		
		loadEvalUserListByAssessee();
		if(evalUserListAssessee==null || evalUserListAssessee.size()==0)
			throw new RecordNotFoundException(messages.format("err-no-graded-data-for-x", getCurUser().getUsername()));
		
		if(!canViewScoredCrit(eval) )
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		if(eval.getGroup()!=null){
			if(eval.getAllowSelfEvaluation()){
				assessors = new ArrayList<User>();
				assessors.add(getCurUser());
				assessors.addAll(getOtherStudentsInSameGroup(eval.getGroup(), getCurUser()));
			}
			else{
				assessors = getOtherStudentsInSameGroup(eval.getGroup(), getCurUser());
			}
		}
		else{
			evalUserListAssessee = eval.getEvalUsersByAssessee( getCurUser());
			assessors = new ArrayList<User>();
			for(int i=0; i<evalUserListAssessee.size(); i++){
				assessors.add(evalUserListAssessee.get(i).getAssessor());
			}
			Collections.sort(assessors, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
		}
	}
	
	public String loadEvalUserListByAssessee(){
		evalUserListAssessee = eval.getEvalUsersByAssessee( getCurUser());
		for(int i=evalUserListAssessee.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserListAssessee.get(i);
			if(!eu.isSubmited()){
				evalUserListAssessee.remove(i);
				continue;
			}
		//	if(!eval.getAllowSelfEvaluation() && getCurUser().equals(eu.getAssessor())){
			if( getCurUser().equals(eu.getAssessor())){
					evalUserListAssessee.remove(i);
			}
		}
		return ""; //to return empty string
	}
	
	public String getOverallGradeDisplay(){
		return convertScoreToGrade(getOverallGradeScore(), 100);
	}
	public float getOverallGradeScore(){
		//get Average
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserListAssessee){
		//	if(!eu.getAssessee().equals(eu.getAssessor())){
				float score = getTotalScore(eu);
				if(score!=0){
					total += getTotalScore(eu);
					num++;
				}
		//	}
		}
		float average = 0;
		if(num!=0)
			average = total/num;
		return Float.parseFloat(Util.formatDecimal(average));
	}
	public float getAverageCriteriaScore(EvaluationCriteria evalCrit){
		//get Average
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserListAssessee){
		//	if(!eu.getAssessee().equals(eu.getAssessor()) ){
			float score = getCritScore(evalCrit, eu);
			if(score!=0){
				total += score;
				num++;
			}
		//	}
		}
		float average = 0;
		if(num!=0)
			average = total/num;
		return Float.parseFloat(Util.formatDecimal(average));
	}
	
	
	
	public String getSelectedClass(EvaluationCriterion crion){
		
		if(crion.equals(scoreToEvalCriterion(crion.getCriteria(), 
				getAverageCriteriaScore(crion.getCriteria()))))
			return "selected";
		return "";
	}
	

	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
	
	public String getIsSelf(User u){
		if(getCurUser().equals(u)){
			return messages.get("self");
		}
		return "";
	}
}

