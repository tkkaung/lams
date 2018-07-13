package ntu.celt.eUreka2.pages.modules.care;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CARESurvey;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageCARE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<CARESurvey> cares;
	@Property
	private CARESurvey care;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private CAREDAO careDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PropertyConduitSource propertyConduitSource;
	@Inject
	private Request request;
	
	void onActivate(String id) {
		pid = id;
		
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		if(!canManageCARESurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		cares =  careDAO.getCARESurveysByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(CARESurvey care){
		if(care.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<CARESurvey> getModel() {
		BeanModel<CARESurvey> model = beanModelSource.createEditModel(CARESurvey.class, messages);
		model.include("name","mdate","orderNumber","edate", "released", "questionSetName");
		model.get("orderNumber").label("");
		model.get("edate").label("Rating Period");
		model.get("released").label("Released Result");
		model.add("action",null);
		
		model.reorder("orderNumber","name","edate", "questionSetName", "released", "mdate", "action");
		
		return model;
	}
	
	@CommitAfter
	void onActionFromToggleReleased(long id) {
		care = careDAO.getCARESurveyById(id);
		if(care==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "careiling ID", id));
		}
		project = care.getProject();
		
		care.setReleased(!care.getReleased());
		careDAO.updateCARESurvey(care);
	}
	
}
