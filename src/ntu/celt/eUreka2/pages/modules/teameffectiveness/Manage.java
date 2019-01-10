package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageTE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<TESurvey> tes;
	@Property
	private TESurvey te;
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private TEDAO teDAO;
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
		if(!canManageTESurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		tes =  teDAO.getTESurveysByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(TESurvey te){
		if(te.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<TESurvey> getModel() {
		BeanModel<TESurvey> model = beanModelSource.createEditModel(TESurvey.class, messages);
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
		te = teDAO.getTESurveyById(id);
		if(te==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "teiling ID", id));
		}
		project = te.getProject();
		
		te.setReleased(!te.getReleased());
		teDAO.updateTESurvey(te);
	}
	
}
