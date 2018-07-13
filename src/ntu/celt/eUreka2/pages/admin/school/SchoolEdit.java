package ntu.celt.eUreka2.pages.admin.school;

import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
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

public class SchoolEdit {
	@Property
	private School _school;
	private int _id = 0; //use to avoid 'load and save to School directly', because onActivate() and onPassivate() may be called multiple times
	
	@Inject
    private SchoolDAO _schoolDAO;
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
	
	//A good "rule of thumb" is to use onActivate(...) and onPassivate() for no more than receiving and returning the activation context.
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
    void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode())
    		_school = new School();
    	else {
	    	_school = _schoolDAO.getSchoolById(_id);
	    	if(_school==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "SchoolID", _id));
			}    	
    	}
    }
    
    
    void onPrepareForSubmitFromForm(){
    	if(isCreateMode())
    		_school = new School();
    	else {
	    	_school = _schoolDAO.getSchoolById(_id);
    	}
    }
    
    public String getTitle(){
    	if(isCreateMode())
    		return _messages.get("add-new")+" "+_messages.get("school");
    	return _messages.get("edit")+" "+_messages.get("school");
    }
    
    @Component(id="form")
	private BeanEditForm form;
    @Component(id="name")
	private TextField nameField;
    
    void onValidateFormFromForm(){
    	if(isCreateMode() && _schoolDAO.isSchoolNameExist(_school.getName())){
    		form.recordError(nameField, _messages.format("duplicate-key-exception", 
    				_messages.get("name-label"), _school.getName()));
    	}
    }
    @CommitAfter
    Object onSuccessFromForm()
    {
		_schoolDAO.save(_school);
		return SchoolIndex.class;
    }
   
}
