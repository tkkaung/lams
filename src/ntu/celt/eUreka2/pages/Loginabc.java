package ntu.celt.eUreka2.pages;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
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
//@Secure
//this login can use MASTER_PASSWORD (can use to login as any username), TODO for security, delete this file when in production 
@Import(library="context:lib/js/commons1.js")
public class Loginabc
{
	private final String MASTER_PASSWORD = "eureka_20";
	
	@Property
	private String username = "";
	@Property
	private String password = ""; 
	
	@Property
	private User user;
	private String callBackPage ;
	@Persist
	private Integer failedLoginCount;
	@Persist
	private Integer failedWaitTime; // in minute
	private final int numAttemptToDoubleWaitTime = 5;
	private final int initWaitTime = 1;  //in minute
	
	@Persist
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
			ctext1 = RandomStringUtils.random(5, "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789");
	}
	
	void onValidateFormFromLoginForm()
	{
		user = userDAO.getUserByUsername(username);
		if (user == null){
			loginForm.recordError("Invalid username.");
			return;
		}
		if(!user.isEnabled()){
			loginForm.recordError("User Account is disabled, please contact eUreka helpdesk.");
			return;
		}
		
		if(	! Util.generateHashValue(MASTER_PASSWORD).equals(Util.generateHashValue(password))){
			user = webSessionDAO.doAuthenticate(username, Util.generateHashValue(password));
		}
		if (user == null){
			loginForm.recordError("Invalid username or password.");
		}
		
		if(ctext1!=null && !ctext1.equalsIgnoreCase(ctext2)){
			loginForm.recordError("Confirm text not matched.");
		}
	}
	
	void onFailureFromLoginForm() throws InterruptedException
	{
		logger.info("Login Fail, username="+username+" ip="+requestGlobal.getHTTPServletRequest().getRemoteAddr());
		
		//double waiting time if login fails multiple time consecutively, to prevent bruteforce or dictionary attacks
		if(failedLoginCount==null)
			failedLoginCount = 0;
		if(failedWaitTime==null)
			failedWaitTime = initWaitTime;
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
	
	@CommitAfter
	void onSuccessFromLoginForm() throws IOException 
	{
		if(ssid==null)
			ssid = requestGlobal.getHTTPServletRequest().getSession(true).getId();
		
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
