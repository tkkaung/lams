package ntu.celt.eUreka2.pages.modules.message;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.modules.message.PrivilegeMessage;

public abstract class AbstractPageMessage {
	public String getModuleName(){
		return PredefinedNames.MODULE_MESSAGE;
	}
	
	@Inject
	private ModuleDAO moduleDAO;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_MESSAGE);
	}
	public String encode(String str){
		return Util.encode(str);
	}
	
	public String getTypeNameDisplay(MessageType type){
		return Util.capitalize(type.name());
	}
	public Object[] getMsgTypes(){
		return MessageType.values();
	}
	public Object[] getParams(Object pid, Object type){
		return new Object[] {pid, type};
	}
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	
	
	
	protected List<Integer> getAccessibleSchlIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		return idList;
	}
	public boolean canAdminAccess(Project proj){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC)){
			List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
			if(proj.getSchool() != null && accessibleSchlIDs.contains(proj.getSchool().getId()) )
				return true;
		}
		return false;
	}
	public boolean canViewMessage(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu ==null || !pu.hasPrivilege(PrivilegeMessage.VIEW_MESSAGE ))){
			return false;
		}
		return true;
	}
	public boolean canCreateMessage(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu ==null || !pu.hasPrivilege(PrivilegeMessage.CREATE_MESSAGE ))){
			return false;
		}
		return true;
	}
	public boolean canReplyMessage(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu ==null || !pu.hasPrivilege(PrivilegeMessage.REPLY_MESSAGE ))){
			return false;
		}
		return true;
	}
	
}
