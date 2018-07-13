package ntu.celt.eUreka2.pages.admin.report;

import java.io.IOException;
import java.util.List;

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
import ntu.celt.eUreka2.pages.modules.assessment.Score;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stylesheet="context:lib/css/gradeColor.css")
public class SchoolAssessmentView extends AbstractReport{
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
	@Property
	private int rowIndex;
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private Messages messages;
	
	@InjectPage
	private Score pageAssessmentScore;
	
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
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		assessees = pageAssessmentScore.getAllAssessees(project,assmt);
	}
	
	public int getColNum(){
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public AssessmentUser loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		return null; //to return empty string
	}
	
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	
	public String getTotalAvgGradeClass(){
		return pageAssessmentScore.convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	public String getTotalGradeClass(){
		if(assmtUser==null)
			return null;
		return pageAssessmentScore.convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
	public String getAverageScoreGMAT(AssessCriteria crit){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				String s = getComputedCritScoreDisplayGMAT(crit, assmtUser);
				if(!"-".equals(s)){
					total += Float.parseFloat(s);
					i++;

				}
				//System.err.println("...........total=" + total + ",score=" + s + ",i=" + i);

			}
		}
		if(i==0)
			return "";
		
		//System.err.println("...........avg=" + total/i + ",total=" + total + ",i=" + i) ;

		return Util.formatDecimal(total/i);
	}
	public String getAverageScore(AssessCriteria crit){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				total += Float.parseFloat(getComputedCritScore(crit,assmtUser));
				i++;
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAvgGradeClassGMAT(AssessCriteria crit){
		String avgScore = getAverageScoreGMAT(crit);
		if("".equals(avgScore)){
			return null;
		}
		return pageAssessmentScore.convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public String getAvgGradeClass(AssessCriteria crit){
		String avgScore = getAverageScore(crit);
		if("".equals(avgScore)){
			return null;
		}
		return pageAssessmentScore.convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public float getTotalAverageScore(){
		float total = 0;
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
	public String getTotalAvgScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}
	
	private int initWidth = 330;
	private int pageWidth = 974;
	private int padding = 4;
	public int getCritNameWidth(){
		int numColum = assmt.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getTableWidth(){
		int numColum = assmt.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return "100%";
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return width +"px";
		return "100%";
	}
	
	
	
	public String getColorCodeRangeAplus(){
		return pageAssessmentScore.getColorCodeRangeAplus() ;
	}
	public String getColorCodeRangeA(){
		return pageAssessmentScore.getColorCodeRangeA() ;
	}
	public String getColorCodeRangeAMinus(){
		return pageAssessmentScore.getColorCodeRangeAMinus() ;
	}
	public String getColorCodeRangeBplus(){
		return pageAssessmentScore.getColorCodeRangeBplus() ;
	}
	public String getColorCodeRangeB(){
		return pageAssessmentScore.getColorCodeRangeB() ;
	}
	public String getColorCodeRangeBMinus(){
		return pageAssessmentScore.getColorCodeRangeBMinus() ;
	}
	public String getColorCodeRangeCplus(){
		return pageAssessmentScore.getColorCodeRangeCplus() ;
	}
	public String getColorCodeRangeC(){
		return pageAssessmentScore.getColorCodeRangeC() ;
	}
	public String getColorCodeRangeDplus(){
		return pageAssessmentScore.getColorCodeRangeDplus() ;
	}
	public String getColorCodeRangeD(){
		return pageAssessmentScore.getColorCodeRangeD() ;
	}
	public String getColorCodeRangeF(){
		return pageAssessmentScore.getColorCodeRangeF() ;
	}
	public String getComputedCritScoreDisplay(AssessCriteria crit, AssessmentUser assmtUser){
		return pageAssessmentScore.getComputedCritScoreDisplay(crit,assmtUser);
	}
	public String getComputedCritScoreDisplayGMAT(AssessCriteria crit, AssessmentUser assmtUser){
		return pageAssessmentScore.getComputedCritScoreDisplayGMAT(crit,assmtUser);
	}
	public String getComputedCritScore(AssessCriteria crit, AssessmentUser assmtUser){
		return pageAssessmentScore.getComputedCritScore(crit,assmtUser);
	}
	
	public String getColorCodeGradeAplus(){
		return pageAssessmentScore.getColorCodeGradeAplus() ;
	}
	public String getColorCodeGradeA(){
		return pageAssessmentScore.getColorCodeGradeA() ;
	}
	public String getColorCodeGradeAMinus(){
		return pageAssessmentScore.getColorCodeGradeAminus() ;
	}
	public String getColorCodeGradeBplus(){
		return pageAssessmentScore.getColorCodeGradeBplus() ;
	}
	public String getColorCodeGradeB(){
		return pageAssessmentScore.getColorCodeGradeB() ;
	}
	public String getColorCodeGradeBMinus(){
		return pageAssessmentScore.getColorCodeGradeBminus() ;
	}
	public String getColorCodeGradeCplus(){
		return pageAssessmentScore.getColorCodeGradeCplus() ;
	}
	public String getColorCodeGradeC(){
		return pageAssessmentScore.getColorCodeGradeC() ;
	}
	public String getColorCodeGradeDplus(){
		return pageAssessmentScore.getColorCodeGradeDplus() ;
	}
	public String getColorCodeGradeD(){
		return pageAssessmentScore.getColorCodeGradeD() ;
	}
	public String getColorCodeGradeF(){
		return pageAssessmentScore.getColorCodeGradeF() ;
	}
	
	public StreamResponse onExportXls(int aId) throws IOException {
		Assessment assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		if(!canViewAssessmentReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		return pageAssessmentScore.onExportXls(aId);
	}
	public String getSTDEV(AssessCriteria acrit, Assessment assmt){
		return pageAssessmentScore.getSTDEV(acrit, assmt) ;
	}
	
	public String getTotalSTDEV(Assessment assmt){
		return pageAssessmentScore.getTotalSTDEV(assmt);
	}
	
}
