package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.EvalAssesseeView;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

public class AssesseeHome extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Evaluation> evals;
	@Property
	private Evaluation eval;
	@Property
	private int rowIndex;
	
	
	@Property
	private List<EvaluationUser> evalUserListAssessee = new ArrayList<EvaluationUser>();
	
	
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private EvaluationDAO eDAO;
	
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	
	void onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		
		evals = eDAO.getVisibleEvaluations(project);
	}
	String onPassivate() {
		return pid;
	}
	
	
	public void setupRender() {
		
	}
	
	
	
	
	private boolean hasAllowViewScoredCriteria(){
		for(Evaluation as : evals){
			if(as.isAllowViewScoredCriteria())
				return true;
		}
		return false;
	}
	private boolean hasAllowViewGrade(){
		for(Evaluation as : evals){
			if(as.isAllowViewGrade())
				return true;
		}
		return false;
	}
	private boolean hasAllowViewGradeDetail(){
		for(Evaluation as : evals){
			if(as.getAllowViewGradeDetail())
				return true;
		}
		return false;
	}
	
	public BeanModel<Evaluation> getModel() {
		BeanModel<Evaluation> model = beanModelSource.createEditModel(Evaluation.class, messages);
		model.include("name","edate");
		model.get("name").label(messages.get("evaluation-name-des-label"));
		model.get("edate").label(messages.get("assessee-edate"));
		
		model.add("order",null);
		model.get("order").label(messages.get("empty-label"));
		model.add("group",null);
		model.add("status",null);
		model.add("rubric",null);
		if(hasAllowViewGrade() || hasAllowViewScoredCriteria() || hasAllowViewGradeDetail()){
			model.add("myGrade",null);
		}
		model.reorder("order","name");
		
		return model;
	}
	
	public int getRowNum(){
		return rowIndex+1;
	}

	
	public String loadEvalUserListByAssessee(Evaluation eval){
		evalUserListAssessee = eval.getEvalUsersByAssessee( getCurUser());
		for(int i=evalUserListAssessee.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserListAssessee.get(i);
			if(!eu.isSubmited()){
				evalUserListAssessee.remove(i);
				continue;
			}
			//if(!eval.getAllowSelfEvaluation() && getCurUser().equals(eu.getAssessor())){
			if( getCurUser().equals(eu.getAssessor())){
				evalUserListAssessee.remove(i);
			}
		}
		return ""; //to return empty string
	}
	
	
	
	public boolean hasGraded(){
		if(evalUserListAssessee!=null && evalUserListAssessee.size()>0){
			return true;
		}
		return false;	
	}
	public String getGradeDisplay(){
		String grade = convertScoreToGrade(getAverageScore(), 100);
		
		logViewTime(eval, getCurUser());
		
		return grade;
	}
	public float getAverageScore(){
		float total=0;
		int i = 0;
		
		if(eval.getUseFixedPoint()){
			GroupUser groupUser = getGroupUser(eval.getGroup(), getCurUser());
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserListAssessee) * 100 / maxStudentPoint ;
			return total;
		}
		
		//get Average
		for(EvaluationUser eu : evalUserListAssessee){
			float score= getTotalScore(eu); 
			if(score!=0){
				total += score;
				i++;
			}
		}
		if(i == 0)
			return 0;
		return total/i;
	}
	
	@CommitAfter
	public void logViewTime(Evaluation eval, User assessee){
		EvalAssesseeView evalAView = new EvalAssesseeView(assessee);
		int index = eval.getEvalAssesseeViews().indexOf(evalAView);
		if(index < 0){ //not found, the view not exist yet, => add
			eval.addEvalAssesseeViews(evalAView);
		}
		else{
			eval.getEvalAssesseeViews().get(index).setLastGradeViewTime(new Date());
		}
		eDAO.updateEvaluation(eval);
	}
}
