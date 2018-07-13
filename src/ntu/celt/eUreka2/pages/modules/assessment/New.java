package ntu.celt.eUreka2.pages.modules.assessment;

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
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class New extends AbstractPageAssessment{
	@Property
	private Project project;
	@Property
	private String pid;
	
	@Property
	private Assessment assmt;
	@Property
	private Rubric rubric;
	@Property
	private Rubric rubricMy;
	@Property
	private Rubric rubricMaster;
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
	
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private AssessmentDAO aDAO;
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
		assmt = new Assessment();
		assmt.setName(messages.get("untitled"));
		assmt.setRdate(new Date());
		assmt.setWeightage((float) 0.0);
		
		if(!canCreateAssessment(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	}
	
	void onPrepareForSubmitFromForm(){
		assmt = new Assessment();

	}
	
	@Component(id = "Form")
	private Form form;
	@Component(id="selectRubricSchool")
    private Select selectRubricSchool;
	void onValidateFormFromForm(){
		int c = 0;
		if(rubric != null ) c++;
		if(rubricMy != null ) c++;
		if(rubricMaster != null ) c++;
			
		if(c>1){
			form.recordError(selectRubricSchool, "Please select only one rubric");
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		//assmt.setAssmtUsers(assmtUsers)
		assmt.setProject(project);
		assmt.setCdate(new Date());
		assmt.setMdate(new Date());
		assmt.setDes(Util.textarea2html(assmt.getDes()));
		assmt.setCreator(getCurUser());
		Rubric selectedRubric = null;
		if(rubric != null ) selectedRubric = rubric;
		if(rubricMy != null ) selectedRubric = rubricMy;
		if(rubricMaster != null ) selectedRubric = rubricMaster;
		assmt.setRubric(selectedRubric);
		if(selectedRubric!=null){
			for(RubricCriteria rc : selectedRubric.getCriterias()){
				AssessCriteria ac = new AssessCriteria();
				ac.setAssessment(assmt);
				ac.setName(rc.getName());
				ac.setDes(rc.getDes());
				ac.setWeightage(rc.getWeightage());
				for(RubricCriterion rcion : rc.getCriterions()){
					AssessCriterion acion = new AssessCriterion();
					acion.setCriteria(ac);
					acion.setDes(rcion.getDes());
					acion.setScore(rcion.getScore());
					
					ac.addCriterion(acion);
				}
				assmt.addCriteria(ac);
			}
			assmt.setGmat(selectedRubric.getGmat());
		}
		assmt.setPossibleScore(100);
		assmt.setOrderNumber(aDAO.getNextOrderNumber(project));
		if (assmt.isAllowViewScoredCriteria() && assmt.isAllowViewGrade()) 
			assmt.setAllowViewGradeDetail(true);
		else
			assmt.setAllowViewGradeDetail(false);
		aDAO.addAssessment(assmt);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, AuditTrail.ACTION_ADD, getCurUser());
		auditDAO.saveAuditTrail(ad);
		
		
		if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
			Util.sendHttpGETAsync(callBackURLForIframe);
			callBackURLForIframe = null;
		}
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
	}
	public List<RubricCriterion> getFirstCriterions(){
		return rubric.getCriterias().get(0).getCriterions();
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
	
	
	
}
