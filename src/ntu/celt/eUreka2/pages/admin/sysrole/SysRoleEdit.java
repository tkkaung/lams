package ntu.celt.eUreka2.pages.admin.sysrole;

import java.util.List;

import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class SysRoleEdit {
	@Property
	private SysRole _sysRole;
	private int _id;
	@SuppressWarnings("unused")
	@Property
	private Privilege _priv;
	@SuppressWarnings("unused")
	@Property
	private List<Privilege> _privs;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	
	@Inject
	private SysRoleDAO _sysRoleDAO;
	@Inject
	private PrivilegeDAO _privDAO;
	@Inject
	private Messages _messages ;
	@Inject
	private Request request;
	
	void onActivate(int id) {
		_id = id;
	}
	int onPassivate() {
		return _id;
	}
	public boolean isCreateMode(){
		if(_id == 0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){ 
			_sysRole = new SysRole();
		}else{ 
			_sysRole = _sysRoleDAO.getSysRoleById(_id);
			if(_sysRole==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "SystemRoleId", _id));
			}
		}
		_privs = _privDAO.getPrivilegesByType(PrivilegeType.SYSTEM);
	}
	
	void onPrepareForSubmitFromForm() {
		if(isCreateMode()){ 
			_sysRole = new SysRole();
		}else{ 
			_sysRole = _sysRoleDAO.getSysRoleById(_id);
		}
	}

	@Component
	private Form form;
	@Component(id="name")
	private TextField nameField;
	void onValidateFormFromForm() {
		if (isCreateMode() &&  _sysRoleDAO.isRoleNameExist(_sysRole.getName())) {
			form.recordError(nameField, _messages.format("duplicate-key-exception", 
    				_messages.get("name-label"), _sysRole.getName()));
		}
		
	}

	@CommitAfter
	Object onSuccessFromForm() {
		String privIDs[] = request.getParameters("privIDs");
		_sysRole.getPrivileges().clear();
		if(privIDs!=null){
			for(String privID : privIDs){
				Privilege priv = _privDAO.getPrivilegeById(privID);
				if(priv!=null){
					_sysRole.getPrivileges().add(priv);
				}
			}
		}
		
		_sysRoleDAO.save(_sysRole);
		return SysRoleIndex.class;
	}
	
	
	public String hasPrivChecked(Privilege priv){
		if(_sysRole.getPrivileges().contains(priv)){
			return "checked";
		}
		return null;
	}
}
