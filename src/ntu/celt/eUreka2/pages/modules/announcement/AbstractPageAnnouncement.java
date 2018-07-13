package ntu.celt.eUreka2.pages.modules.announcement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
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
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;

public abstract class AbstractPageAnnouncement {
	public String getModuleName(){
		return PredefinedNames.MODULE_ANNOUNCEMENT;
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private Messages messages;
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
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_ANNOUNCEMENT);
	}
	public String formatDateTime(Date date){
		return Util.formatDateTime(date, messages.get("datetimefield-format"));
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
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
	
	public boolean canCreateAnnouncement(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAnnouncement.CREATE_ANNOUNCEMENT  )){
			return true;
		}
		return false;
	}
	public boolean canUpdateAnnouncement(Announcement annmt){
		Project proj = annmt.getProject();
		if(canAdminAccess(proj))
			return true;
		if(annmt.getCreator().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAnnouncement.UPDATE_ANNOUNCEMENT  )){
			return true;
		}
		return false;
	}
	public boolean canDeleteAnnouncement(Announcement annmt){
		Project proj = annmt.getProject();
		if(canAdminAccess(proj))
			return true;
		if(annmt.getCreator().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAnnouncement.DELETE_ANNOUNCEMENT  )){
			return true;
		}
		return false;
	}
	public boolean canViewAnnouncement(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAnnouncement.VIEW_ANNOUNCEMENT  )){
			return true;
		}
		return false;
	}
	public boolean canAccessAnnouncement(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null   ){
			return true;
		}
		return false;
	}
	public boolean canManageAnnouncement(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAnnouncement.ADMIN_MANAGE  )){
			return true;
		}
		return false;
	}
	
	
	
	
}
