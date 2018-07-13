package ntu.celt.eUreka2.pages.modules.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Path;
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
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;

public abstract class AbstractPageScheduling {
	public String getModuleName(){
		return PredefinedNames.MODULE_SCHEDULING;
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
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_SCHEDULING);
	}
	
	
	
	public String encode(String str){
		return Util.encode(str);
	}
	public String getSpace(){
		return "&nbsp;";
	}
	
	@Inject
	@Path("context:lib/img/prio_low.png")
	private Asset pri1_png;
	@Inject
	@Path("context:lib/img/prio_medium.png")
	private Asset pri2_png;
	@Inject
	@Path("context:lib/img/prio_high.png")
	private Asset pri3_png;
	public Asset getUrgencyLevelIcon(UrgencyLevel ul){
		switch(ul){
			case LOW:
				return pri1_png;
			case MEDIUM:
				return pri2_png;
			case HIGH:
				return pri3_png;
		}
		return null;
	}
	public String getUrgencyTip(UrgencyLevel ul){
		switch(ul){
		case LOW:
			return messages.get("lowUrgency-label");
		case MEDIUM:
			return messages.get("mediumUrgency-label");
		case HIGH:
			return messages.get("highUrgency-label");
		}
		return null;
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
	
	public boolean canCreateMilestone(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu!=null && pu.hasPrivilege(PrivilegeSchedule.CREATE_MILESTONE))
			return true;  
		return false;
	}
	public boolean canCreatePhase(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu!=null && pu.hasPrivilege(PrivilegeSchedule.CREATE_PHASE))
			return true;  
		return false;
	}
	public boolean canCreateTask(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu!=null && pu.hasPrivilege(PrivilegeSchedule.CREATE_TASK))
			return true;  
		return false;
	}
	public boolean canEditMilestone(Milestone m){
		Project proj = m.getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.UPDATE_MILESTONE))
			|| m.getManager().equals(getCurUser()))
			return true;  
		return false;
	}
	public boolean canEditPhase(Phase p){
		Project proj = p.getMilestone().getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.UPDATE_PHASE))
			|| p.getManager().equals(getCurUser()))
			return true;  
		return false;
	}
	
	public boolean canEditTask(Task t){
		Project proj = t.getMilestone().getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.UPDATE_TASK))
			|| t.getManager().equals(getCurUser())
			|| t.hasAssignedPerson(getCurUser()) //probably only edit, not delete
			)
			return true;  
		return false;
	}
	public boolean canDeleteMilestone(Milestone m){
		Project proj = m.getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.REMOVE_MILESTONE))
			|| m.getManager().equals(getCurUser()))
			return true;  
		return false;
	}
	public boolean canDeletePhase(Phase p){
		Project proj = p.getMilestone().getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.REMOVE_PHASE))
			|| p.getManager().equals(getCurUser()))
			return true;  
		return false;
	}
	public boolean canDeleteTask(Task t){
		Project proj = t.getMilestone().getSchedule().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu!=null && pu.hasPrivilege(PrivilegeSchedule.REMOVE_MILESTONE))
			|| t.getManager().equals(getCurUser())
			//|| t.hasAssignedPerson(getCurUser()) //probably only edit, not delete
			)
			return true;  
		return false;
	}
	public boolean canManageVersion(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu!=null && pu.hasPrivilege(PrivilegeSchedule.MANAGE_VERSION))
			return true;  
		return false; 
	}
	
	public boolean canViewScheduling(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return true;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu!=null && pu.hasPrivilege(PrivilegeSchedule.VIEW_SCHEDULE))
			return true;  
		return false; 
	}
	
}
