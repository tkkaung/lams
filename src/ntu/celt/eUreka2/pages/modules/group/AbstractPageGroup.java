package ntu.celt.eUreka2.pages.modules.group;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;

public abstract class AbstractPageGroup {
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public String getModuleName(){
		return PredefinedNames.PROJECT_INFO;
	}
	
	public String encode(String str){
		return Util.encode(str);
	}
	public String truncateStr(String str){
		return Util.truncateString(str, 50);
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[] {o1, o2};
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
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
	public boolean canEnrollTutor(Project proj){
		//allow ALL
		return true;
		/*
		if(proj.hasModule(PredefinedNames.MODULE_LEADERSHIP_PROFILING)){
			return true;
		}
		return false;*/
	}
	
	public List<User> getNonLeaderMembers(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(!isLeader(pu))
				uList.add(pu.getUser());
		}
		return uList;
	}
	public List<User> getLeaderMembers(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(isLeader(pu))
				uList.add(pu.getUser());
		}
		return uList;
	}
	public boolean isLeader(ProjUser pu){
		if(pu.hasPrivilege(PrivilegeProject.IS_LEADER))
			return true;
		return false;
	}
	public List<User> getAllAssessees(Project p, Group group){
		List<User> uList = new ArrayList<User>();
		
		uList.addAll(getNonGroupedUsers(group));
		
		for(GroupUser gu : group.getGroupUsers()){
			List<User> guList = gu.getUsers();
			Collections.sort(guList, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
			uList.addAll(guList);
		}
		
		return uList;
	}

	public List<User> getNonGroupedUsers(Group group){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : group.getProject().getMembers()){
			if(!isLeader(pu))
				uList.add(pu.getUser());
		}
		
		for(int i=0; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			uList.removeAll(gu.getUsers());
		}
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return uList;
	}

	
	public String getGroupTypeNumber(Group group, User user){
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return ""+ gu.getGroupNum();
		else
			return "";
	}
	public String getGroupTypeName(Group group, User user){
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return gu.getGroupNumNameDisplay();
		else
			return "";
	}
	public GroupUser getGroupUser(Group group, User user){
		for(int i=0 ; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			if(gu.getUsers().contains(user)){
				return gu;
			}
		}
		return null;
	}
	
	public String getIsGroupBeingUsedModuleName(Group group){
		if(groupDAO.isGroupBeingUseAS(group))
			return PredefinedNames.MODULE_ASSESSMENT;
		if(groupDAO.isGroupBeingUsePE(group))
			return PredefinedNames.MODULE_PEER_EVALUATION;
		
		return "";
	}
}
