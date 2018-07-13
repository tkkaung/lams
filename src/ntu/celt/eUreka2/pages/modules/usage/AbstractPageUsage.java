package ntu.celt.eUreka2.pages.modules.usage;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.usage.PrivilegeUsage;
import ntu.celt.eUreka2.modules.usage.UsageType;

public abstract class AbstractPageUsage {
	public String getModuleName(){
		return PredefinedNames.MODULE_USAGE;
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_USAGE);
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public String encode(String str){
		return Util.encode(str);
	}
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	public String textarea2html(String str){
		return Util.textarea2html(str); 
	}
	public Object[] getParams(Object o1, Object o2) {
		return new Object[] {o1, o2}; 
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	
	
	public String getTypeDisplay(UsageType type){
		return messages.get("utype-"+type.name());
	}
	public String getContextPath(){
		return request.getContextPath();
	}
	
	public boolean canViewMemberContributions(Project proj){
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeUsage.VIEW_CONTRIBUTION )){
			return true;
		}
		return false;
	}
	
	
	
}
