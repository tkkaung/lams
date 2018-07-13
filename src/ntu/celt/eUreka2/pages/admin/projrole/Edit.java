package ntu.celt.eUreka2.pages.admin.projrole;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.pages.admin.projrole.Manage;

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

public class Edit {
	@Property
	private ProjRole _role;
	private int _id = 0;
	@Property
	private List<PrivilegeType> _privTypes;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeType _privType;
	@SuppressWarnings("unused")
	@Property
	private Privilege _priv;
	
	
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private PrivilegeDAO _privDAO;
	@Inject
	private Messages _messages;
	@Inject
	private Request request;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){ 
			_role = new ProjRole();
		}else{ 
			_role = _projRoleDAO.getRoleById(_id);
			if(_role==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "RoleId", _id));
			}
		}
		_privTypes = new ArrayList<PrivilegeType>();
		PrivilegeType[] privTypes = PrivilegeType.values();
		for(PrivilegeType pt : privTypes){
			_privTypes.add(pt);
		}
		//exclue PrivilegeType.SYSTEM
		_privTypes.remove(PrivilegeType.SYSTEM);
	}
	
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){ 
			_role = new ProjRole();
		}else{ 
			_role = _projRoleDAO.getRoleById(_id);
		}
	}
	
	@Component
	private Form form;
	@Component(id="name")
	private TextField nameField;
	
	void onValidateFormFromForm() {
		if(isCreateMode() && _projRoleDAO.isRoleNameExist(_role.getName())){
			form.recordError(nameField, _messages.format("duplicate-key-exception", _messages.get("name-label"), _role.getName()));
		}
	}

	@CommitAfter
	public Object onSuccessFromForm() {
		String privIDs[] = request.getParameters("privIDs");
		_role.getPrivileges().clear();
		if(privIDs!=null){
			for(String privID : privIDs){
				Privilege priv = _privDAO.getPrivilegeById(privID);
				if(priv!=null){
					_role.getPrivileges().add(priv);
				}
			}
		}
		_projRoleDAO.updateRole(_role);
		return Manage.class;
	}
	
	public List<Privilege> getPrivs(PrivilegeType privType){
		return _privDAO.getPrivilegesByType(privType);
	}
	public String hasPrivChecked(Privilege priv){
		if(_role.getPrivileges().contains(priv)){
			
			return "checked";
		}
		return null;
	}
}
