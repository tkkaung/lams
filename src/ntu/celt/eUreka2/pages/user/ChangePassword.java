package ntu.celt.eUreka2.pages.user;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.dao.UserDAO;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class ChangePassword extends AbstractPageUser{
	@Property
	private User user ;
	@Property
    private String oldPassword;
	@Property
    private String password;
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
    @Component(id="oldPassword")
	private PasswordField oldPasswordField;
	@Component(id="password")
	private PasswordField passwordField;
	
	@Inject
	private Messages messages;
	
    void onPrepareFromUserForm(){
    	user = getCurUser();
    }
    void onValidateFormFromUserForm()
    {
    	if(user.getPassword()!=null){
    		oldPassword = oldPassword==null? "" : oldPassword;
	    	if(!Util.generateHashValue(oldPassword).equals(user.getPassword())){
	    		userForm.recordError(oldPasswordField, messages.get("invalid-old-password"));
	        }
    	}
    	if(password==null || password.length()<MIN_LENGTH){
    		userForm.recordError(passwordField, messages.format("invalid-password-length", MIN_LENGTH));
    	}
    	if (confirmPassword!=null && !confirmPassword.equals(password))  
		{
			userForm.recordError(passwordField, messages.get("password-not-match"));
		}	
    }
    @CommitAfter
    Object onSuccessFromUserForm()
    {
    	user.setPassword(Util.generateHashValue(password));
		user.setModifyDate(new Date());
    	userDAO.save(user);
		return ViewYourInfo.class;
    }
    
	
}
