package ntu.celt.eUreka2.pages.admin.schltypeannouncement;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;


public abstract class AbstractSchlTypeAnnouncement {
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
	
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public String truncateStr(String str){
		return Util.truncateString(str, 50);
	}
	public String encode(String str){
		return Util.encode(str);
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
	protected List<Integer> getAccessibleProjTypeIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_PROJTYPE_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		return idList;
	}
	
	public boolean canManageTypeAnnmts(SchlTypeAnnouncement annmt){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.PROJ_TYPE_ANNOUNCEMENT))
			return false;
		if(annmt.getProjType()==null)
			return false;
		if(getAccessibleProjTypeIDs(getCurUser()).contains(annmt.getProjType().getId()))
			return true;
		return false;
	}
	public boolean canManageSchlAnnmts(SchlTypeAnnouncement annmt){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.PROJ_SCHOOL_ANNOUNCEMENT))
			return false;
		if(annmt.getSchool()==null)
			return false;
		if(getAccessibleSchlIDs(getCurUser()).contains(annmt.getSchool().getId()))
			return true;
		return false;
	}
	
}
