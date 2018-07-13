package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AssesseeViewGrade extends AbstractPageAssessment{
	@SuppressWarnings("unused")
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private AssessmentUser assmtUser;
	@SuppressWarnings("unused")
	@Property
	private String rubricOrder;
	
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
	@InjectPage
	private AssesseeHome assesseeHome;
	
	
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
		
		project = assmt.getProject();
		
		assmtUser = assmt.getAssmtUser(getCurUser());
		if(assmtUser==null)
			throw new RecordNotFoundException(messages.format("err-no-graded-data-for-x", getCurUser().getUsername()));
			//throw new RecordNotFoundException(messages.format("not-graded-yet", "AssmtUser", aId  ));
		
		if(!assesseeHome.canViewGradeDetail(assmtUser) )
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		rubricOrder = getRubricOrderBy(assmt);
	}
	
	public String getSelectedClassGMATL(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "L");
	}
	public String getSelectedClassGMATH(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "H");
	}
	public String getSelectedClassGMAT(AssessCriterion crion, String HL){
		if(assmtUser == null )
			return "";
		int index = assmtUser.getSelectedCriterions().indexOf(crion);
		if(index != -1){
			if(assmtUser.getSelectedGMATHL().size()>index){
				if(HL.equals(assmtUser.getSelectedGMATHL().get(index)))
					return "selected";
			}
		}
		return "";
	}
	
	public String getSelectedClass(AssessCriterion crion){
		if(assmtUser.getSelectedCriterions().contains(crion))
			return "selected";
		return "";
	}
	public float getCritScore(AssessCriteria crit){
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			if(ac.getCriteria().equals(crit))
				return ac.getScore();
		}
		return 0;	
	}
/*	public int getTotalScore(){
		int total =0 ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			total += ac.getScore();
		}
		return total;
	}
	public String getSelCrterions(){
		String str = "" ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			str += ac.getId()+",";
		}
		return str;
	}
*/	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
}
