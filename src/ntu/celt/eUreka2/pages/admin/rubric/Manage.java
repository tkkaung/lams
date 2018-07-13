package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.hibernate.exception.ConstraintViolationException;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

public class Manage extends AbstractPageAdminRubric{
	
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
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private School searchSchool;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
   
	
	void setupRender() {
		if(!canManageRubrics() && !canManageSchoolRubrics()) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if(searchSchool==null)
			searchSchool = getDefaultSchool();
		
		rubrics = aDAO.searchRubrics(searchText, null, null, null, searchSchool);
	}
	
	
	private School getDefaultSchool(){
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		for(Integer schlId : accessibleSchlIDs){
			School s = schoolDAO.getSchoolById(schlId);
			if(s!=null){
				return s;
			}
		}
		return null;
	}
	
	public boolean canDeleteRubric(Rubric r){
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(canManageRubrics())
			return true;
		if(canManageSchoolRubrics()){
			List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
			if(r.getSchool() != null && accessibleSchlIDs.contains(r.getSchool().getId()) )
				return true;
		}
		return false;
	}
	
	
	void onActionFromDelete(int id) {
		Rubric r = aDAO.getRubricById(id);
		
		if(!canDeleteRubric(r)) {
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", r.getName() ));
		}
		long counEval = aDAO.countEvaluationByRubric(r);
		if( counEval > 0){
			appState.recordErrorMsg("Cannot delete the rubric, becausing it is being used by Peer-Evaluation Module. ("+ counEval +" evaluation(s) )");
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
		model.include("name","master","shared","mdate");
		model.get("name").label(messages.get("rubric"));
		model.add("owner", propertyConduitSource.create(Rubric.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
	
}
