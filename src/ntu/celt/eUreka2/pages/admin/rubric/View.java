package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class View extends AbstractPageAdminRubric{
	private int id;
	@Property
	private Rubric rubric;
	
	
	@SuppressWarnings("unused")
	@Property
	private RubricCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private RubricCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	@Property
	private String oQuestion;
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
   
	
	void onActivate(int id) {
		this.id = id;
	}
	int onPassivate() {
		return id;
	}
	
	void setupRender(){
		rubric = aDAO.getRubricById(id);
		if(rubric==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "RubricID", id));
		}  
		if(!canViewRubric(rubric)) 
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		

	}
	
	
	
	
	public List<RubricCriterion> getFirstCriterions(){
		return rubric.getCriterias().get(0).getCriterions();
	}
	
	
	public BeanModel<Rubric> getModel() {
		BeanModel<Rubric> model = beanModelSource.createEditModel(Rubric.class, messages);
		if(rubric.getOwner().equals(getCurUser())){
			model.include("name","des","shared","cdate","mdate");
		}
		else{
			model.include("name","des","mdate");
		}
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC)){
			model.add("master", propertyConduitSource.create(Rubric.class, "master"));
			model.reorder("name","des","master");
		}
		model.add("owner", null);
		model.add("possibleScore",null);
		
		return model;
	}
}
