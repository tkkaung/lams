package ntu.celt.eUreka2.pages.admin.user;


import java.util.Date;

import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class UserEdit extends AbstractPageAdminUser{
	@Property
	private User user;
	@Property
	private int userId;
	@SuppressWarnings("unused")
	@Property
	private SysroleUser sRoleUser;
	@Property
	private SysRole eRole1;
	@Property
	private SysRole eRole2;
	@Property
	private SysRole eRole3;
	@Property
	private SysRole eRole4;
	@Property
	private SysRole eRole5;
	@Property
	private ProjType eProjType1;
	@Property
	private ProjType eProjType2;
	@Property
	private ProjType eProjType3;
	@Property
	private ProjType eProjType4;
	@Property
	private ProjType eProjType5;
	@Property
	private School eSchl1;
	@Property
	private School eSchl2;
	@Property
	private School eSchl3;
	@Property
	private School eSchl4;
	@Property
	private School eSchl5;
	
	
	@Inject
	private UserDAO userDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	@Component
	private Form userForm;
	@Component(id = "username")
	private TextField usernameField;
	
	void onActivate(int id) {
		userId = id;
	}
	int onPassivate() {
		return userId;
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		user = userDAO.getUserById(userId);
		if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "User", userId));
		}
	}
	void onPrepareForSubmitFromUserForm() {
		user = userDAO.getUserById(userId);
	}
	
	void onValidateFormFromUserForm() {
		User temp = userDAO.getUserByUsername(user.getUsername());
		if (temp!=null && temp.getId()!=user.getId()) {
			userForm.recordError(usernameField, messages.format("duplicate-key-exception", "Username", user.getUsername()));
		}
	}

	@CommitAfter
	Object onSuccessFromUserForm() {
		user.setModifyDate(new Date());
	//	user.setIp(wsData.getIp());
		
		saveExtraRole(eRole1, eProjType1, eSchl1);
		saveExtraRole(eRole2, eProjType2, eSchl2);
		saveExtraRole(eRole3, eProjType3, eSchl3);
		saveExtraRole(eRole4, eProjType4, eSchl4);
		saveExtraRole(eRole5, eProjType5, eSchl5);
		
		userDAO.save(user);
		
		if(user.getId() == getCurUser().getId()){
		}
		
		return linkSource.createPageRenderLinkWithContext(UserView.class, user.getId());
	}
	private void saveExtraRole(SysRole eRole, ProjType eProjType, School eSchl){
		if(eRole!=null){
			SysroleUser su = new SysroleUser();
			su.setSysRole(eRole);
			su.setUser(user);
			if(eRole.getName().equals(PredefinedNames.SYSROLE_PROJTYPE_ADMIN))
				su.setParam(eProjType.getId());
			if(eRole.getName().equals(PredefinedNames.SYSROLE_SCHOOL_ADMIN))
				su.setParam(eSchl.getId());
			
			user.addExtraRole(su);
		}
	}
	
	@CommitAfter
	void onRemoveRole(long eRoleUserId){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		SysroleUser eRoleUser = sysRoleDAO.getSysroleUserById(eRoleUserId);
		if(eRoleUser==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "SysRoleUserID", eRoleUserId));
		
		sysRoleDAO.deleteSysroleUser(eRoleUser);
		
	}

	
}
