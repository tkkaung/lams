package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class Manage extends AbstractPageLCDP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<LCDPSurvey> lcdps;
	@Property
	private LCDPSurvey lcdp;
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO lcdpDAO;
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
		if(!canManageLCDPSurvey(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		lcdps =  lcdpDAO.getLCDPSurveysByProject(project);
	}
	
	
	
	
	public boolean isSetEdate(LCDPSurvey lcdp){
		if(lcdp.getEdate()!=null)
			return true;
		return false;
	}
	public BeanModel<LCDPSurvey> getModel() {
		BeanModel<LCDPSurvey> model = beanModelSource.createEditModel(LCDPSurvey.class, messages);
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
		lcdp = lcdpDAO.getLCDPSurveyById(id);
		if(lcdp==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "lcdpiling ID", id));
		}
		project = lcdp.getProject();
		
		lcdp.setReleased(!lcdp.getReleased());
		lcdpDAO.updateLCDPSurvey(lcdp);
	}
	
}
