package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.List;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

public class SearchShared extends AbstractPageAdminRubric{
	
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
	@Inject
    private PropertyConduitSource propertyConduitSource; 
   
	
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_OWN_RUBRIC)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		rubrics = aDAO.searchRubrics(searchText, null, true, null, null);
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
		return rubric.getCriterias().get(0).getCriterions();
	}
	
	public BeanModel<Rubric> getModel() {
		BeanModel<Rubric> model = beanModelSource.createEditModel(Rubric.class, messages);
		model.include("name","mdate");
		model.get("name").label(messages.get("rubric"));
		model.add("owner", propertyConduitSource.create(Rubric.class, "owner.displayName"));
		model.add("info",null);
		model.add("action",null);
		
		return model;
	}
	
}
