package ntu.celt.eUreka2.pages.admin.user;

import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.slf4j.Logger;

public class UserCreate extends AbstractPageAdminUser {
	@Property
	private User user;
	@Property
	@Validate("required")
	private String confirmPassword;

	@Inject
	private UserDAO userDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals requestGlobals;

	@Inject
	private Messages messages;

	@Component(id = "userForm")
	private BeanEditForm userForm;
	@Component(id = "username")
	private TextField usernameField;
	@Component(id = "password")
	private PasswordField passwordField;
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		user = new User();
		user.setSysRole(sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER));
		user.setSchool(schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS));
	}
	void onPrepareForSubmitFromUserForm(){
		user = new User();
		user.setSysRole(sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER));
		user.setSchool(schoolDAO.getSchoolByName(PredefinedNames.SCHOOL_OTHERS));
	}
	
	void onValidateFormFromUserForm() {
		if (userDAO.isUsernameExist(user.getUsername())) {
			userForm.recordError(usernameField, messages.format("duplicate-key-exception", "Username", user.getUsername()));
		}
		if (confirmPassword != null && !confirmPassword.equals(user.getPassword())) {
			userForm.recordError(passwordField, messages.get("password-not-match"));
		}
	}

	@CommitAfter
	Object onSuccessFromUserForm() {
		user.setPassword(Util.generateHashValue(user.getPassword()));
		user.setCreateDate(new Date());
		user.setModifyDate(new Date());
		user.setIp( requestGlobals.getHTTPServletRequest().getRemoteAddr());
		
		userDAO.save(user);
		return UserIndex.class;
	}

}
