package ntu.celt.eUreka2.pages.admin.privilege;

import java.util.List;

import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.exception.ConstraintViolationException;

public class PrivilegeIndex {
	@Property
	private Privilege _priv;
	@Property
	private List<Privilege> _privs;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String _searchText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private PrivilegeType _privType;
	
	@Inject
	private PrivilegeDAO _privDAO;
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
	

	public boolean canManagePrivilege(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) 
			return true;
		return false;
	}
	
	void setupRender() {
		if(!canManagePrivilege()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_privs = _privDAO.searchPrivilege(_searchText, _privType);
	}

	
	void onActionFromDelete(String id) {
		if(!canManagePrivilege()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_priv = _privDAO.getPrivilegeById(id);
		try{
			deletePriv(_priv);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _priv.getId()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _priv.getId()));
		}
		
	}
	@CommitAfter
	void deletePriv(Privilege priv){
		_privDAO.delete(priv);
	}
	
	public int getTotalSize() {
		if (_privs == null)
			return 0;
		return _privs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
