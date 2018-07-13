package ntu.celt.eUreka2.pages.modules.assessment;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AssesseeViewComment extends AbstractPageAssessment{
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private AssessmentUser assmtUser;
	
	
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;

	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	
	void setupRender(){
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		
		assmtUser = assmt.getAssmtUser(getCurUser());
		if(assmtUser==null)
			throw new RecordNotFoundException("No Comment found");
		
		if(!canViewComments(assmtUser) )
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			
	}
	
}
