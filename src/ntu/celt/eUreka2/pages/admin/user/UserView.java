package ntu.celt.eUreka2.pages.admin.user;

import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class UserView extends AbstractPageAdminUser {
	
	@Property
	private User user ;
	@Property
    private int userId ;
	@SuppressWarnings("unused")
	@Property
	private SysroleUser sRoleUser;
    @Inject
    private UserDAO userDAO;
    @Inject
    private Messages messages;
	
	void onActivate(int id) {
    	userId = id;
    }

    int onPassivate() {
       return userId;
    }
    
    void setupRender(){
    	if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
    	
    	user = userDAO.getUserById(userId);
    	if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", userId));
		}
    }
    
    
}
