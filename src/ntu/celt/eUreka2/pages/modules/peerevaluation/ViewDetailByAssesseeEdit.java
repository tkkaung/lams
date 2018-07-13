package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.AuditTrail;
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
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class ViewDetailByAssesseeEdit extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private Group group;	
	@SuppressWarnings("unused")
	@Property
	private GroupUser groupUser;	
	
	private long eId;
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
	@Property
	private int quesIndex;
	@Property
	private String oQuestion;
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	@Inject
	private AuditTrailDAO auditDAO;
	
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		uId = ec.get(Integer.class, 1);
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		selectedUser = uDAO.getUserById(uId);
		if(selectedUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		project = eval.getProject();
		group = eval.getGroup();
		if(group != null){
			groupUser = getGroupUser(group, selectedUser);
			
			if(eval.getAllowSelfEvaluation()){
				assessors = new ArrayList<User>();
				assessors.add(selectedUser);
				assessors.addAll(getOtherStudentsInSameGroup(group, selectedUser));
			}
			else{
				assessors = getOtherStudentsInSameGroup(group, selectedUser);
			}
		}
		else{
			loadEvalUserList(eval, selectedUser);
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
	Object[] onPassivate() {
		return new Object[] {eId, uId};
	}
	
	
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		if (!canModerate(eval)){
			throw new NotAuthorizedAccessException(messages.format("cannot-edit-when-edate-not-over-x" ) + eval.getEdateDisplay());
		}
	}
	void onPrepareForSubmitFromForm(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		if (!canModerate(eval)){
			throw new NotAuthorizedAccessException(messages.format("cannot-edit-when-edate-not-over-x" ) + eval.getEdateDisplay());
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
		}
		return ""; //to return empty string
	}
	public String loadEvalUser(Evaluation eval, User assessor){
		EvaluationUser eu = eval.getEvalUserByAssessor(evalUserList, assessor);
		if(eu==null || !eu.isSubmited()){
			evalUser = null;
			return "";
		}
		if(!eval.getAllowSelfEvaluation() && getCurUser().equals(eu.getAssessor())){
			evalUser = null;
			return "";
		}
		evalUser = eu;
		return ""; //to return empty string
	}
	
	
	public boolean hasGraded(){
		if(evalUser==null)
			return false;
		return true;
	}
	public String getHasNotGradedClass(){
		if(hasGraded())
			return "";
		return "notGraded";
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
		
		if(eval.getUseFixedPoint()){
			float maxStudentPoint = getMaxGroupStudentTotalFixedPoint(groupUser, eval);
			if(maxStudentPoint!=-1)
				total = getStudentTotalFixedPoint(evalUserList) * 100 / maxStudentPoint ;
			return total;
		}
		
		for(User u : assessors){
			loadEvalUser(eval, u);
			if(!selectedUser.equals(u)){
				float score = getTotalScore(); 
				if(score!=0){
					total += score;
					i++;	
				}
				
			}
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
		for(User u : assessors){
			str += "'" + u.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		str = "[" + str + "]";
		return str;
	}
	
	
	@CommitAfter
	Object onSuccessFromForm() {
		String prevAuditValue = "";
		String newAuditValue = "";
		
		loadEvalUserList(eval, selectedUser);
		for (User u : assessors) {
			loadEvalUser(eval, u);
			
			boolean createMode = false;
			if (evalUser == null) {
				evalUser = new EvaluationUser();
				evalUser.setAssessee(selectedUser);
				evalUser.setEvaluation(eval);
				evalUser.setAssessor(u);
				evalUser.setSubmited(true);
				evalUser.setAssessedDate(new Date());				
				createMode = true;
			}
			
			int i = 1;
			for(EvaluationCriteria ec : eval.getCriterias()){
				String editScoreStr = request.getParameter("s_"+ ec.getId() + "_"+ Integer.toString(u.getId()) );
				if(editScoreStr==""){
					editScoreStr ="0";
				}
				int editScore = Integer.parseInt(editScoreStr);
				int score = getCritScoreOriginal(ec, evalUser);
				Integer curEditedScore = evalUser.getCritScoreEdited(ec);
				
				if(! (editScore==score && curEditedScore==null)){
					EvaluationCriterion eCrion = scoreToEvalCriterion(ec, editScore);
					evalUser.addOrUpdateSelCritLeader(eCrion);
				}
				
				//track audit
				if(editScore != score){
					prevAuditValue += "Evaluator: " + u.getDisplayName() + ", Criteria_"+ i +", Score: " + score
							+ System.lineSeparator();
					newAuditValue += "Evaluator: " + u.getDisplayName() + ", Criteria_"+ i +", Score: " + editScore
							+ System.lineSeparator();
				}
				i++;
			}
			String editCmtStr = request.getParameter("cstr_"+ Integer.toString(u.getId()) );
			if(editCmtStr!=null && !editCmtStr.isEmpty()){
				if(!editCmtStr.equals(evalUser.getCmtStrength())){
					evalUser.setEditedCmtStrength(editCmtStr);
					
					prevAuditValue += "Evaluator: " + u.getDisplayName() + ", Strength: " + Util.nvl(evalUser.getCmtStrength())
							+ System.lineSeparator();
					newAuditValue += "Evaluator: " + u.getDisplayName() + ", Strength: " + editCmtStr
							+ System.lineSeparator();
				}
				else{
					evalUser.setEditedCmtStrength(null);
				}
			}
			else{
				if(evalUser.getCmtStrength()!=null && !evalUser.getCmtStrength().isEmpty())
					evalUser.setEditedCmtStrength("");
			}
			
			String editCmtWkn = request.getParameter("cwkn_"+ Integer.toString(u.getId()) );
			if(editCmtWkn!=null && !editCmtWkn.isEmpty()){
				if(!editCmtWkn.equals(evalUser.getCmtWeakness())){
					evalUser.setEditedCmtWeakness(editCmtWkn);
					
					prevAuditValue += "Evaluator: " + u.getDisplayName() + ", Areas to Improve: " + Util.nvl(evalUser.getCmtWeakness())
							+ System.lineSeparator();
					newAuditValue += "Evaluator: " + u.getDisplayName() + ", Areas to Improve: " + editCmtWkn
							+ System.lineSeparator();
				}
				else{
						evalUser.setEditedCmtWeakness(null);
				}
			}
			else{
				if(evalUser.getCmtWeakness()!=null && !evalUser.getCmtWeakness().isEmpty())
					evalUser.setEditedCmtWeakness("");
			}
			
			String editCmtOthr = request.getParameter("cothr_"+ Integer.toString(u.getId()) );
			if(editCmtOthr!=null && !editCmtOthr.isEmpty()){
				if(!editCmtOthr.equals(evalUser.getCmtOther())){
					evalUser.setEditedCmtOther(editCmtOthr);
					prevAuditValue += "Evaluator: " + u.getDisplayName() + ", Others: " + Util.nvl(evalUser.getCmtOther())
							+ System.lineSeparator();
					newAuditValue += "Evaluator: " + u.getDisplayName() + ", Others: " + editCmtOthr
							+ System.lineSeparator();
				}
				else{
					evalUser.setEditedCmtOther(null);
				}
			}
			else{
				if(evalUser.getCmtOther()!=null && !evalUser.getCmtOther().isEmpty())
					evalUser.setEditedCmtOther("");
			}

			evalUser.setEditedDate(new Date());
			evalUser.setEditedPerson(getCurUser());
			
			if(createMode){
				eval.addEvalUsers(evalUser);
				eDAO.updateEvaluation(eval);
			}
			else{
				//eval.addEvalUsers(evalUser);
				eDAO.updateEvaluation(eval);
			}
		}
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, "Moderate Evalaution", getCurUser());
		if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
			ad.setPrevValue("Student: " + selectedUser.getDisplayName() + System.lineSeparator() + prevAuditValue);
			ad.setNewValue("Student: " + selectedUser.getDisplayName() + System.lineSeparator() + newAuditValue);
			
			auditDAO.saveAuditTrail(ad);
		}
		return linkSource.createPageRenderLinkWithContext(ViewAverageModerate.class, eval.getId());
	}
	public boolean containNotEditedByInstructor(){
		for(User u : assessors){
			if(!messages.get("Edited-by-Instructor").equals(getEvalStatus(eval, u))){
				return true;
			}
		}
		return false;
	}
	
	
	public String getEditedPerson(User editedPerson){
		if(editedPerson == null)
			return "";
		return editedPerson.getDisplayName();
	}
}
