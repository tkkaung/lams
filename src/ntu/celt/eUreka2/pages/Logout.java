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
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;


public class Logout
{
	
	@Property
	private String username = "";
	
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
	private Request request;
	 
	void onActivate(EventContext ec) {
		if(ec.getCount()>0)
			username = ec.get(String.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {username};
	}
	
	Object setupRender(){
		String redirectPage = Config.getString("authen.logout.redirectpage");
		String ssid = (String) request.getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME);
		webSessionDAO.cleanWebSessionsCookies();
		webSessionDAO.deleteWebSessionData(ssid);
		request.getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, null);
		request.getSession(true).invalidate();
		
		
		//clear invalid sessions //TODO: put this as auto run task 
		webSessionDAO.cleanWebSessions();
		
		
		return linkSource.createPageRenderLink(redirectPage);
	}
	
	
}
