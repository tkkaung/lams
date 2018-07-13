package ntu.celt.eUreka2.pages.admin.systemannouncement;

import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysAnnouncementDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.SysAnnouncement;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.chenillekit.tapestry.core.components.DateTimeField;

public class Edit {
	@Property
	private SysAnnouncement _annmt;
	private int _id = 0; 
	
	@Inject
    private SysAnnouncementDAO _sysAnnmtDAO;
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.SYSTEM_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		if(isCreateMode()){
    		_annmt = new SysAnnouncement();
    		_annmt.setEnabled(true);
    	}
    	else {
	    	_annmt = _sysAnnmtDAO.getSysAnnouncementById(_id);
	    	if(_annmt==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "SysAnnouncementID", _id));
			}    	
    	}
    }
    
    
    void onPrepareForSubmitFromForm(){
    	if(isCreateMode()){
    		_annmt = new SysAnnouncement();
    		_annmt.setEnabled(true);
    	}
    	else {
	    	_annmt = _sysAnnmtDAO.getSysAnnouncementById(_id);
    	}
    }
    
    @Component(id="form")
	private BeanEditForm form;
    @Component(id="edate")
    private DateTimeField edateField;
    
    void onValidateFormFromForm(){
    	if(_annmt.getSdate()!=null && _annmt.getEdate()!=null
    			&&	_annmt.getSdate().after(_annmt.getEdate())){
    			form.recordError(edateField, _messages.get("endDate-must-be-after-startDate"));
    		}
    	
    }
    @CommitAfter
    Object onSuccessFromForm()
    {
    	if(isCreateMode()){
    		_annmt.setCdate(new Date());
    	}
    	_annmt.setMdate(new Date());
    	_annmt.setCreator(getCurUser());
    	_annmt.setContent(Util.filterOutRestrictedHtmlTags(_annmt.getContent()));
    	
		_sysAnnmtDAO.save(_annmt);
		return Manage.class;
    }
}
