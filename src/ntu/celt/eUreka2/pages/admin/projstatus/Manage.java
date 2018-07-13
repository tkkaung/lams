package ntu.celt.eUreka2.pages.admin.projstatus;

import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

public class Manage {
	@Inject
	private ProjStatusDAO _projStatusDAO;
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private Messages _messages;
	
	@Property
	private List<ProjStatus> _statuses;
	@Property
	private ProjStatus _status;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_statuses = _projStatusDAO.getAllStatus();
	}
	
	public long getCountProjectByStatus(ProjStatus status){
		return _projDAO.countProjectByStatus(status);
	}
	
	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_status = _projStatusDAO.getStatusById(id);
		try{
			deleteStatus(_status);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _status.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _status.getName()));
		}
	}
	@CommitAfter
	void deleteStatus(ProjStatus _status){
		_projStatusDAO.deleteStatus(_status);
	}
	
	public int getTotalSize() {
		if(_statuses==null) return 0;
		return _statuses.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
