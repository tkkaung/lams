package ntu.celt.eUreka2.pages.user;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.Index;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class FirstLogin extends AbstractPageUser {
	@Property
	private User user ;
    @Property
    private String confirmPassword;
    
    @Inject
    private UserDAO userDAO;
    @SuppressWarnings("unused")
	@Inject
    private Logger logger;
    
    private int MIN_LENGTH = 6;
    
    @Component(id="userForm")
	private Form userForm;
	@Component(id="password")
	private PasswordField passwordField;
	@SessionState
	private AppState appState;
	
	@Inject
	private Messages messages;
	
	
    void onPrepareFromUserForm(){
    	user = getCurUser();
    	
    }
    void onValidateFormFromUserForm()
    {
    	if(user.getPassword()==null || user.getPassword().length()<MIN_LENGTH){
    		userForm.recordError(passwordField, messages.format("invalid-password-length", MIN_LENGTH));
    	}
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
    	
    	appState.recordInfoMsg(messages.get("successful-saved"));
    	
		return Index.class;
    }
    
    
    
}
