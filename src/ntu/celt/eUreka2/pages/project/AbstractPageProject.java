package ntu.celt.eUreka2.pages.project;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.ApproveItemStatus;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ProjTypeSetting;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageProject {
	
	@Inject
	private Messages messages;
	@Inject
	private ProjExtraInfoDAO projExtraInfoDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public String getModuleName(){
		return PredefinedNames.PROJECT_INFO;
	}
	
	public String getModuleDisplayName(Project curProj, Module module){
		if(PredefinedNames.PROJTYPE_SENATE.equalsIgnoreCase(curProj.getType().getName())){
			if(PredefinedNames.MODULE_FORUM.equalsIgnoreCase(module.getName()))
				return messages.get("senate-module-forum-name");
			if(PredefinedNames.MODULE_RESOURCE.equalsIgnoreCase(module.getName()))
				return messages.get("senate-module-resources-name");
		}
		
		return module.getDisplayName();
	}
	public String getSpace(){
		return "&nbsp;";
	}
	
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
	}
	public String formatDateTime(Date date){
		return Util.formatDateTime(date, messages.get("datetimefield-format"));
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	public String truncateStringShort(String str){
		return Util.truncateString(str, 50);
	}
	public boolean isFirst(int index, List<Object> list){
		if(index == 0)
			return true;
		return false;
	}
	public boolean isLast(int index, List<Object> list){
		if(index == list.size()-1)
			return true;
		return false;
	}
	public String getContextPath(){
		return request.getContextPath();
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
	
	public boolean canCreateProject(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.CREATE_PROJECT))
			return true;
		
		return false;
	}
	public boolean canEditProject(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!proj.getType().getSettings().contains(ProjTypeSetting.CAN_UPDATE_BASE_INFO))
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.UPDATE_PROJECT))
			return false;
		
		return true;
	}
	public boolean canEnrollMember(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER))
			return false;
		
		return true;
	}
	public boolean canEnrollOrgSupervisor(Project proj){
		/*if(! proj.getType().getName().equals(PredefinedNames.PROJTYPE_CAO)){
			return false;
		}
		*/
		/*if(canAdminAccess(proj))
			return true;
		*/
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.ENROLL_ORG_SUPERVISOR))
			return false;
		
		return true;
	}
	public boolean canAssignModule(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.ASSIGN_MODULE))
			return false;
		
		return true;
	}
	public boolean canAssignLeaderShipProfiling(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.LEADSHIP_PROFILING))
			return true;
		return false;
	}
	
	
	public boolean canManageGroup(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.MANAGE_GROUP))
			return false;
		
		return true;
	}
	public boolean canSelfEnroll(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeProject.IS_LEADER))
			return false;
		
		return true;
	}
	public boolean hasSelfEnrollGroups(Project proj){
		List<Group> gList = groupDAO.getGroupsByProject(proj);
		for(int i=gList.size()-1; i>=0; i--){
			Group grp = gList.get(i);
			if(!grp.getAllowSelfEnroll()){
				gList.remove(i);
			}
		}
		if(gList==null || gList.size()==0)
			return false;
		
		return true;
	}
	
	public StreamResponse onRetrieveAttachment(String fId) {
		ProjectAttachedFile att = projDAO.getAttachedFileById(fId);
		if(att==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(att);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + att.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", att.getDisplayName()));
		}
	}
	
	public boolean isCAO(Project project){
		if(project.getType().getName().equals(PredefinedNames.PROJTYPE_CAO))
			return true;
		return false;
	}
	public boolean hasSettingProjScope(Project project){
		if(project.getType().getSettings().contains(ProjTypeSetting.HAS_PROJECT_SCOPE))
			return true;
		return false;
	}
	public boolean canUpdateScope(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.UPDATE_PROJECT_SCOPE))
			return false;
		if(!hasSettingProjScope(proj))
			return false;
		ProjCAOExtraInfo projExtraInfo = projExtraInfoDAO.getProjExtraInfoByProject(proj);
		if(projExtraInfo==null)
			return true;
		if(ApproveItemStatus.PENDING.equals(projExtraInfo.getStatus()))
			return false;
		
		return true;
	}
	public boolean canApprovedScope(Project project){
		if(canAdminAccess(project))
			return true;
		
		if(project.isReference())
			return false;
		ProjUser pu = project.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.APPROVE_PROJECT_SCOPE))
			return false;
		if(!hasSettingProjScope(project))
			return false;
		ProjCAOExtraInfo projExtraInfo = projExtraInfoDAO.getProjExtraInfoByProject(project);
		if(projExtraInfo==null)
			return true;
		
		return true;
	}
	
	public List<User> getApprovers(Project project){
		List<User> approvers = new ArrayList<User>();
		
		for(ProjUser pu : project.getMembers()){
			if(pu.hasPrivilege(PrivilegeProject.APPROVE_PROJECT_SCOPE))
				approvers.add(pu.getUser());
		}
		return approvers;
	}
	
	
}
