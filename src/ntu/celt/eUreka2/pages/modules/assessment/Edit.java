package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class Edit extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private Assessment oldAssmt;
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private AuditTrailDAO auditDAO;

    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	
	
	void setupRender(){
		if(!canEditAssessment(assmt))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	}
	
	void onPrepareFromForm(){
		oldAssmt = assmt.clone();
		assmt.setDes(Util.html2textarea(assmt.getDes()));
		
	}
	
/*	@Component(id = "Form")
	private Form form;
	@Component(id="edate")
    private DateField edateField;
	void onValidateFormFromForm(){
		if(assmt.getSdate()!=null && assmt.getEdate()!=null
			&&	assmt.getSdate().after(assmt.getEdate())){
			form.recordError(edateField, messages.get("endDate-must-be-after-startDate"));
		}
	}
*/	
	@CommitAfter
	Object onSuccessFromForm(){
		assmt.setMdate(new Date());
		assmt.setEditor(getCurUser());
		assmt.setDes(Util.textarea2html(assmt.getDes()));
		if (assmt.isAllowViewScoredCriteria() && assmt.isAllowViewGrade()) 
			assmt.setAllowViewGradeDetail(true);
		else
			assmt.setAllowViewGradeDetail(false);
		aDAO.updateAssessment(assmt);
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, AuditTrail.ACTION_EDIT, getCurUser());
		ad = assmt.getDifferent(oldAssmt, ad);
		if (!ad.getPrevValue().isEmpty() || !ad.getNewValue().isEmpty()){			
			auditDAO.saveAuditTrail(ad);
		}
		
		if(callBackURLForIframe!=null && !"".equals(callBackURLForIframe)){
			Util.sendHttpGETAsync(callBackURLForIframe);
			callBackURLForIframe = null;
		}
		
		return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
		//return linkSource.createPageRenderLinkWithContext(View.class, assmt.getId());
	}
	
	
	
	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
}
