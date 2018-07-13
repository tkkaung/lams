package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class CheckAuditLog extends AbstractPageEvaluation{
	@Property
	private Project project;
	private long aId;
	@Property
	private Evaluation eval;
	@SuppressWarnings("unused")
	@Property
	private List<AuditTrail> auditTrails;
	@SuppressWarnings("unused")
	@Property
	private AuditTrail auditTrail;
	
	@Property
	private int rowIndex;
	
	
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private AuditTrailDAO auditDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Long.class, 0);
		
		eval = evalDAO.getEvaluationById(aId);
		if(eval==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = eval.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	
	
	
	void setupRender(){
		if(!canManageEvaluation(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		auditTrails = auditDAO.getAuditTrailsByModuleNObjID(getModuleName(), String.valueOf(eval.getId()));
		
	}
	
	
	public int getRowNum(){
		return rowIndex+1;
	}
		
	

}
