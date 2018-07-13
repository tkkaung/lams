package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class AssesseeEdit extends AbstractPageEvaluation {
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
	private Group group;
	
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
	
	
	private enum SubmitType {SAVE_GRADE, SUBMIT}; 
	private SubmitType submitType;
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private AuditTrailDAO auditDAO;
	
	
	
	Object onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		
		
		eval = evalDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Evaluation ID", eId));
		
		project = eval.getProject();
		evalUserList = eval.getEvalUsersByAssessor(getCurUser());
		group = eval.getGroup();
		if(group!=null){
			if(eval.getAllowSelfEvaluation()){
				assessees = new ArrayList<User>();
				assessees.add(getCurUser());
				assessees.addAll(getOtherStudentsInSameGroup(group, getCurUser()));
			}
			else{
				assessees = getOtherStudentsInSameGroup(group, getCurUser());
			}
		}
		else{
			if(evalUserList.size() == 0){
				return (linkSource.createPageRenderLinkWithContext(AssesseeSelect.class, eId));
			}
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
			
			/*assessees = new ArrayList<User>();
			assessees.addAll(getAllAssessees(project));
			if(!eval.getAllowSelfEvaluation()){
				assessees.remove(getCurUser());
			}*/
		}
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {eId};
	}
	
	void setupRender(){
		if(getEvalStatus(eval, getCurUser()).equals(messages.get("Edited-by-Instructor"))){
			throw new RuntimeException(messages.format("evaluation-x-already-edited-by-instructor", eId));
		}
		if (!canSubmit(eval)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	void onPrepareForSubmitFromForm(){
		//not allow when instructor already submitted
		if(getEvalStatus(eval, getCurUser()).equals(messages.get("Edited-by-Instructor"))){
			throw new RuntimeException(messages.format("evaluation-x-already-edited-by-instructor", eId));
		}
		if (!canSubmit(eval)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
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
	
	
	
	private int initWidth = 330;
	private int pageWidth = 974;
	private int padding = 4;
	private int critWidthPercent = 60;
	public int getCritNameWidth(){
		int numColum = eval.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getCritNameWidthPercent(){
		int numColum = eval.getCriterias().size();
		
		if(numColum==0) //in case not-use-rubric
			return "1%";
		
		int num = Math.round((critWidthPercent)/numColum);
		return num + "%";
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
	
	
	@Component()
	private Form form;
	void onValidateFromForm(){
		
		if(SubmitType.SUBMIT.equals(submitType)){
			if(group!=null){
			
				boolean selectAllField = true;
				String str = "";
				for (User u : assessees) {
					String selCriterions = request.getParameter(Integer.toString(u.getId()) + "_selCriterions");
					if(selCriterions!=null && !selCriterions.isEmpty()){
						String crionIDs[] = selCriterions.split(",");
						List<EvaluationCriteria> ecList = new ArrayList<EvaluationCriteria>();
						for(String crionID : crionIDs){
							long crID = Long.parseLong(crionID);
							EvaluationCriterion acrion = evalDAO.getEvaluationCriterionById(crID);
							EvaluationCriteria ecrit = acrion.getCriteria();
							ecList.add(ecrit);
						}
						if(!ecList.containsAll(eval.getCriterias())){
						//	str += Util.arrayToString(ecList.toArray()) + "____" + Util.arrayToString(eval.getCriterias().toArray()) ;
							selectAllField = false;
							break;
						}
					}
					else{
					//	str += "selCriterions:" + selCriterions ;
						selectAllField = false;
						break;
					}
				}
				
				if(!selectAllField){
					form.recordError(messages.get("error-select-all-field") );
				}
			}
			else{ //group==null
				boolean selectAllField = true;
				for (User u : assessees) {
					String selCriterions = request.getParameter(Integer.toString(u.getId()) + "_selCriterions");
					if(selCriterions!=null && !selCriterions.isEmpty()){
						String crionIDs[] = selCriterions.split(",");
						List<EvaluationCriteria> ecList = new ArrayList<EvaluationCriteria>();
						for(String crionID : crionIDs){
							long crID = Long.parseLong(crionID);
							EvaluationCriterion acrion = evalDAO.getEvaluationCriterionById(crID);
							EvaluationCriteria ecrit = acrion.getCriteria();
							ecList.add(ecrit);
						}
						if(!ecList.containsAll(eval.getCriterias())){
							selectAllField = false;
							
							form.recordError("You choose to evaluate '" + u.getDisplayName() + "' but not select score for some criteria, please select one score for each criteria");
							break;
						}
					}
					
				}
				
			}
			if(eval.getUseFixedPoint()){
				int totalFixedPoint = getTotalFixedPoint();
				int totalPoint = 0;
				for (User u : assessees) {
					String selCriterions = request.getParameter(Integer.toString(u.getId()) + "_selCriterions");
					if(selCriterions!=null && !selCriterions.isEmpty()){
						String crionIDs[] = selCriterions.split(",");
						for(String crionID : crionIDs){
							long crID = Long.parseLong(crionID);
							EvaluationCriterion acrion = evalDAO.getEvaluationCriterionById(crID);
							totalPoint += acrion.getScore();
						}
					}
				}
				if(totalPoint > totalFixedPoint){
					form.recordError("Maximum Point possible to allocate is " + totalFixedPoint + ", but you enter " + totalPoint);
				}
			}
			
		/*	boolean allComment = true;
			for (User u : assessees) {
				String cmtStr = request.getParameter("cmtStr_" + Integer.toString(u.getId()));
				if(cmtStr!=null && !cmtStr.isEmpty()){
					allComment = false;
					break;
				}
				String cmtWeak = request.getParameter("cmtWeak_" + Integer.toString(u.getId()));
				if(cmtWeak!=null && !cmtWeak.isEmpty()){
					allComment = false;
					break;
				}
			}
			if(!allComment){
				form.recordError(messages.get("error-fill-in-all-field"));
			}*/
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm() {
		String newAuditValue = "";
		String prevAuditValue = "";
		for (User u : assessees) {
			String selCriterions = request.getParameter(Integer.toString(u.getId()) + "_selCriterions");
			if(group==null && (selCriterions==null || selCriterions.isEmpty())){
				continue; //if No selectedCriterion from request for this assessee, then ignore this assessee
			}
			
			EvaluationUser evalU = eval.getEvalUserByAssessee(evalUserList, u);
			boolean createMode = false;
			if (evalU == null) {
				evalU = new EvaluationUser();
				createMode = true;
			}
			evalU.getSelectedCriterions().clear(); 
			String selectedScoresStr = "";
			if(selCriterions!=null && !selCriterions.isEmpty()){
				String crionIDs[] = selCriterions.split(",");
				for(String crionID : crionIDs){
					long crID = Long.parseLong(crionID);
					EvaluationCriterion acrion = evalDAO.getEvaluationCriterionById(crID);
					evalU.addSelectedCriterions(acrion);
					selectedScoresStr += acrion.getScore() + ",";
				}
			}
			
			String cmtStr = request.getParameter("cmtStr_" + Integer.toString(u.getId()));
			if(cmtStr!=null && !cmtStr.isEmpty()){
				evalU.setCmtStrength(Util.filterOutRestrictedHtmlTags(cmtStr));	
			}
			String cmtWeak = request.getParameter("cmtWeak_" + Integer.toString(u.getId()));
			if(cmtWeak!=null && !cmtWeak.isEmpty()){
				evalU.setCmtWeakness(Util.filterOutRestrictedHtmlTags(cmtWeak));
			}
			String cmtOther = request.getParameter("cmtOther_" + Integer.toString(u.getId()));
			if(cmtOther!=null && !cmtOther.isEmpty()){
				evalU.setCmtOther(Util.filterOutRestrictedHtmlTags(cmtOther));
			}
			
			evalU.getOpenEndedQuestionAnswers().clear();
			for(int i=0; i<eval.getOpenEndedQuestions().size(); i++){
				String answerOQuestion = request.getParameter("cmtOpQues_" + Integer.toString(u.getId()) + "_" + Integer.toString(i));
				if(answerOQuestion!=null )
					evalU.addOpenEndedQuestionAnswers(Util.filterOutRestrictedHtmlTags(answerOQuestion));
				else
					evalU.addOpenEndedQuestionAnswers("");		
			}
			
			evalU.setAssessedDate(new Date());
			evalU.setAssessee(u);
			evalU.setEvaluation(eval);
			evalU.setAssessor(getCurUser());
			if(SubmitType.SUBMIT.equals(submitType)){
				evalU.setSubmited(true);
			}
			else{
				evalU.setSubmited(false);
			}
			
			if(createMode){
				eval.addEvalUsers(evalU);
				evalDAO.updateEvaluation(eval);
			}
			else{
				evalDAO.updateEvaluation(eval);
			}
			newAuditValue += "Student: " + u.getDisplayName() + ", Total Score: "+ evalU.getTotalScore() 
				+ " ("+ Util.removeLastSeparator(selectedScoresStr, ",") +")" 
				+ System.lineSeparator();
			
		}
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, "Student evaluate other", getCurUser());
		ad.setPrevValue( prevAuditValue);
		ad.setNewValue( newAuditValue);
		
		auditDAO.saveAuditTrail(ad);
		
		
		return linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId());
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
	
	public String getJSArrCritID(){
		String str = "";
		for(EvaluationCriteria ec : eval.getCriterias()){
			str += "'" + ec.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		str = "[" + str + "]";
		return str;
	}
	public String getJSArrUserID(){
		String str = "";
		for(User u : assessees){
			str += "'" + u.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		str = "[" + str + "]";
		return str;
	}
	
	void onSelectedFromSaveGrade() {
		submitType = SubmitType.SAVE_GRADE;
	}
	void onSelectedFromSubmit() {
		submitType = SubmitType.SUBMIT;
	}
	public String getIsSelf(User u){
		if(getCurUser().equals(u)){
			return messages.get("self");
		}
		return "";
	}
	public int getTotalFixedPoint(){
		if(eval.getAllowSelfEvaluation()){
			return eval.getTotalFixedPointByNum(assessees.size());
		}
		return eval.getTotalFixedPointByNum(assessees.size() + 1);
	}
}
