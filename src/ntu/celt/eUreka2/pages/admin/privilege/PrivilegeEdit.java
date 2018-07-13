package ntu.celt.eUreka2.pages.admin.privilege;

import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class PrivilegeEdit {
	@Property
	private Privilege _priv;
	private String _id;
	
	
	@Inject
	private PrivilegeDAO _privDAO;
	@Inject
	private Messages _messages ;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void onActivate(String id) {
		_id = id;
	}
	String onPassivate() {
		return _id;
	}
	public boolean isCreateMode(){
		if(_id == null)
			return true;
		return false;
	}
	
	public boolean canManagePrivilege(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) 
			return true;
		return false;
	}
	
	void setupRender(){
		if(!canManagePrivilege()) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){ 
			_priv = new Privilege();
		}else{ 
			_priv = _privDAO.getPrivilegeById(_id);
			if(_priv==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "PrivilegeID", _id));
			}
		}
	}
	
	void onPrepareForSubmitFromForm() {
		if(isCreateMode()){ 
			_priv = new Privilege();
		}else{ 
			_priv = _privDAO.getPrivilegeById(_id);
		}
	}

	@Component
	private BeanEditForm form;
	@Component(id="id")
	private TextField idField;
	@Component(id="name")
	private TextField nameField;
	void onValidateFormFromForm() {
		if(_priv.getId() == null || _priv.getId().isEmpty())
			form.recordError(idField, "Please fill in ID");
		if(_priv.getName() == null || _priv.getName().isEmpty())
			form.recordError(nameField, "Please fill in Name");
		
		if(isCreateMode() && _privDAO.isPrivilegeIdExist(_priv.getId())){ 
			form.recordError(idField, _messages.format("duplicate-key-exception", _messages.get("id-label"), _priv.getId()));
		}
	}

	@CommitAfter
	Object onSuccessFromForm() {
		_privDAO.save(_priv);
		return PrivilegeIndex.class;
	}

}
