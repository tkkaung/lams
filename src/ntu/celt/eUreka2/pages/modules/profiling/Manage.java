package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageProfiling{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Profiling> profs;
	@Property
	private Profiling prof;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ProfilingDAO profDAO;
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
		if(!canManageProfiling(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		profs =  profDAO.getProfilingsByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(Profiling prof){
		if(prof.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<Profiling> getModel() {
		BeanModel<Profiling> model = beanModelSource.createEditModel(Profiling.class, messages);
		model.include("name","mdate","orderNumber","edate", "released", "questionSetName");
		model.get("orderNumber").label("");
		model.get("edate").label("Rating Period");
		model.get("released").label("Released Result");
		model.add("group",propertyConduitSource.create(Profiling.class, "group.groupType"));
		model.add("action",null);
		
		model.reorder("orderNumber","name","edate","group", "questionSetName", "released", "mdate", "action");
		
		return model;
	}
	
	
	@CommitAfter
	void onActionFromToggleReleased(long id) {
		prof = profDAO.getProfilingById(id);
		if(prof==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Profiling ID", id));
		}
		project = prof.getProject();
		
		prof.setReleased(!prof.getReleased());
		profDAO.updateProfiling(prof);
	}
	
}
