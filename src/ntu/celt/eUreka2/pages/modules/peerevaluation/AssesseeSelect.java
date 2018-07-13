package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeSelect extends AbstractPageEvaluation {
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
	
	@Property
	private List<String> toUsersId;
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	
	private enum SubmitType {SAVE_GRADE, SUBMIT}; 
	private SubmitType submitType;
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private UserDAO _userDAO;
	
	void onActivate(EventContext ec) {
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
			
			
			assessees = new ArrayList<User>();
			assessees.addAll(getAllAssessees(project));
			if(!eval.getAllowSelfEvaluation()){
				assessees.remove(getCurUser());
			}
			
			if(toUsersId == null){
				toUsersId = new ArrayList<String>();
				for(int i=0; i< evalUserList.size(); i++){
					toUsersId.add(evalUserList.get(i).getAssessee().getId() + "");
				}
			}
		}
		
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
	
	
	
	public String loadEvalUser(User user){
		evalUser = eval.getEvalUserByAssessee(evalUserList, user);
		return ""; 
	}
	
	public boolean hasGraded(){
		if(evalUser==null)
			return false;
		return true;
	}
	
	
	
	
	
	@Component()
	private Form form;
	void onValidateFromFromForm(){
		if(SubmitType.SUBMIT.equals(submitType)){
			
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm() {
		ArrayList<User> uList = new ArrayList<User>();
		for (String uId : toUsersId) {
			User u = _userDAO.getUserById(Integer.parseInt(uId));
			uList.add(u);
		}
		
		//check to remove non in list
		for (int j = evalUserList.size()-1; j>=0; j--){
			EvaluationUser evalU = evalUserList.get(j);
			if(!uList.contains(evalU.getAssessee())){
				evalU.getSelectedCriterions().clear();
				evalU.getSelectedCriterionsByLeader().clear();
				evalUserList.remove(evalU);
				eval.removeEvalUsers(evalU);
			}
		}
		
		//check to add
		for (User u : uList) {
			EvaluationUser evalU = eval.getEvalUserByAssessee(evalUserList, u);
			if (evalU == null) {
				evalU = new EvaluationUser();
				evalU.setAssessedDate(new Date());
				evalU.setAssessee(u);
				evalU.setEvaluation(eval);
				evalU.setAssessor(getCurUser());
				eval.addEvalUsers(evalU);
			}
		}
		
		
		evalDAO.updateEvaluation(eval);
		
		return linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, eval.getId());
	}
	
	
	
	
	void onSelectedFromSubmit() {
		submitType = SubmitType.SUBMIT;
	}
	
	public SelectModel getAvailableUsersModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (User u : assessees) {
			OptionModel optModel = new OptionModelImpl(u.getDisplayName(), Integer.toString(u.getId()));
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
