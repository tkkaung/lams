package ntu.celt.eUreka2.pages.admin.projtype;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjType;
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
	@Inject
	private ProjTypeDAO _projTypeDAO;
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private Messages _messages;
	
	@Property
	private List<ProjType> _types;
	@Property
	private ProjType _type;
	@SuppressWarnings("unused")
	@Property
	private Module _tmodule;
	@SuppressWarnings("unused")
	@Property
	private ProjRole _tRole;
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_TYPE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_types = _projTypeDAO.getAllTypes();
	}
	
	public long getCountProjectByType(ProjType type){
		return _projDAO.countProjectByType(type);
	}
	
	
	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_TYPE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_type = _projTypeDAO.getTypeById(id);
		try{
			deleteType(_type);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _type.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _type.getName()));
		}
	}
	
	@CommitAfter
	void deleteType(ProjType type){
		_projTypeDAO.deleteType(_type);
	}
	
	public int getTotalSize() {
		if(_types==null) return 0;
		return _types.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
}
