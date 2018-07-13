package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
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

public class New extends AbstractPageEvaluation{
	@Property
	private Project project;
	@Property
	private String pid;
	
	@Property
	private Evaluation eval;
	@Property
	private Rubric rubric;
	@Property
	private Rubric rubricMy;
	@Property
	private Rubric rubricMaster;
	@Property
	private Group group;
	@SuppressWarnings("unused")
	@Property
	private RubricCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private RubricCriterion tempRCriterion; 
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
	private String oQuestion;
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private EvaluationDAO eDAO;
	@Inject
	private AssessmentDAO aDAO;
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
	
	
	
	void onActivate(EventContext ec) {
		pid = ec.get(String.class, 0);
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
	}
	Object[] onPassivate() {
		return new Object[] {pid};
	}
	
	void setupRender(){
		eval = new Evaluation();
		eval.setName(messages.get("untitled"));
		
		
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	}
	
	void onPrepareForSubmitFromForm(){
		eval = new Evaluation();

	}
	
	@Component
	private Form form;
	@Component
	private Select selectGroup;
	@Component
	private Select selectRubricSchool;
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
		else{
			//TODO
			//if(isInvalidGroup(group)){
			//	form.recordError(selectGroup, messages.format("invalid-group-set"));
			//}
		}
		int c = 0;
		if(rubric != null ) c++;
		if(rubricMy != null ) c++;
		if(rubricMaster != null ) c++;
		if(c!=1){
			form.recordError(selectRubricSchool, "Please select one rubric");
		}
		if (eval.getEdate().before(eval.getSdate())) {
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}
		
	@CommitAfter
	Object onSuccessFromForm(){
		eval.setProject(project);
		eval.setCdate(new Date());
		eval.setMdate(new Date());
		eval.setDes(Util.textarea2html(eval.getDes()));
		eval.setCreator(getCurUser());
		Rubric selectedRubric = null;
		if(rubric != null ) selectedRubric = rubric;
		if(rubricMy != null ) selectedRubric = rubricMy;
		if(rubricMaster != null ) selectedRubric = rubricMaster;
		eval.setRubric(selectedRubric);
		eval.setGroup(group);
		if(selectedRubric!=null){
			for(RubricCriteria rc : selectedRubric.getCriterias()){
				EvaluationCriteria ac = new EvaluationCriteria();
				ac.setEvaluation(eval);
				ac.setName(rc.getName());
				ac.setDes(rc.getDes());
				ac.setWeightage(rc.getWeightage());
				for(RubricCriterion rcion : rc.getCriterions()){
					EvaluationCriterion acion = new EvaluationCriterion();
					acion.setCriteria(ac);
					acion.setDes(rcion.getDes());
					acion.setScore(rcion.getScore());
					
					ac.addCriterion(acion);
				}
				eval.addCriteria(ac);
			}
			eval.setCustomNameCmtStrength(selectedRubric.getCmtStrength());
			eval.setCustomNameCmtWeakness(selectedRubric.getCmtWeakness());
			eval.setCustomNameCmtOther(selectedRubric.getCmtOther());
			eval.setUseCmtStrength(selectedRubric.getUseCmtStrength());
			eval.setUseCmtWeakness(selectedRubric.getUseCmtWeakness());
			eval.setUseCmtOther(selectedRubric.getUseCmtOther());

			if(canUseOpenQuestion()){
				
				for(String q : selectedRubric.getOpenEndedQuestions()){
					eval.addOpenEndedQuestions(q);
				}
	
			}
		}
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
		
		eval.setOrderNumber(eDAO.getNextOrderNumber(project));
		eDAO.addEvaluation(eval);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(eval.getId())
				, AuditTrail.ACTION_ADD, getCurUser());
		auditDAO.saveAuditTrail(ad);
		
		if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
			Util.sendHttpGETAsync(callBackURLForIframe);
			callBackURLForIframe = null;
		}
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	
	
	
	
	
	@InjectComponent
	private Zone rubricDetailZone;
	Object onSelectRubric(){
		String rId = request.getParameter("param");
		if(rId==null){
			rubric = null;
		}
		else
			rubric = aDAO.getRubricById(Integer.parseInt(rId));
		
		return rubricDetailZone.getBody();
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
	
	
	
	
	public List<RubricCriterion> getFirstCriterions(){
		return rubric.getCriterias().get(0).getCriterions();
	}
}
