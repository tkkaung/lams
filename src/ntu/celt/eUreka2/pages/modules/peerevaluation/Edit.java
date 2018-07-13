package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class Edit extends AbstractPageEvaluation{
	@Property
	private Project project;
	private long eId;
	@Property
	private Evaluation eval;
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
	@SuppressWarnings("unused")
	@Property
	private Integer[] availableDaysToRemind = {0, 1, 3, 5, 7};
	@SuppressWarnings("unused")
	@Property
	private Integer tempInt;
	@Property
	private Group group;
	@SuppressWarnings("unused")
	@Property
	private String oQuestion;
	
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

    
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private GroupDAO gDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private AuditTrailDAO auditDAO;
	private Evaluation oldEval;

	
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
		group = eval.getGroup();

		eval.setCustomNameCmtStrength(getCmmtStrengthName(eval));
		eval.setCustomNameCmtWeakness(getCmmtWeaknessName(eval));
		eval.setCustomNameCmtOther(getCmmtOtherName(eval));
		
		if(canUseOpenQuestion()){
			
		}
		

		
	}
	
	void onPrepareFromForm(){
		oldEval = eval.clone();
		eval.setDes(Util.html2textarea(eval.getDes()));
	}
	
	@Component
	private Form form;
	@Component
	private Select selectGroup;
	@Component(id="edate")
	private DateField edateField;
	void onValidateFormFromForm(){
		if(group == null){
			if(!canCreateEvalWithoutGroup()){
				form.recordError(selectGroup, messages.format("must-select-group"));
			}
			if(eval.getUseFixedPoint()){
				form.recordError(selectGroup, "Must select a group when use Fixed point distribution.");
			}
		}
		/*else if(isInvalidGroup(group)){
			form.recordError(selectGroup, messages.format("invalid-group-set"));
		}	
		*/
		if (eval.getEdate().before(eval.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}	
	
	@InjectComponent
	private Zone groupDetailZone;
	Object onSelectGroup(){
		String gId = request.getParameter("param");
		if(gId==null){
			group = null;
		}
		else
			group = gDAO.getGroupById(Long.parseLong(gId));
		
		return groupDetailZone.getBody();
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		eval.setMdate(new Date());
		eval.setEditor(getCurUser());
		eval.setDes(Util.textarea2html(eval.getDes()));
	
		
		eval.setGroup(group);
		
		
		//set reminder
		eval.getDaysToRemind().clear();
		String[] daysToRemind = request.getParameters("dayChkBox");
		if(daysToRemind !=null){
			for(String _day : daysToRemind){
				eval.addDaysToRemind(Integer.parseInt(_day));
			}
		}
		eval.getDaysToRemindInstructor().clear();
		String[] daysToRemindI = request.getParameters("dayChkBoxI");
		if(daysToRemindI !=null){
			for(String _day : daysToRemindI){
				eval.addDaysToRemindInstructor(Integer.parseInt(_day));
			}
		}
		
		eDAO.updateEvaluation(eval);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, AuditTrail.ACTION_EDIT, getCurUser());
		ad = eval.getDifferent(oldEval, ad);
		if (!ad.getPrevValue().isEmpty() || !ad.getNewValue().isEmpty()){			
			auditDAO.saveAuditTrail(ad);
		}
	
		if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
			Util.sendHttpGETAsync(callBackURLForIframe);
			callBackURLForIframe = null;
		}
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	public boolean hasGroupChangedAndHaveGradedData(Group newGroup, Evaluation eval){
		if(!newGroup.equals(eval.getGroup())){
			if(eval.getEvalUsers().size()>0)
				return true;
		}
		return false;
	}
	
	public List<EvaluationCriterion> getFirstCriterions(){
		return eval.getCriterias().get(0).getCriterions();
	}
}
