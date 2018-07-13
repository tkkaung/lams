package ntu.celt.eUreka2.pages.admin;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;

public class AdminPageInstruction {
	@Inject
	private Messages messages;
	@Inject
    private WebSessionDAO webSessionDAO;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    @SessionState
    private AppState appState ;
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
	}
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
}
