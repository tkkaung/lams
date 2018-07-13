package ntu.celt.eUreka2.pages.admin.projstatus;


import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.pages.admin.projstatus.Manage;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class Edit {
	@Inject
	private ProjStatusDAO _dao;
	@Inject
	private Messages _messages;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	@Property
	private ProjStatus _status;
	private int _id = 0;
	
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){ 
			_status = new ProjStatus();
		}else{ 
			_status = _dao.getStatusById(_id);
			if(_status==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "StatusId", _id));
			}
		}
	}
	
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){ 
			_status = new ProjStatus();
		}else{ 
			_status = _dao.getStatusById(_id);
		}
	}
	
	@Component
	private BeanEditForm form;
	@Component(id="name")
	private TextField nameField;
	
	void onValidateFormFromForm() {
		if(isCreateMode() && _dao.isStatusNameExist(_status.getName())){
			form.recordError(nameField, _messages.format("duplicate-key-exception", _messages.get("name-label"), _status.getName()));
		}
	}
	
	@CommitAfter
	public Object onSuccessFromForm() {
		_dao.updateStatus(_status);
		return Manage.class;
	}
}
