package ntu.celt.eUreka2.pages.admin.projrole;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class Manage {
	@Property
	private List<ProjRole> _roles;
	@Property
	private ProjRole _role;
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private UserDAO _userDAO;
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private Messages _messages;
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_roles = _projRoleDAO.getAllRole();
	}
	
	public long getCountUserByProjRole(ProjRole role){
		return _userDAO.countUserByProjRole(role);
	}
	public long getCountProjectByProjRole(ProjRole role){
		return _projDAO.countProjectByProjRole(role);
	}

	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_role = _projRoleDAO.getRoleById(id);
		try{
			deleteRole(_role);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _role.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _role.getName()));
		}
	}
	
	@CommitAfter
	void deleteRole(ProjRole _role){
		_projRoleDAO.deleteRole(_role);
	}
	
	public int getTotalSize() {
		if(_roles==null) return 0;
		return _roles.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
