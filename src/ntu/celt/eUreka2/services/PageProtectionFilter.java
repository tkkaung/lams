//based on http://jumpstart.doublenegative.com.au:8080/jumpstart/examples/infrastructure/protectingpages
package ntu.celt.eUreka2.services;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.pages.Index;
import ntu.celt.eUreka2.pages.Login;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

/**
 * A service that protects pages. It examines each {@link org.apache.tapestry5.services.Request}
 * and redirects it to the login page if the user is not logged in AND 
 * the requested page is not annotated with {@link ntu.celt.eUreka2.annotation.PublicPage}.
 * when user already logged in, It checks for annotation {@link ntu.celt.eUreka2.annotation.RequirePrivilege},
 * If the user does not have required privilege, {@link ntu.celt.eUreka2.dao.NotAuthorizedAccessException}
 * is thrown.
 * <p>
 * if both {@link ntu.celt.eUreka2.annotation.PublicPage} and {@link ntu.celt.eUreka2.dao.NotAuthorizedAccessException}
 * are presented, {@link ntu.celt.eUreka2.annotation.PublicPage} takes precedence
 * <p>
 * To use it, insert it as a service into Tapestry's dispatch chain as we do in AppModule: 
 * public static void contributeComponentRequestHandler(OrderedConfiguration configuration) {
 *  	configuration.addInstance("PageProtection", PageProtectionFilter.class);
 * 	}
 * 
 */
public class PageProtectionFilter implements ComponentRequestFilter {
	
	private final PageRenderLinkSource _linkSource;
	private final ComponentSource _componentSource;
	private final Response _response;
	private final RequestGlobals _reqGlobal;
	private WebSessionDAO _websessionDAO;
	private final Logger _logger;
	
	/**
	 * Receive all the services needed as constructor arguments. 
	 * When we bind this service, T5 IoC will provide all the services!
	 */
	public PageProtectionFilter(PageRenderLinkSource pageRenderLinkSource,
			ComponentSource componentSource, Response response, RequestGlobals reqGlobal, 
			WebSessionDAO websessionDAO, Logger logger) {  
		_linkSource = pageRenderLinkSource;
		_response = response;
		_reqGlobal = reqGlobal;
		_componentSource = componentSource;
		_websessionDAO = websessionDAO;
		_logger = logger;
		
		_response.setHeader("X-Frame-Options", "SAMEORIGIN");
		
	}

	@Override
	public void handleComponentEvent(ComponentEventRequestParameters parameters,
			ComponentRequestHandler handler) throws IOException {
		if (isAuthorisedToPage(parameters.getActivePageName(), parameters.getEventContext())) {
			handler.handleComponentEvent(parameters);
		}
		else {
			// The method will have redirected us to the login page
			return;
		}
	}
	
	@Override
	public void handlePageRender(PageRenderRequestParameters parameters,
			ComponentRequestHandler handler) throws IOException {
		if (isAuthorisedToPage(parameters.getLogicalPageName(), parameters.getActivationContext())) {
			handler.handlePageRender(parameters);
		}
		else {
			// The method will have redirected us to the login page
			return;
		}
	}
	
	public boolean isAuthorisedToPage(String requestedPageName, EventContext eventContext) throws IOException {
		// If the requested page is annotated @PublicPage
		Component page = _componentSource.getPage(requestedPageName);
		boolean PublicPage = page.getClass().getAnnotation(PublicPage.class) != null;

		if (PublicPage) {
			return true;
		}
		
		HttpServletRequest request = _reqGlobal.getHTTPServletRequest();
		String ssid = (String) request.getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME);
		WebSessionData ws = _websessionDAO.getWebSessionById(ssid);
		
		if(ws==null){
			//try from Tomcat session ID
			ssid = (String) request.getSession(true).getId();
			ws = _websessionDAO.getWebSessionById(ssid);
			
			if(ws==null){
			
				//try load from cookies
				ws = _websessionDAO.getWebSessionFromCookies(request);
				if(ws!=null){
					request.getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ws.getId());
				}
			}
		}
		
		
		//get expired time
		Calendar expiredTime = Calendar.getInstance();
		int validTime = Config.getInt("session.validtimelimit");
		expiredTime.add(Calendar.SECOND, (-1)*validTime);
		
		if(ws==null 
			//|| ws.getLastActiveTime().before(expiredTime.getTime())
			){ //if no valid session OR session expired, redirect to login page
			//construct the callBackURL
			String file = request.getRequestURI();
			if (request.getQueryString() != null) {
			   file += '?' + request.getQueryString();
			}
			URL reconstructedURL = new URL(request.getScheme(),
			                               request.getServerName(),
			                               request.getServerPort(),
			                               file);
			
			
			
			
			Link loginPageLink;
			Link homeLink = _linkSource.createPageRenderLink(Index.class);
		//	System.out.println(homeLink.toURI() + ".........." +request.getRequestURI());	
		//	_logger.error(homeLink.getBasePath() + ".........." +request.getRequestURI());
			if(! (homeLink.toURI().endsWith(request.getRequestURI()))){
				loginPageLink = _linkSource.createPageRenderLinkWithContext(
						Login.class, reconstructedURL.toString());
			}
			else{
				loginPageLink = _linkSource.createPageRenderLink(Login.class);
			}
			_response.sendRedirect(loginPageLink);
			

			/*
			String idp = Config.getString("saml.idp"); //"uat";
			String target = URLEncoder.encode(reconstructedURL.toString(), "UTF-8");
			String samlAccessLink = Config.getString("base.url") + "/saml/access?idp="+idp+"&TARGET=" + target;
			_response.sendRedirect(samlAccessLink);
			
			*/
			
			return false;
		}
		
		
			
		//update user's last active time
		ws.setLastActiveTime(new Date());
		_websessionDAO.immediateSaveWebSessionData(ws);
		_websessionDAO.setWebSessionToCookies(ws, request);
		
		return true;
	}
	
	

}
