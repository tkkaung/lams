package ntu.celt.eUreka2.pages.admin.project;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ProjectEditStatus extends AbstractPageAdminProject {
	@Property
	private Project proj;
	private String projId;
	
	
	@SuppressWarnings("unused")
	@Property
	private ProjStatus tempStatus;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjStatusDAO projStatusDAO;
	@Inject
	private Messages _messages;

	void onActivate(String id){
		projId = id;
	}
	Object onPassivate(){
		return projId;
	}
	public boolean isCreateMode(){
		if(projId == null)
			return true;
		return false;
	}
	
	void setupRender(){
		proj = projDAO.getProjectById(projId);
		if(proj==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "ProjId", projId));
		}
		ProjUser pu = proj.getMember(getCurUser());
		if((pu ==null || !pu.hasPrivilege(PrivilegeProject.CHANGE_STATUS ))){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
	}
	
	void onPrepareForSubmitFromForm(){
		proj = projDAO.getProjectById(projId);
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		
		//proj.setStatus(status);
		proj.setLastStatusChange(new Date());
		
		projDAO.saveProject(proj);
		
		appState.recordInfoMsg(_messages.format("successfully-update-status-to-x", proj.getStatus().getDisplayName()));
		
		return ManageProjects.class;
	}
	
	public SelectModel getProjStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		
		for (ProjStatus ps : getAvailableStatus()) {
			OptionModel optModel = new OptionModelImpl(ps.getDisplayName(), ps);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	public List<ProjStatus> getAvailableStatus(){
		List<ProjStatus> projStatusList = projStatusDAO.getAllStatus();
		ProjStatus statusDeleted = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_DELETED);
		projStatusList.remove(statusDeleted);
		
		return projStatusList;
	}
	
}
