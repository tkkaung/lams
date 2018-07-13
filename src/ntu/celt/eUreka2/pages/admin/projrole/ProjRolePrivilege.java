package ntu.celt.eUreka2.pages.admin.projrole;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.data.ViewMode;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class ProjRolePrivilege {
	@SuppressWarnings("unused")
	@Property
	private ProjRole _projRole;
	@SuppressWarnings("unused")
	@Property
	private List<ProjRole> _projRoles;
	@SuppressWarnings("unused")
	@Property
	private Privilege _priv;
	@SuppressWarnings("unused")
	@Property
	private List<Privilege> _privs;
	@Property
	private List<PrivilegeType> _privTypes;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeType _privType;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private PrivilegeType _showType;
	@Property
	private List<PrivilegeType> _privTypesShow;
	
	@Persist
	@Property
	private ViewMode _mode; 
	
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private PrivilegeDAO _privDAO;
	@Inject
	private Messages _messages;
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
	
	
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_projRoles = _projRoleDAO.getAllRole();
		_privTypes = new ArrayList<PrivilegeType>();
		PrivilegeType[] privTypes = PrivilegeType.values();
		for(PrivilegeType pt : privTypes){
			_privTypes.add(pt);
		}
		//exclue PrivilegeType.SYSTEM
		_privTypes.remove(PrivilegeType.SYSTEM);
		
		if(_showType==null){
			_privTypesShow = _privTypes;
		}
		else{
			_privTypesShow = new ArrayList<PrivilegeType>();
			_privTypesShow.add(_showType);
		}
		
		if(_mode==null)
			_mode = ViewMode.VIEW;
	}
	void onPrepareForSubmitFromForm(){
		
	}
	
	public List<Privilege> getPrivs(PrivilegeType pt){
		return  _privDAO.getPrivilegesByType(pt);
	}
	
	void onActionFromEditModeBtn() {
		_mode = ViewMode.EDIT;
	}
	void onActionFromViewModeBtn() {
		_mode = ViewMode.VIEW;
	}

/*	@CommitAfter
	void onPrivYesClicked(int projRoleId, String privId) {
		ProjRole role = _projRoleDAO.getRoleById(projRoleId);
		Privilege priv = _privDAO.getPrivilegeById(privId);
		if(role!=null && priv!=null){
			//toggle to No //no need to check isExist because it is a java.util.Set
			role.getPrivileges().remove(priv);
			_projRoleDAO.updateRole(role);
		}
	}
	@CommitAfter
	void onPrivNoClicked(int projRoleId, String privId) {
		ProjRole role = _projRoleDAO.getRoleById(projRoleId);
		Privilege priv = _privDAO.getPrivilegeById(privId);
		if(role!=null && priv!=null){
			//toggle to Yes //no need to check isExist because it is a java.util.Set
			role.getPrivileges().add(priv);
			_projRoleDAO.updateRole(role);
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
				
				ProjRole role = _projRoleDAO.getRoleById(Integer.parseInt(rID));
				Privilege priv = _privDAO.getPrivilegeById(pID);
				if(role!=null && priv!=null){
					role.getPrivileges().add(priv);
					_projRoleDAO.updateRole(role);
				}
			}
		}
		
		if(changedToNoIDs!=null && !changedToNoIDs.isEmpty()){
			String rIDpIDs[] = changedToNoIDs.split(",");
			for(String rIDpID : rIDpIDs){
				String temp[] = rIDpID.split(":");
				String rID = temp[0];
				String pID = temp[1];
				
				ProjRole role = _projRoleDAO.getRoleById(Integer.parseInt(rID));
				Privilege priv = _privDAO.getPrivilegeById(pID);
				if(role!=null && priv!=null){
					role.getPrivileges().remove(priv);
					_projRoleDAO.updateRole(role);
				}
			}
		}
		
	}
	

	public boolean isEditMode(){
		if(ViewMode.EDIT.equals(_mode))
			return true;
		return false;
	}
	public boolean containsPriv(ProjRole projRole, Privilege priv){
		if(projRole.getPrivileges().contains(priv))
			return true;
		return false;
	}
	public String containsPrivChecked(ProjRole projRole, Privilege priv){
		if(containsPriv(projRole, priv))
			return "checked";
		return null;
	}
	
	
	public SelectModel getPrivTypeModel(){
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (PrivilegeType pt : _privTypes) {
			OptionModel optModel = new OptionModelImpl(pt.name(), pt);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
}
