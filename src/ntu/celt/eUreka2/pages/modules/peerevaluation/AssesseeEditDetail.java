package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

@Deprecated
public class AssesseeEditDetail extends AbstractPageEvaluation{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	private long eId;
	private int uId;
	@Property
	private Evaluation eval;
	@Property
	private User user;
	@Property
	private EvaluationUser evalUser;
	@Property
	private List<EvaluationUser> evalUserList = new ArrayList<EvaluationUser>();
	
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
	private boolean createMode;
	
	
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	void onActivate(EventContext ec) {
		eId = ec.get(Long.class, 0);
		uId = ec.get(Integer.class, 1);
		
		eval = eDAO.getEvaluationById(eId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "EvaluationID", eId));
		user = uDAO.getUserById(uId);
		if(user==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		project = eval.getProject();
		loadEvalUserListByAssessor();
		evalUser = eval.getEvalUserByAssessee(evalUserList, user);
		if(evalUser==null){
			createMode = true;
			evalUser = new EvaluationUser();
		}
	}
	Object[] onPassivate() {
		return new Object[] {eId, uId};
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
	}
	
	public String loadEvalUserListByAssessor(){
		evalUserList = eval.getEvalUsersByAssessor( getCurUser());
		/*for(int i=evalUserList.size()-1; i>=0; i--){
			EvaluationUser eu = evalUserList.get(i);
			if(!eu.isSubmited()){
				evalUserList.remove(i);
				continue;
			}
			if(!eval.getAllowSelfEvaluation() && getCurUser().equals(eu.getAssessor())){
				evalUserList.remove(i);
			}
		}*/
		return ""; //to return empty string
	}
	
	public boolean isCreateMode(){
		return createMode;
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		String selCriterions = request.getParameter("selCriterions");
		List<EvaluationCriterion> acList = new ArrayList<EvaluationCriterion>();
		
		if(selCriterions!=null && !selCriterions.isEmpty()){
			String crionIDs[] = selCriterions.split(",");
			for(String crionID : crionIDs){
				long crID = Long.parseLong(crionID);
				EvaluationCriterion acrion = eDAO.getEvaluationCriterionById(crID);
				acList.add(acrion);
			}
		}
		evalUser.getSelectedCriterions().clear(); 
		evalUser.setSelectedCriterions(acList);
		
		evalUser.setAssessedDate(new Date());
		evalUser.setAssessee(user);
		evalUser.setEvaluation(eval);
		evalUser.setAssessor(getCurUser());
		evalUser.setCmtStrength(Util.filterOutRestrictedHtmlTags(evalUser.getCmtStrength()));
		evalUser.setCmtWeakness(Util.filterOutRestrictedHtmlTags(evalUser.getCmtWeakness()));
//		evalUser.setSubmited(false)
		
		Link returnPageLink = null;
		if(createMode){
			eval.addEvalUsers(evalUser);
			eDAO.updateEvaluation(eval);
		}
		else{
			eDAO.updateEvaluation(eval);
		}
		returnPageLink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, eval.getId());
		
		return returnPageLink;
	}
	
	
	
	public String getSelectedClass(EvaluationCriterion crion){
		if(evalUser.getSelectedCriterions().contains(crion))
			return "selected";
		return "";
	}
	public int getCritScore(EvaluationCriteria crit){
		for(EvaluationCriterion ac : evalUser.getSelectedCriterions()){
			if(ac.getCriteria().equals(crit))
				return ac.getScore();
		}
		return 0;	
	}
	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
	
}
