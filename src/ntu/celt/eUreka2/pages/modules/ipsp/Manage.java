package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageIPSP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<IPSPSurvey> ipsps;
	@Property
	private IPSPSurvey ipsp;
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private IPSPDAO ipspDAO;
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
		if(!canManageIPSPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		ipsps =  ipspDAO.getIPSPSurveysByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(IPSPSurvey ipsp){
		if(ipsp.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<IPSPSurvey> getModel() {
		BeanModel<IPSPSurvey> model = beanModelSource.createEditModel(IPSPSurvey.class, messages);
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
		ipsp = ipspDAO.getIPSPSurveyById(id);
		if(ipsp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ipspiling ID", id));
		}
		project = ipsp.getProject();
		
		ipsp.setReleased(!ipsp.getReleased());
		ipspDAO.updateIPSPSurvey(ipsp);
	}
	
}
