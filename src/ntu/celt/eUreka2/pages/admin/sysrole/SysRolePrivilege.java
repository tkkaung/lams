package ntu.celt.eUreka2.pages.admin.sysrole;

import java.util.List;

import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.data.ViewMode;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class SysRolePrivilege {
	@SuppressWarnings("unused")
	@Property
	private SysRole _sysRole;
	@SuppressWarnings("unused")
	@Property
	private List<SysRole> _sysRoles;
	@SuppressWarnings("unused")
	@Property
	private Privilege _priv;
	@SuppressWarnings("unused")
	@Property
	private List<Privilege> _privs;
	
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private PrivilegeDAO privDAO;
	@Inject
	private Messages _messages ;
	@Inject
	private Request request;
	
	@Persist
	@Property
	private ViewMode mode; 
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_sysRoles = sysRoleDAO.getAllSysRoles();
		_privs = privDAO.getPrivilegesByType(PrivilegeType.SYSTEM);
		if(mode==null)
			mode = ViewMode.VIEW;
	}
	 

	void onActionFromEditModeBtn() {
		mode = ViewMode.EDIT;
	}
	void onActionFromViewModeBtn() {
		mode = ViewMode.VIEW;
	}
/*	@CommitAfter
	void onPrivYesClicked(int sysRoleId, String privId) {
		SysRole role = sysRoleDAO.getSysRoleById(sysRoleId);
		Privilege priv = privDAO.getPrivilegeById(privId);
		if(role!=null && priv!=null){
			//toggle to No //no need to check isExist because it is a java.util.Set
			role.getPrivileges().remove(priv);
			sysRoleDAO.save(role);
		}
	}
	@CommitAfter
	void onPrivNoClicked(int sysRoleId, String privId) {
		SysRole role = sysRoleDAO.getSysRoleById(sysRoleId);
		Privilege priv = privDAO.getPrivilegeById(privId);
		if(role!=null && priv!=null){
			//toggle to Yes //no need to check isExist because it is a java.util.Set
			role.getPrivileges().add(priv);
			sysRoleDAO.save(role);
		}
	}
*/	
	@CommitAfter
	void onSuccessFromForm(){
		String changedToYesIDs = request.getParameter("changedToYes"); //format: roleID1:privID1,roleID2:privID2 ...
		String changedToNoIDs = request.getParameter("changedToNo");
		
		if(changedToYesIDs!=null && !changedToYesIDs.isEmpty()){
			String rIDpIDs[] = changedToYesIDs.split(",");
			for(String rIDpID : rIDpIDs){
				String temp[] = rIDpID.split(":");
				String rID = temp[0];
				String pID = temp[1];
				
				SysRole role = sysRoleDAO.getSysRoleById(Integer.parseInt(rID));
				Privilege priv = privDAO.getPrivilegeById(pID);
				if(role!=null && priv!=null){
					role.getPrivileges().add(priv);
					sysRoleDAO.save(role);
				}
			}
		}
		
		if(changedToNoIDs!=null && !changedToNoIDs.isEmpty()){
			String rIDpIDs[] = changedToNoIDs.split(",");
			for(String rIDpID : rIDpIDs){
				String temp[] = rIDpID.split(":");
				String rID = temp[0];
				String pID = temp[1];
				
				SysRole role = sysRoleDAO.getSysRoleById(Integer.parseInt(rID));
				Privilege priv = privDAO.getPrivilegeById(pID);
				if(role!=null && priv!=null){
					role.getPrivileges().remove(priv);
					sysRoleDAO.save(role);
				}
			}
		}
		
	}
	

	public boolean isEditMode(){
		if(ViewMode.EDIT.equals(mode))
			return true;
		return false;
	}
	public boolean containsPriv(SysRole sysRole, Privilege priv){
		if(sysRole.getPrivileges().contains(priv))
			return true;
		return false;
	}
	public String containsPrivChecked(SysRole sysRole, Privilege priv){
		if(containsPriv(sysRole, priv))
			return "checked";
		return null;
	}
	
}
