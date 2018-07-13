package ntu.celt.eUreka2.components;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.BreadcrumbNode;
import ntu.celt.eUreka2.data.ThemeColor;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Preference;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SessionVisitStatistic;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.BindingConstants;

/**
 * Layout component for pages of application webapps.
 */
@Import(library={"context:lib/js/commons1.js" ,"context:lib/js/stickyheader.prototype.js"}, stylesheet="theme/main1.1.css")
public class Layout
{
	public static final String EUREKA2_HIDE_HEADER_IFRAME = "celt.eureka2.hideheader";
	public static final String EUREKA2_CALL_BACK_IFRAME = "celt.eureka2.callbackurl";
	
	
    /** The page title, for the <title> element . */
    @SuppressWarnings("unused")
	@Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title = "eUreka 2.0";
	@Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String currentModule = "";  //if empty or null, then don't display ProjModuleTab
	@Property
    @Parameter(defaultPrefix = BindingConstants.PROP)
    private Project currentProj ;
    @Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String breadcrumb ;     /* input format:    title1=link1?context1:context2,title2=link2?context3:context4 
    									be aware of <title>, if necessary, use Util.encode() before pass it in, 
    									e.g: it contains special character such as , = : ?
    								*/
    @SuppressWarnings("unused")
	@Property
    private List<BreadcrumbNode> breadcrumbNodes;
    @SuppressWarnings("unused")
	@Property
    private BreadcrumbNode breadcrumbNode;
    @SuppressWarnings("unused")
	@Property
    private int rowIndex;
    
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.BLOCK)
    private Block actionBtns;
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.BLOCK)
    private Block inner_content_bot;
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.BLOCK)
    private Block leftMenuBlk;
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.BLOCK)
    private Block legendBlk;
    @SuppressWarnings("unused")
	@Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private Integer totalShowOption;
    
    
    
	@Property
    private User curUser;
    @SuppressWarnings("unused")
	@Property
    private String msg;
    
    
    @Inject
    private ModuleDAO moduleDAO;
    @Inject
    private Request request;
    @Inject
    private RequestGlobals requestGlobal;
    @Inject
    private PageRenderLinkSource linkSource;
    @Inject
    private WebSessionDAO websessionDAO;
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    @SessionState
    private AppState appState ;
    @SessionAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME)
    private String hideHeaderForIframe;

    private ThemeColor themeColor = null;
    @Inject
    @Path("theme/blue/blue.css")
    private Asset blue_css; 
    @Inject
    @Path("theme/green/green.css")
    private Asset green_css; 
    @Inject
    @Path("theme/orange/orange.css")
    private Asset orange_css; 
    @Inject
    @Path("theme/purple/purple.css")
    private Asset purple_css; 
    
    @Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    private String buildNumber ; 
    
    @SessionAttribute(Layout.EUREKA2_CALL_BACK_IFRAME)
    private String callBackURLForIframe;

    
    void setupRender(){
    	//TEST DEBUG
    	//System.err.println(".......callBackURLForIframe= "+ callBackURLForIframe );
		
    	//TEST DEBUG
    	//hideHeaderForIframe = "0";
    	
    	//callBackURLForIframe = "https://intulearnuat.ntu.edu.sg/ntu-app/app/dashboard.html#/course-launch/iframerubricassessment";
    	//callBackURLForIframe = "https://google.com";
    	//Util.sendHttpGETAsync(callBackURLForIframe);
		
    	
    	curUser = websessionDAO.getCurrentUser(ssid);
    	Preference pref = websessionDAO.getCurrentPreference(ssid);
    	if(pref!=null ){
    		themeColor = pref.getThemeColor();
    	}
    	
    	breadcrumbNodes = parseBreadcrumb(breadcrumb);
    	if(hideHeaderForIframe == "2"){
    		if(breadcrumbNodes != null && breadcrumbNodes.size()>0){
    			breadcrumbNodes.remove(0);
    		}
    	}
    	
    	
    	if(curUser == null)
    		return;
    	
    	ProjRole pRole = null;
    	if(currentProj != null){
    		ProjUser pu = currentProj.getMember(curUser);
    		if(pu !=null){
    			pRole = pu.getRole();
    		}
    	}
    	
    	websessionDAO.addSessesionVististatistic(new SessionVisitStatistic(curUser.getUsername(),
    			request.getPath(),requestGlobal.getActivePageName(),  new Date(),
    			requestGlobal.getHTTPServletRequest().getRemoteHost(),ssid
    			, currentProj, pRole, currentModule));
    	
    }
    
    public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
    public boolean isEqual(int a, int b){
		return (a == b) ;
	}
    public Asset getThemeColorCss(){
    	if(themeColor!=null){
    		switch(themeColor){
    		case DEFAULT:
    			break;
			case BLUE : 
				return blue_css;
			case GREEN : 
				return green_css;
			case ORANGE : 
				return orange_css;
			case PURPLE :
				return purple_css;
    		}
    	}
    	return null;
    }
   
    
    public List<BreadcrumbNode> parseBreadcrumb(String breadcrumbStr){
    	if(breadcrumbStr==null)
    		return null;
    	
    	List<BreadcrumbNode> bcList = new ArrayList<BreadcrumbNode>();
    	
    	//expect breadcrumb in format:    title1=link1?context1:context2,title2=link2?context3:context4 
    	//parse input
    	String nodes[] = breadcrumbStr.split(",");
    	for(String node : nodes){
    		node = node.trim();
    		int i = node.indexOf("=");
    		if(i>-1){
	    		String title = node.substring(0, i);
	    		String link = node.substring(i+1);
	    		BreadcrumbNode bcNode = new BreadcrumbNode();
	    		bcNode.setTitle(Util.decode(title));
		
	    		int j = link.indexOf("?");
	    		if(j>-1){
	    			String pageName = link.substring(0, j);
	    			String contxts = link.substring(j+1);
	    			Object contexts[] = contxts.split(":");
	    			bcNode.setLink(linkSource.createPageRenderLinkWithContext(pageName, contexts));
	    		}
	    		else{
	    			bcNode.setLink(linkSource.createPageRenderLink(link));
	    		}
	    		bcList.add(bcNode);
    		}
    	}
    	return bcList;
    }
    
    
    public String getSpace(){
    	return "&nbsp;";
    }
    
    public boolean isLastNode(int i, List<Object> objs){
    	if(i== objs.size()-1)
    		return true;
    	return false;
    }
    public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
	
	public long getSessionValidTime(){
		return  Config.getInt("session.validtimelimit")* 1000; //*1000 to convert from second to mili-second
	}
	public long getSessionValidTimeLess2mn(){
		return  getSessionValidTime() - 120000; 
	}
	public long getKeepSessionAliveTime(){
		return  1200000;  //20*60*1000
	}
	@Inject
	private ComponentResources componentResources;
	public String getKeepAliveURL(){
		Link link = componentResources.createEventLink("keepAlive");
		return link.toURI();
	}
	Object onKeepAlive(){
		return null; //do nothing, purpose is to update last-active-time
	}
	
	
	
	
	@CommitAfter
	public Object onActionFromLogout(){
		String redirectPage = Config.getString("authen.logout.redirectpage");
		String ssid = (String) request.getSession(true).getAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME);
		websessionDAO.cleanWebSessionsCookies();
		websessionDAO.deleteWebSessionData(ssid);
		request.getSession(true).setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, null);
		request.getSession(true).invalidate();
		
		
		//clear invalid sessions //TODO: put this as auto run task 
		websessionDAO.cleanWebSessions();
		
		
		return linkSource.createPageRenderLink(redirectPage);
	}

	public boolean hasMsgToDisplay(){
		return appState.hasMsgsToDisplay();
	}
	
	@Cached
	public List<String> getInfoMsgs() {
		return appState.popInfoMsgs();
	}
	@Cached
	public List<String> getWarningMsgs() {
		return appState.popWarningMsgs();
	}
	@Cached
	public List<String> getErrorMsgs() {
		return appState.popErrorMsgs();
	}
	
	public String getServerId(){
		return Config.getString("server.id");
	}
	
	@Inject
	private Block msgsBlk;
	public String getLoadMsgsURL(){
		Link link = componentResources.createEventLink("LoadMsgsBlk");
		return link.toURI();
	}
	Object onLoadMsgsBlk(){
		return msgsBlk;
	}
	
	public int getYear(){
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	/*public boolean isHideHeaderForIframe(){
		if(hideHeaderForIframe == null)
			return false;
		if(hideHeaderForIframe.equals("1"))
			return true;
		return false;
	}*/
	public int getHideHeaderIDForIframe(){
		if(hideHeaderForIframe == null)
			return 0;
		if(hideHeaderForIframe.equals("1"))
			return 1;
		if(hideHeaderForIframe.equals("2"))
			return 2;
		return 0;
	}
	
	public String getBuildNumber(){
    	return buildNumber ;
    }
}
