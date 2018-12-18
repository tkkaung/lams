package ntu.celt.eUreka2.pages.modules.big5;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageBIG5{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<BIG5Survey> big5s;
	@Property
	private BIG5Survey big5;
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BIG5DAO big5DAO;
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
		if(!canManageBIG5Survey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		big5s =  big5DAO.getBIG5SurveysByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(BIG5Survey big5){
		if(big5.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<BIG5Survey> getModel() {
		BeanModel<BIG5Survey> model = beanModelSource.createEditModel(BIG5Survey.class, messages);
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
		big5 = big5DAO.getBIG5SurveyById(id);
		if(big5==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "big5iling ID", id));
		}
		project = big5.getProject();
		
		big5.setReleased(!big5.getReleased());
		big5DAO.updateBIG5Survey(big5);
	}
	
}
