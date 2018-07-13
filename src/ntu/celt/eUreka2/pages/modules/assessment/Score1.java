package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class Score1 extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempCrit;
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempCriterion; 
	@Property
	private int colIndex;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	
	
	
	void setupRender(){
		if(!canViewAssessmentGrade(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		
		assessees = getAllAssessees(project, assmt);
	}
	
	public int getColNum(){
		return colIndex+1;
	}
	
	public double getTotalAverageScore(){
		double total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				total += assmtUser.getTotalScore();
				i++;
			}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAverageScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	public String getAverageScore(AssessCriteria crit){
		double total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				total += Double.parseDouble(getComputedCritScore(crit,assmtUser));
				i++;
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	
	public AssessmentUser loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		return null; //to return empty string
	}
	public float getCritScore(AssessCriteria crit){
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			if(ac.getCriteria().equals(crit))
				return ac.getScore();
		}
		return 0;	
	}
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
	public String getAvgAssmtUserDisplayGradeClass(){
		return convertScoreToGradeClass((int)Math.round(getTotalAverageScore()), 100); 
	}
	public String getAssmtUserDisplayGradeClass(){
		if(assmtUser==null)
			return null;
		if("-".equals(assmtUser.getTotalScoreDisplay()))
			return null;
		return convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
	public int getTotalTableRow(){
		return assmt.getCriterias().size()+3;
	}
	
	public float getTotalScore(){
		return assmtUser.getTotalScore();
	}
	public int getNameWidth(){
		if(assessees.size()<10){
			return 30;
		}
		return 20;
	}
	public String getTableWidth(){
		if(assessees.size()>15){
			return (700 + 20*assessees.size() ) +"px";
		}
		return "100%";
	}
	
	private Map<Integer, Integer> scoreCount ;
	public String loadCountScore(){
		scoreCount = new HashMap<Integer, Integer>();
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
					Integer value = scoreCount.get(ac.getScore());
					if(value==null){
						scoreCount.put(ac.getScore(),  1);
					}
					else{
						scoreCount.put(ac.getScore(), value+1);
					}
				}
			}
		}
		return null;
	}
	public Integer getScoreCount(Integer score){
		return scoreCount.get(score);
	}
}
