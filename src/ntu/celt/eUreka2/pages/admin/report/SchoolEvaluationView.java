package ntu.celt.eUreka2.pages.admin.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.MathStatistic;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;
import ntu.celt.eUreka2.pages.modules.peerevaluation.ViewAverage;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stylesheet="context:lib/css/gradeColor.css")
public class SchoolEvaluationView extends AbstractReport{
	@Property
	private Project project;
	private long eId;
	@Property
	private Evaluation eval;
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
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private Messages messages;
	
	@InjectPage
	private ViewAverage pageEvalAverage;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		
		project = eval.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		if(!canViewEvaluationReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		assessees = pageEvalAverage.getAllAssessees(project, eval.getGroup());
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
	
	
	public float getComputedCritScore(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		//get Average
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserList){
			if(!eu.getAssessee().equals(eu.getAssessor())){
				float score = pageEvalAverage.getCritScore(evalCrit, eu);
				if(score!=0){
					total += score;
					num++;
				}
			}
			
		}
		float average = 0;
		if(num!=0)
			average = total/num;
		return average;
	}
	public String getComputedCritScoreDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		if(evalUserList == null || evalUserList.isEmpty()){
			return "-";
		}
		return Util.formatDecimal(getComputedCritScore(evalCrit, evalUserList));
	}
	public float getTotalScore(){
		float total = 0;
		
		for(EvaluationCriteria ec : eval.getCriterias()){
			total += (getComputedCritScore(ec, evalUserList) * ec.getWeightage())/ec.getMaxScore();
		}
		return total;
	}
	public String getTotalScoreDisplay(){
		if(!hasGraded())
			return "-";
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if(!hasGraded())
			return null;
		return pageEvalAverage.convertScoreToGradeClass(getTotalScore(), 100); 
	}
	
	public float getAverageScore(EvaluationCriteria crit){
		float total = 0;
		int i = 0;
		for(User u : assessees){
			loadEvalUserList(eval, u);
			float score = getComputedCritScore(crit, evalUserList); 
			if(score!=0){
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
		return pageEvalAverage.convertScoreToGradeClass(getAverageScore(crit), crit.getMaxScore()); 
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
		return pageEvalAverage.convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	public String getGroupTypeName(Group group, User user){
		return pageEvalAverage.getGroupTypeNumber(group, user);
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
	
	
	

	public String convertScoreToGrade(float score, int possibleScore){
		return pageEvalAverage.convertScoreToGrade(score, possibleScore);
	}
	public String getColorCodeRangeAplus(){
		return pageEvalAverage.getColorCodeRangeAplus() ;
	}
	public String getColorCodeRangeA(){
		return pageEvalAverage.getColorCodeRangeA() ;
	}
	public String getColorCodeRangeAMinus(){
		return pageEvalAverage.getColorCodeRangeAMinus() ;
	}
	public String getColorCodeRangeBplus(){
		return pageEvalAverage.getColorCodeRangeBplus() ;
	}
	public String getColorCodeRangeB(){
		return pageEvalAverage.getColorCodeRangeB() ;
	}
	public String getColorCodeRangeBMinus(){
		return pageEvalAverage.getColorCodeRangeBMinus() ;
	}
	public String getColorCodeRangeCplus(){
		return pageEvalAverage.getColorCodeRangeCplus() ;
	}
	public String getColorCodeRangeC(){
		return pageEvalAverage.getColorCodeRangeC() ;
	}
	public String getColorCodeRangeDplus(){
		return pageEvalAverage.getColorCodeRangeDplus() ;
	}
	public String getColorCodeRangeD(){
		return pageEvalAverage.getColorCodeRangeD() ;
	}
	public String getColorCodeRangeF(){
		return pageEvalAverage.getColorCodeRangeF() ;
	}
	
	public String getColorCodeGradeAplus(){
		return pageEvalAverage.getColorCodeGradeAplus() ;
	}
	public String getColorCodeGradeA(){
		return pageEvalAverage.getColorCodeGradeA() ;
	}
	public String getColorCodeGradeAMinus(){
		return pageEvalAverage.getColorCodeGradeAminus() ;
	}
	public String getColorCodeGradeBplus(){
		return pageEvalAverage.getColorCodeGradeBplus() ;
	}
	public String getColorCodeGradeB(){
		return pageEvalAverage.getColorCodeGradeB() ;
	}
	public String getColorCodeGradeBMinus(){
		return pageEvalAverage.getColorCodeGradeBminus() ;
	}
	public String getColorCodeGradeCplus(){
		return pageEvalAverage.getColorCodeGradeCplus() ;
	}
	public String getColorCodeGradeC(){
		return pageEvalAverage.getColorCodeGradeC() ;
	}
	public String getColorCodeGradeDplus(){
		return pageEvalAverage.getColorCodeGradeDplus() ;
	}
	public String getColorCodeGradeD(){
		return pageEvalAverage.getColorCodeGradeD() ;
	}
	public String getColorCodeGradeF(){
		return pageEvalAverage.getColorCodeGradeF() ;
	}
	
	public StreamResponse onExportXls(Long eId) throws IOException {
		 eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		if(!canViewEvaluationReport())
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		return  pageEvalAverage.onExportXls(eId);
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

}
