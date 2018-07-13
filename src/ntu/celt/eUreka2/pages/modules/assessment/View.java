package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class View extends AbstractPageAssessment{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	@Property
	private int aId;
	
	@Property
	private Assessment assmt;
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
	private Messages messages;
	
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
		
		if(!canViewAssessment(assmt)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
	}
	
	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
	
}
