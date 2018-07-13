package ntu.celt.eUreka2.pages.admin.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.FilterType;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.UserSearchableField;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.Index;
import ntu.celt.eUreka2.pages.project.Home;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class ManageMember extends AbstractPageAdminProject {

	
	@Property
	private String projId;
	@Property
	private Project curProj;

	private enum SubmitType {ASSIGN, UNASSIGN}; 
	
	@SessionState
	private AppState appState;
	
	@Property
	private String searchText;
	@Property
	private UserSearchableField searchIn;
	@Property
	@Persist
	private List<User> resultUsers;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@SuppressWarnings("unused")
	@Property
	private List<ProjUser> selMembers;
	@SuppressWarnings("unused")
	@Property
	private ProjUser selMember;
	@Property
	private ProjRole projRole;
	private SubmitType submitType;
	@Property
	private boolean notifyMember;
	private final int MAX_USER_RESULT = Config.getInt("ENROLL_USER_MAX_USER_RESULT");
	
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private BeanModelSource source;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
	
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
		
		ProjUser pu = curProj.getMember(getCurUser());
		if(!pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}

		if(projRole == null)
			projRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_MEMBER);
		
		if(resultUsers==null)
			resultUsers = new ArrayList<User>();
		selMembers = curProj.getMembers();
	}
	
	void onPrepareForSubmit(){
		curProj = projDAO.getProjectById(projId);
		
		selMembers = curProj.getMembers();
	}
	

	void onSuccessFromSearchForm(){
		int count = userDAO.countSearchUsers(FilterType.CONTAIN, searchText, searchIn, true, null);
		if(count>MAX_USER_RESULT){
			appState.recordWarningMsg(messages.format("search-return-x-result-only-display-y", count, MAX_USER_RESULT));
		}
		resultUsers = userDAO.searchUsers(FilterType.CONTAIN, searchText, searchIn, true, null, 0, MAX_USER_RESULT);
		
		//remove users already been assigned
		for(int i=resultUsers.size()-1; i>=0; i--){
			User u = resultUsers.get(i);
			if(curProj.hasMember(u)){
				resultUsers.remove(i);
			}
		}
	}
	
	public SelectModel getProjRoleModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (ProjRole pr : curProj.getType().getRoles()) {
			OptionModel optModel = new OptionModelImpl(pr.getDisplayName(), pr);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
	
	public boolean shouldnotUnassign(User user){
		if(curProj.getCreator()!=null && curProj.getCreator().equals(user))
			return true;
		if(getCurUser().equals(user))
			return true;
		return false;
	}
	
	
	void onSelectedFromAssign() {
		submitType = SubmitType.ASSIGN;
	}
	void onSelectedFromUnassign() {
		submitType = SubmitType.UNASSIGN;
	}
	
	@Component(id = "assignForm")
	private Form assignForm;
	void onValidateFormFromAssignForm() {
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("chkBox");
			if(selUserId==null || selUserId.length==0){
				assignForm.recordError(messages.get("select-at-least-one-from-left"));
			}
		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selProjUserId = request.getParameters("selChkBox");
			if(selProjUserId==null || selProjUserId.length==0){
				assignForm.recordError(messages.get("select-at-least-one-from-right"));
			}
		}
		
	}
	@CommitAfter
	void onSuccessFromAssignForm(){
		if(SubmitType.ASSIGN.equals(submitType)){
			String[] selUserId = request.getParameters("chkBox");
			for(String uId : selUserId){
				User u = userDAO.getUserById(Integer.parseInt(uId));
				curProj.addMember(new ProjUser(curProj, u, projRole));	
				
				//update resultUsers list
				resultUsers.remove(u);
				
				//email to notify the user
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
			projDAO.saveProject(curProj);
		}
		else if(SubmitType.UNASSIGN.equals(submitType)){
			String[] selProjUserId = request.getParameters("selChkBox");
			for(String puId : selProjUserId){
				ProjUser pu = projDAO.getProjUserById(Integer.parseInt(puId));
				User u = pu.getUser();
				curProj.removeMember(pu);
				userDAO.save(u);
				
				//update resultUsers list
				resultUsers.add(u);
				
				//email to notify the user
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
			projDAO.saveProject(curProj);
		}
	}
	
	public BeanModel<ProjUser> getModel() {
		BeanModel<ProjUser> model = source.createEditModel(ProjUser.class, messages);
		model.exclude("customRoleName");
		
		model.add("chkBox", null);
		model.add("username", propertyConduitSource.create(ProjUser.class, "user.username"));
		model.add("displayName", propertyConduitSource.create(ProjUser.class, "user.displayname"));
		model.add("role", propertyConduitSource.create(ProjUser.class, "role.displayname"));
		model.add("edit", null);
		return model;
	}
}
