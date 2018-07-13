package ntu.celt.eUreka2.pages.modules.assessment;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;

import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ViewCritComment extends AbstractPageAssessment {
	@Property
	private int aCritId;
	@Property
	private long assmtUserId;
	@Property
	private AssessCriteria aCrit;
	@Property
	private AssessmentUser assmtUser;
	
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	
	void onActivate(EventContext ec) {
		aCritId = ec.get(Integer.class, 0);
		assmtUserId = ec.get(Long.class, 1);

	}
	Object[] onPassivate() {
		return new Object[] {aCritId, assmtUserId};
	}
	
	void setupRender(){
		assmtUser = aDAO.getAssessmentUserById(assmtUserId);
		if(assmtUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentUserID", assmtUserId));
		aCrit = aDAO.getAssessCriteriaById(aCritId);
		if(aCrit==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentCritID", aCrit));
		
		ProjUser pu = assmtUser.getAssessment().getProject().getMember(getCurUser());
		if(isAssessee(pu)){
			if(!assmtUser.getAssessment().getAllowViewComment() 
					|| !assmtUser.getAssessee().equals(getCurUser())
					|| !canRelease(assmtUser.getAssessment()))
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		
	}
}
