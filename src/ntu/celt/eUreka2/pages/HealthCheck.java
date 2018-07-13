package ntu.celt.eUreka2.pages;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.LdapService;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileStreamResponse;
import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SymbolConstants;
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
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;


/**
 * page to check if the page can be accessed, and can query database
 *
 */
@PublicPage
public class HealthCheck
{
	
	@Property
	private int userID = 1;
	@Property
	private User user;
	private String status;
	
	
	@Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    private String buildNumber ; 
    
    
	
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
	
	
	void onActivate(EventContext ec) {
		
	}
	Object[] onPassivate() {
		return new Object[] {};
	}
	
	void setupRender(){
		user = userDAO.getUserById(userID);
		if(user!=null){
			status = "Database connection OK";
		}
		else{
			status = "Cannot file default data on database";
		}
	}
	
	
	
	public String getHostname(){
    	return requestGlobal.getHTTPServletRequest().getServerName();
    }
	public String getBuildNumber(){
    	return buildNumber;
    }
	public String getReqTime(){
    	return (new Date()).toString();
    }
	public String getStatus(){
		return status;
	}
	public String getFolderStatus(){
		String path  = Config.getString(Config.VIRTUAL_DRIVE) ;
		File f = new File(path);
		if(f.exists()){
			return "OK, Folder exist.";
		}
			
		
		return "No, Not found the folder";
	}
	
	public String getServerId(){
		return Config.getString("server.id");
	}
	
}
