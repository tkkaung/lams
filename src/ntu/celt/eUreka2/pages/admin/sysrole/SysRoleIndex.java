package ntu.celt.eUreka2.pages.admin.sysrole;

import java.util.List;

import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class SysRoleIndex {
	@Property
	private SysRole _sysRole;
	
	@Property
	private List<SysRole> _sysRoles;

	
	@Inject
	private SysRoleDAO _sysRoleDAO;
	@Inject
	private UserDAO _userDAO;
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
	void setupRender() {
		if(!hasAccessRight()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_sysRoles = _sysRoleDAO.getAllSysRoles();
	}

	
	void onActionFromDelete(int id) {
		if(!hasAccessRight()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_sysRole = _sysRoleDAO.getSysRoleById(id);
		try{
			deleteSysRole(_sysRole);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _sysRole.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _sysRole.getName()));
		}
	}
	@CommitAfter
	void deleteSysRole(SysRole sysRole){
		_sysRoleDAO.delete(sysRole);
	}
	
	
	public long getCountUserBySysRole(SysRole role){
		return _userDAO.countUserBySysRole(role);
	}
	
	public boolean hasAccessRight(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_ROLE)) 
			return true;
		if( !_sysRole.isSystem())
			return true;
		return false;
	}
	
	public int getTotalSize() {
		if (_sysRoles == null)
			return 0;
		return _sysRoles.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
