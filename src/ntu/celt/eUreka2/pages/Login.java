package ntu.celt.eUreka2.pages;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.LdapService;
import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;


/**
 * Login page
 *
 */
@PublicPage
//@Secure  //TODO: to use HTTPS
@Import(library="context:lib/js/commons1.js")
public class Login
{
	
	@Property
	private String username = "";
	@Property
	private String password = "";
	@Property
	private User user;
	private String callBackPage ;
	@Persist(PersistenceConstants.CLIENT)
	private Integer failedLoginCount;
	@Persist(PersistenceConstants.CLIENT)
	private Integer failedWaitTime; // in minute
	private final int numAttemptToDoubleWaitTime = 5;
	private final int initWaitTime = 2;  //in minute
	
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String ctext1;
	@Property
	private String ctext2;
	
	
    
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
	
	@Inject 
	private Logger logger;
	@Inject @Local
	private WebSessionDAO webSessionDAO;
	@Inject 
	private UserDAO userDAO;
	
	@Inject
    private RequestGlobals requestGlobal;
	@Inject
	private Response response;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages; 
	@Inject
	private LdapService ldapService;
	
	@Component
	private Form loginForm;

	void onActivate(EventContext ec) {
		callBackPage = ec.get(String.class, 0);
		if(callBackPage==null)
			callBackPage = ""; //default to Index page
	}
	Object[] onPassivate() {
		return new Object[] {callBackPage};
	}
	
	void setupRender(){
			
		//when ctext1 has been set by fail to login multiple times, then we regenerate ctext1 here
		if(ctext1!=null)
			ctext1 = RandomStringUtils.random(5, "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789"); //TODO: generate CAPTCHA as image, not text
		
		if(failedLoginCount==null)
			failedLoginCount = 0;
		if(failedWaitTime==null)
			failedWaitTime = initWaitTime;
	}
	
	public boolean canDirectLogin(User u){
		if(u.hasPrivilege(PrivilegeSystem.DIRECT_LOGIN))
			return true;
		return false;
	}
	
	
	void onValidateFormFromLoginForm()
	{
		user = userDAO.getUserByUsername(username);
		if (user == null){
			loginForm.recordError("Invalid username or password.");
			return;
		}
		if(!user.isEnabled()){
			loginForm.recordError("User Account is disabled, please contact eUreka helpdesk.");
			return;
		}
		if(!canDirectLogin(user)){
			loginForm.recordError("For NTU account users please login through NTULearn. " +
					"If you do not have a NTU account please contact eUreka helpdesk.");
			return;
		}
		if(ctext1!=null && !ctext1.equalsIgnoreCase(ctext2)){
			loginForm.recordError("CAPTCHA text not matched. please enter again");
			return;
		}
		
		//authenticate user within the system
		User user1 = webSessionDAO.doAuthenticate(username, Util.generateHashValue(password));
		if (user1 == null){
			//then try authenticate user within LDAP servers
			if(!ldapService.authUser(username, password)){
				loginForm.recordError("Invalid username or password.");
				return;
			}
		}
		
		
	}
	
	
	void onFailureFromLoginForm() throws InterruptedException
	{
		logger.info("Login Fail, username="+username+" ip="+requestGlobal.getHTTPServletRequest().getRemoteAddr());
		
		//double waiting time if login fails multiple time consecutively, to prevent bruteforce or dictionary attacks
		
		failedLoginCount++;
		if(failedLoginCount >= numAttemptToDoubleWaitTime){
			loginForm.recordError("Wait for "+failedWaitTime+" minutes. Then try again.");
			Thread.sleep(failedWaitTime * 60000);
			
			failedLoginCount = 0;
			failedWaitTime = failedWaitTime*2;
			
			//probably use CAPTCHA image after 5 Fail Logins
			ctext1 = "NOT-NULL";
		}
	}
	
	public String getWaitwarningMessage(){
		if(numAttemptToDoubleWaitTime - failedLoginCount <= 1){
			return messages.format("next-fail-attempt-wait-x-minutes", failedWaitTime);
		}
		return null;
	}
	
	@CommitAfter
	void onSuccessFromLoginForm() throws IOException 
	{
		if(ssid==null)
			ssid = requestGlobal.getHTTPServletRequest().getSession(true).getId();
		
		//find existing session
		WebSessionData wsData = webSessionDAO.getWebSessionById(ssid);
		if(wsData==null){
			//try load from cookies
			wsData = webSessionDAO.getWebSessionFromCookies(requestGlobal.getHTTPServletRequest());
			if(wsData!=null)
				requestGlobal.getHTTPServletRequest().getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, wsData.getId());
		}
		
		if(wsData==null){
			wsData = new WebSessionData();
			wsData.setId(ssid);
			wsData.setUsername(user.getUsername());
			wsData.setLoginTime(new Date());
			wsData.setIp(requestGlobal.getHTTPServletRequest().getRemoteAddr());
			
		}
		wsData.setLastActiveTime(new Date());
		requestGlobal.getHTTPServletRequest().getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ssid);
		webSessionDAO.immediateSaveWebSessionData(wsData);
		webSessionDAO.setWebSessionToCookies(wsData, requestGlobal.getHTTPServletRequest());
		
		ctext1 = null; //reset to init value
		failedWaitTime = null;
		failedLoginCount = null;
		
		//still use default password, (i.e.: user login the first Time) 
		if(Util.generateDefaultPassword(user).equals(user.getPassword())){
			response.sendRedirect("user/firstlogin");
			
		}
		else{ 
			if(callBackPage=="" || callBackPage.endsWith("layout.logout"))
				response.sendRedirect("index");
			else	
				response.sendRedirect(callBackPage);
		}
		
	}

	public String getDefaultPageLink(){
		return	linkSource.createPageRenderLink(DefaultPage.class).toURI();
	}
	public String getLoginForgetPasswordLink(){
		return	linkSource.createPageRenderLink(LoginForgetPassword.class).toURI();
	}
	
	
	public String getLoginFAQLink(){
		return	linkSource.createPageRenderLink(Login_faq.class).toURI();
	}
	
	//Temp function, can delete later
	public String getContextCallBackPage(){
		return callBackPage;
	}
	
	public String getServerId(){
		return Config.getString("server.id");
	}
	
	public int getYear(){
		return Calendar.getInstance().get(Calendar.YEAR);
	}
}
