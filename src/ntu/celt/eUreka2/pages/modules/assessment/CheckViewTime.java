package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class CheckViewTime extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@SuppressWarnings("unused")
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	
	@Property
	private int rowIndex;
	
	
	@Inject
	private AssessmentDAO assmtDAO;
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
		
		assessees = getAllAssessees(project, assmt);
	}
	
	
	public int getRowNum(){
		return rowIndex+1;
	}
		
	public AssessmentUser loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		return null; //to return empty string
	}
	public float getCritScore(AssessCriteria crit){
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			if(ac.getCriteria().equals(crit))
				return ac.getScore();
		}
		return 0;	
	}
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	

}
