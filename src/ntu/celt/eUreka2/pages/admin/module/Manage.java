package ntu.celt.eUreka2.pages.admin.module;

import java.util.List;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.hibernate.exception.ConstraintViolationException;

public class Manage {
	
	@Property
	private List<Module> _modules;
	@Property
	private Module _module;
	
	@Inject
	private ModuleDAO _moduleDAO;
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private Messages _messages ;
	@Inject
	private Request _request;
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

		_modules = _moduleDAO.getAllModules();
	}
	
	
	
	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_module = _moduleDAO.getModuleById(id);
		try{
			deleteModule(_module);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _module.getName()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _module.getName()));
		}
	}
	@CommitAfter
	void deleteModule(Module module){
		_moduleDAO.deleteModule(module);
	}
	
	public long getCountProjectByModule(Module m){
		return _projDAO.countProjectByModule(m);
	}
	
	public int getTotalSize() {
		if(_modules==null) return 0;
		return _modules.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public String getContextPath(){
		return _request.getContextPath();
	}
}
