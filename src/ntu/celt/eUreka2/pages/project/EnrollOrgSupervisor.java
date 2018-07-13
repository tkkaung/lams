package ntu.celt.eUreka2.pages.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.Index;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class EnrollOrgSupervisor extends AbstractPageProject {
	@Property
	private String projId;
	@Property
	private Project curProj;
	@Property
	private String usernameEmail;
	@Property
	private User newUser;
	@Property
	private User resultUser;
	@Property
	private List<User> resultUsers;
	@Property
	private List<User> selMembers;
	@Property
	private User selMember;
	
	@SessionState
	private AppState appState;
	
	
	@Property
	private boolean notifyMember = true;
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	
	
	
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
	}
	Object[] onPassivate() {
		return new Object[] {projId };
	}
	
	void setupRender(){
		curProj = projDAO.getProjectById(projId);
		if(curProj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
		if(!canEnrollOrgSupervisor(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		selMembers = curProj.getUsers();
		
	}
	
	void onPrepareForSubmit(){
		curProj = projDAO.getProjectById(projId);
		if(newUser==null)
			newUser = new User();
		
	}
	
	
	public boolean hasEnrolled(User user){
		if(curProj.getUsers().contains(user)){
			return true;
		}
		return false;
	}
	
	@Inject
	private Block usersBlock;
	Object onSuccessFromCheckUserForm(){
		if(request.isXHR()){
			
			curProj = projDAO.getProjectById(projId);
			
			resultUsers = userDAO.getUserByUsernameOrEmail(usernameEmail);
			
			return usersBlock;
		}
		return null;
	}
	@Inject
	private Block newUserBlock;
	Object onAddNewUser(String usernameEmail){
		if(newUser==null){
			newUser = new User();
			newUser.setEmail(usernameEmail);
		}
		return newUserBlock;
	}
	
	
	private ProjUser curProjUser;
	public ProjUser getCurProjUser(User curUser, Project curProj){
		curProjUser = curProj.getMember(curUser);
		return curProjUser;
	}
	public ProjUser getCurProjUser(){
		return curProjUser;
	}
	
	
	
	@CommitAfter
	void onSuccessFromNewUserForm(){
		//newUser.setEmail(email)
		//newUser.setFirstName(firstName)
		//newUser.setLastName(lastName)
		//newUser.setOrganization(organization)
		//newUser.setJobTitle(jobTitle)
		//newUser.setPhone(phone)
		//newUser.setId(id)
		newUser.setCreateDate(new Date());
		newUser.setModifyDate(new Date());
		newUser.setEnabled(true);
		newUser.setRemarks("Created by "+getCurUser().getUsername());
		//newUser.setSchool(null)
		newUser.setSysRole(sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_EXTERNAL_USER));
		newUser.setUsername(newUser.getEmail());
		newUser.setPassword(Util.generateDefaultPassword(newUser));
		//newUser.setProjects(projects)
		
		userDAO.save(newUser);
		
		ProjRole projRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_ORG_SUPERVISOR);
		curProj.addMember(new ProjUser(curProj, newUser, projRole));	
		
		projDAO.saveProject(curProj);
		appState.recordInfoMsg(messages.format("has-enrolled-x-as-y", newUser.getUsername(), projRole.getDisplayName()));
		
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					(new Date()).toString(), (new Date()).toString(),
					projRole.getDisplayName(), newUser.getDisplayName(), 
					getCurUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, Home.class, curProj.getId()),
					newUser.getUsername(), projRole.getId()+"");
			List<User> uList = new ArrayList<User>();
			uList.add(newUser);
			try{
				emailManager.sendHTMLMail(uList, EmailTemplateConstants.PROJECT_MEMBER_STUDENT_ENROLL, var);
				appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(uList)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		
	}
	
	
	@CommitAfter
	void onEnrollUser(int userId){
		curProj = projDAO.getProjectById(projId);
		if(!canEnrollOrgSupervisor(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		User u = userDAO.getUserById(userId);
		if(u==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserId", userId));
		
		ProjRole projRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_ORG_SUPERVISOR);
		curProj.addMember(new ProjUser(curProj, u, projRole));	
		
		projDAO.saveProject(curProj);
		appState.recordInfoMsg(messages.format("has-enrolled-x-as-y", u.getUsername(), projRole.getDisplayName()));
		
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					(new Date()).toString(), (new Date()).toString(),
					projRole.getDisplayName(), u.getDisplayName(), 
					getCurUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, Home.class, curProj.getId()),
					u.getUsername(), projRole.getId()+"");
			List<User> uList = new ArrayList<User>();
			uList.add(u);
			try{
				emailManager.sendHTMLMail(uList, EmailTemplateConstants.PROJECT_MEMBER_ASSIGN, var);
				appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(uList)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
	}
	@CommitAfter
	void onUnEnrollUser(int userId){
		curProj = projDAO.getProjectById(projId);
		User u = userDAO.getUserById(userId);
		if(u==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserId", userId));
		ProjUser pu = curProj.getMember(u);
		
		if(!canRemoveOrgSupervisor(pu)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		curProj.removeMember(pu);
		userDAO.save(pu.getUser());	
		
		projDAO.saveProject(curProj);
		appState.recordInfoMsg(messages.format("has-unenrolled-x-from-project", u.getUsername()));
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					(new Date()).toString(), (new Date()).toString(),
					pu.getRole().getDisplayName(), u.getDisplayName(), 
					getCurUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, Index.class),
					getCurUser().getEmail(), pu.getCustomRoleName());
			List<User> uList = new ArrayList<User>();
			uList.add(u);
			try{
				emailManager.sendHTMLMail(uList, EmailTemplateConstants.PROJECT_MEMBER_UNASSIGN, var);
				appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(uList)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
	}
	
	
	public boolean canRemoveOrgSupervisor(ProjUser pu){
		if(canAdminAccess(curProj))
			return true;
		if(!canEnrollOrgSupervisor(curProj)){
			return false;
		}
		if(pu.getRole().getName().equals(PredefinedNames.PROJROLE_ORG_SUPERVISOR))
			return true;
		
		return false;
	}
	
}
