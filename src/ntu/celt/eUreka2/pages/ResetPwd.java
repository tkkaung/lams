package ntu.celt.eUreka2.pages;


import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.user.ViewYourInfo;
import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.slf4j.Logger;

@PublicPage
public class ResetPwd
{
	private String username;
	private Long time;
	private String random;
	private String hash;
	
	@Property
	private User user;
	@Property
	private String password;
	@Property
	private String confirmPassword;
	
	@SessionState
	private AppState appState;
	
	
	@Inject
    private UserDAO userDAO;	
	@Inject 
	private WebSessionDAO websessionDAO;
	
	@Inject
	private Logger logger;
	@Inject
    private RequestGlobals requestGlobal;
	@Inject
    private Messages messages;
	
	
    @Component(id="form")
	private BeanEditForm form;
	@Component(id="password")
	private PasswordField passwordField;
		
	void onActivate(EventContext ec) {
		if(ec.getCount()==4){
			username = ec.get(String.class, 0);
			time = ec.get(Long.class, 1);
			random = ec.get(String.class, 2);
			hash = ec.get(String.class, 3);
		}
	}
	Object[] onPassivate() {
		return new Object[]{username, time, random, hash};
	}
	
	void setupRender(){
		//validate the request
		if(username==null || time==null || random==null || hash==null){
			throw new NotAuthorizedAccessException(messages.get("authentication-parameter-is-missing"));
		}
		Date now = new Date();
		if((new Date(time + Config.getLong(Config.AUTH_RESETPASS_TIMEOUT_LIMIT))).before(now)){
			throw new NotAuthorizedAccessException(messages.get("link-parameter-timeout"));
		}
		String h = generateHash(username, time, random);
		if(!h.equals(hash)){
			throw new NotAuthorizedAccessException(messages.get("authentication-failed"));
		}
		
		user = userDAO.getUserByUsername(username);
	}
	
	void onPrepareForSubmitFromForm(){
		user = userDAO.getUserByUsername(username);
	}
	
	void onValidateFormFromForm()
    {
		if (confirmPassword!=null && !confirmPassword.equals(password))  
		{
			form.recordError(passwordField, messages.get("password-not-match"));
		}	
    }
	
    @CommitAfter
    Object onSuccessFromForm()
    {
    	user.setPassword(Util.generateHashValue(password));
		user.setModifyDate(new Date());
    	userDAO.save(user);
    	
    	WebSessionData wsData = new WebSessionData();
    	wsData.setId((String) requestGlobal.getHTTPServletRequest().getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME));
		if(wsData.getId()==null){
			wsData.setId(requestGlobal.getHTTPServletRequest().getSession(true).getId());
			requestGlobal.getHTTPServletRequest().getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, wsData.getId());
		}
    	wsData.setUsername(user.getUsername());
		wsData.setLoginTime(new Date());
		wsData.setIp(requestGlobal.getHTTPServletRequest().getRemoteAddr());
		wsData.setLastActiveTime(new Date());
		
		
		websessionDAO.immediateSaveWebSessionData(wsData);
		
		logger.info("Username:"+username +" ip:"+wsData.getId() + " has reset password");
		
		appState.recordInfoMsg(messages.get("successful-reset-password"));
		
		return ViewYourInfo.class;
    }
    
	
	public static String generateHash(String username, long time, String random){
		return Util.generateHashValue(username + time + random);
	}
}
