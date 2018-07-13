package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.MathStatistic;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ViewAverageModerate extends AbstractPageEvaluation{
	@Property
	private Project project;
	
	private long eId;
	@Property
	private Evaluation eval;
	@Property
	private List<User> assessees ;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@SuppressWarnings("unused")
	@Property
	private User colUser;
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	@Property
	private EvaluationUser evaluser;
	
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
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		assessees = getAllAssessees(project, eval.getGroup());
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
			if(!eval.getAllowSelfEvaluation() && assessee.equals(eu.getAssessor())){
				evalUserList.remove(i);
			}
			else{
			}
		}
		return ""; //to return empty string
	}
	public boolean hasGraded(){
		if(evalUserList==null || evalUserList.size()==0)
			return false;
		return true;
	}
	
	
	
	
	public String getCmtStrengthDisplay(){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			if(!eu.getCmtStrengthDisplay().isEmpty())
				str += "-" + eu.getCmtStrengthDisplay() + "\n";
		}
		return str;
	}
	public String getCmtWeaknessDisplay(){
		String str = "";
		for(EvaluationUser eu : evalUserList){
			if(!eu.getCmtWeaknessDisplay().isEmpty())
				str += "-" + eu.getCmtWeaknessDisplay() + "\n";
		}
		return str;
	}
	
	
	public float getComputedCritScore(EvaluationCriteria evalCrit , List<EvaluationUser> evalUserList){
		//get Average
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserList){
			if(!eu.getAssessee().equals(eu.getAssessor())){
				float score = getCritScore(evalCrit, eu);
				if(score!=0){
					total += score;
					num++;
				}
			}
		}
		if(num==0)
			return 0;
		return total/num;
	}
	public String getComputedCritScoreDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		if(evalUserList ==null || evalUserList.isEmpty())
			return "-";
		
		return Util.formatDecimal(getComputedCritScore(evalCrit, evalUserList));
	}
	
	public float getTotalScore(){
		float total = 0;
		
		if(eval.getUseFixedPoint()){
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserList) * 100 / maxStudentPoint ;
		}
		else{
			for(EvaluationCriteria ec : eval.getCriterias()){
				total += (double) (getComputedCritScore(ec, evalUserList) * ec.getWeightage())/ec.getMaxScore();
			}
		}		return total;
	}
	public String getTotalScoreDisplay(){
		if(!hasGraded()){
			return "-";
		}
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if(!hasGraded()){
			return null;
		}
		return convertScoreToGradeClass(getTotalScore(), 100); 
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i = 0;
		for(User u : assessees){
			loadEvalUserList(eval, u);
			float score = getComputedCritScore(crit, evalUserList);
			if(score != 0){
				total += score;
				i++;
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
		for(EvaluationCriteria ec : eval.getCriterias()){
			total += (getAverageScore(ec) * ec.getWeightage())/ec.getMaxScore();
		}
		return total;
	}
	public String getTotalAverageScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	public String getTotalAverageScoreClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	
	/*public String getTotalAvgGradeClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	public String getTotalGradeClass(){
		if(evalUser==null)
			return null;
		return convertScoreToGradeClass(evalUser.getTotalScore(), 100);
	}
	public String getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i =0;
		
		for(User u : assessees){
			//loadEvalUser(eval, u);
			if(evalUser!=null){
				total += Float.parseFloat(getComputedCritScore(crit,evalUser));
				i++;
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAvgGradeClass(EvaluationCriteria crit){
		String avgScore = getAverageScore(crit);
		if("".equals(avgScore)){
			return null;
		}
		return convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public float getTotalAverageScore(){
		float total = 0;
		int i =0;
		
		
		for(User u : assessees){
			//loadEvalUser(eval, u);
			if(evalUser!=null){
				total += evalUser.getTotalScore();
				i++;
			}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAvgScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}*/
	
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
		String defaultWidth = "100%";
		int scrollWidth = 0;
		if(assessees.size()>20){
			defaultWidth = "98%";
			scrollWidth = 30;
		}
		int numColum = eval.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return defaultWidth;
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return (width-scrollWidth) +"px";
		return defaultWidth;
	}	
	public String getSTDEV(EvaluationCriteria acrit, Evaluation eval){
		List<Double> values = new ArrayList<Double>();
		for(User u : assessees){
			loadEvalUserList(eval, u);
			String s = getComputedCritScoreDisplay(acrit,evalUserList);
			if(!"-".equals(s)){
				values.add(Double.parseDouble(s));
			}
		}
		
		if(values.size()==0)
			return "-";
		else{
			Double[] d = new Double[values.size()];
			values.toArray(d);
			MathStatistic m = new MathStatistic(d);
			
			return Util.formatDecimal(m.getStdDev());
		}
	}
	
	
	public String getTotalSTDEV(Evaluation eval){
		List<Double> values = new ArrayList<Double>();
		for(User u : assessees){
			loadEvalUserList(eval, u);
			String s = getTotalScoreDisplay();
			if(!"-".equals(s)){
				values.add(Double.parseDouble(s));
			}
		}
		
		if(values.size()==0)
			return "-";
		else{
			Double[] d = new Double[values.size()];
			values.toArray(d);
			MathStatistic m = new MathStatistic(d);
			return Util.formatDecimal(m.getStdDev());
		}
	}

	
	@Cached
	public int getGroupSize(){
		return getGroupSize(eval.getGroup());
	}
	
	public int getColspanRubric(){
		return eval.getCriterias().size() * getColspanCriteria() ;
	}
	public int getColspanCriteria(){
		return  getGroupSize() + 1;
	}
	@Property
	private GroupUser groupUser = new GroupUser();
	public String loadGroupUser(Group group, User assessee){
		groupUser = getGroupUser(group, assessee);
		if(groupUser==null)
			return "";
		//sort by name
		Collections.sort(groupUser.getUsers(), new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return "";
	}
	public String getAssessorCritScoreDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList, User assessor){
		float s = getAssessorCritScore(evalCrit, evalUserList, assessor);
		if(s==-1)
			return "";
		return Util.formatDecimal(s);
	}
	public float getAssessorCritScore(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList, User assessor){
		for(EvaluationUser eu : evalUserList){
			if(eu.getAssessor().equals(assessor)){
				float score = getCritScore(evalCrit, eu);
				return score;
			}	
		}
		return -1;
	}
	public String getCritScoreSumDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		float s = getCritScoreSum(evalCrit, evalUserList);
		if(s==-1)
			return "";
		return Util.formatDecimal(s);
	}
	public float getCritScoreSum(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		if(evalUserList==null)
			return -1;
		float total = 0;
		for(EvaluationUser eu : evalUserList){
			float score = getCritScore(evalCrit, eu);
			total+= score;
		}
		return total;
	}
	
	public String getSelfClass(User u1, User u2, Evaluation eval){
		if(eval.getAllowSelfEvaluation())
			return "";
		if(u1.equals(u2))
			return "selfHighlight";
		return "";
	}
}
