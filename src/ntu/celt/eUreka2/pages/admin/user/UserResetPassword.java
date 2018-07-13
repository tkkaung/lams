package ntu.celt.eUreka2.pages.admin.user;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class UserResetPassword extends AbstractPageAdminUser {
	@Property
	private User user ;
	@Property
    private int userId ;
    @Property
    private String confirmPassword;
    
    @SessionState
	private AppState appState;
	
    
    @Inject
    private UserDAO userDAO;
    @Inject
    private Messages messages;
    @Inject
    private PageRenderLinkSource linkSource;
    
    @Component(id="userForm")
	private BeanEditForm userForm;
	@Component(id="password")
	private PasswordField passwordField;
	
	
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
    void onPrepareForSubmitFromUserForm(){
    	user = userDAO.getUserById(userId);
    }
    void onValidateFormFromUserForm()
    {
		if (confirmPassword!=null && !confirmPassword.equals(user.getPassword()))  
		{
			userForm.recordError(passwordField, messages.get("password-not-match"));
		}	
    }
    @CommitAfter
    Object onSuccessFromUserForm()
    {
    	user.setPassword(Util.generateHashValue(user.getPassword()));
		user.setModifyDate(new Date());
    	userDAO.save(user);
    	appState.recordInfoMsg(messages.format("successfully-update-x", user.getUsername()+" "+messages.get("password-label")));
		return linkSource.createPageRenderLinkWithContext(UserView.class, user.getId());
    }
}
