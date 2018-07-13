package ntu.celt.eUreka2.pages.admin.systemannouncement;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysAnnouncementDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.SysAnnouncement;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.hibernate.exception.ConstraintViolationException;

public class Manage {
	@Property
	private SysAnnouncement _annmt;
	@Property
	private List<SysAnnouncement> _annmts;
	
	@Inject
	private SysAnnouncementDAO _sysAnnmtDAO;
	@Inject
	private Messages _messages;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
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
		if(!getCurUser().hasPrivilege(PrivilegeSystem.SYSTEM_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_annmts = _sysAnnmtDAO.getAllSysAnnouncements();
	}
	
	@CommitAfter
	void onActionFromDelete(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.SYSTEM_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_annmt = _sysAnnmtDAO.getSysAnnouncementById(id);
		if(_annmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnouncementID", id));
		}
		
		try{
			_sysAnnmtDAO.delete(_annmt);
			appState.recordInfoMsg(_messages.format("successfully-delete-x", _annmt.getSubject()));
		}
		catch(ConstraintViolationException e){
			appState.recordErrorMsg(_messages.format("cant-delete-x-used-by-other", _annmt.getSubject()));
		}	
	}
	
	@CommitAfter
	void onActionFromToggleEnabled(int id) {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.SYSTEM_ANNOUNCEMENT)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_annmt = _sysAnnmtDAO.getSysAnnouncementById(id);
		if(_annmt==null){
			throw new RecordNotFoundException(_messages.format("entity-not-exists", "AnnouncementID", id));
		}
		
		_annmt.setEnabled(!_annmt.isEnabled());
		_annmt.setMdate(new Date());
		_sysAnnmtDAO.save(_annmt);
	}
	
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public int getTotalSize() {
		if (_annmts == null)
			return 0;
		return _annmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public BeanModel<SysAnnouncement> getModel() {
		BeanModel<SysAnnouncement> model = beanModelSource.createEditModel(SysAnnouncement.class, _messages);
        model.include("urgent","subject","content","sdate","edate","mdate","enabled");
        model.add("creator", propertyConduitSource.create(SysAnnouncement.class, "creator.displayName")); 
        model.add("action", null);
        return model;
    }
}
