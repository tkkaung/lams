package ntu.celt.eUreka2.pages.admin.backuprestore;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

public class AbstractBackupRestore {

			
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	 public String getSpace(){
    	return "&nbsp;";
    }
	    
	
	public boolean canImport(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJECT))
			return true;
		
		return false;
	}
}
