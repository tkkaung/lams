package ntu.celt.eUreka2.pages.admin.rubric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.hibernate.exception.ConstraintViolationException;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

public class Home extends AbstractPageAdminRubric{
	
	@Property
	private Rubric rubric;
	@Property
	private List<Rubric> rubrics;
	@SuppressWarnings("unused")
	@Property
	private RubricCriteria rCrit;
	@SuppressWarnings("unused")
	@Property
	private RubricCriterion rCriterion;
	@SuppressWarnings("unused")
	@Property
	private int tempIndex;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchText;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private AssessmentDAO aDAO;	
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@InjectPage
	private View viewRubricPage;

	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		rubrics = aDAO.searchRubrics(searchText, getCurUser(), null, null, null);
		
	/*	List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		for(int schlID : accessibleSchlIDs){
			School schl = schoolDAO.getSchoolById(schlID);
			List<Rubric> schRubrics = aDAO.searchRubrics(searchText, null, null, true, schl);
			for(Rubric r : schRubrics){
				if(!rubrics.contains(r)){
					rubrics.add(r);
				}
			}
		}
		*/
	}
	
	
	public boolean canDeleteRubric(Rubric r){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) 
			return false;
		if(!r.getOwner().equals(getCurUser()))
			return false;
		return true;
	}
	
	
	void onActionFromDelete(int id) {
		Rubric r = aDAO.getRubricById(id);
		
		if(r==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "RubricID", id));
		if(!canDeleteRubric(r)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", r.getName() ));
		}
		long counEval = aDAO.countEvaluationByRubric(r);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the rubric, becausing it is being used by Peer-Evaluation Module. ("+ counEval +" evaluation(s) )");
			return;
		}
		long counAssmt = aDAO.countAssessmentByRubric(r);
		if( counAssmt > 0){
			appState.recordErrorMsg("Cannot delete the rubric, becausing it is being used by Assessment Module. ("+ counAssmt +" assessment(s) )");
			return;
		}
		
		try{
			
			deleteRubric(r);
			appState.recordInfoMsg(messages.format("successfully-delete-x", r.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", r.getName()));
		}
	}
	
	@CommitAfter
	void deleteRubric(Rubric r){
		aDAO.deleteRubric(r);
	}
	
	public int getTotalSize() {
		if (rubrics == null)
			return 0;
		return rubrics.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public List<RubricCriterion> getFirstCriterions(){
		if(rubric.getCriterias().isEmpty())
			return new ArrayList<RubricCriterion>();
		return rubric.getCriterias().get(0).getCriterions();
	}
	
	public BeanModel<Rubric> getModel() {
		BeanModel<Rubric> model = beanModelSource.createEditModel(Rubric.class, messages);
		if(canManageSchoolRubrics()){
			model.include("name","master","shared","mdate");
		}
		else{
			model.include("name","shared","mdate");
		}
		model.get("name").label(messages.get("rubric"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}

	public StreamResponse onActionFromExportXLS(int rId) throws IOException {
		return viewRubricPage.onExportXls(rId);
	}	
}
