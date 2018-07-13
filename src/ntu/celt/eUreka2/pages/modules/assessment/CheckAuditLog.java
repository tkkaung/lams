package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class CheckAuditLog extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@SuppressWarnings("unused")
	@Property
	private List<AuditTrail> auditTrails;
	@SuppressWarnings("unused")
	@Property
	private AuditTrail auditTrail;
	
	@Property
	private int rowIndex;
	
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private AuditTrailDAO auditDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	
	
	
	void setupRender(){
		if(!canViewAssessmentGrade(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		auditTrails = auditDAO.getAuditTrailsByModuleNObjID(getModuleName(), String.valueOf(assmt.getId()));
		
	}
	
	
	public int getRowNum(){
		return rowIndex+1;
	}
		
	

}
