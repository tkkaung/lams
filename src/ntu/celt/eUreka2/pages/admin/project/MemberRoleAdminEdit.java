package ntu.celt.eUreka2.pages.admin.project;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.project.Home;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class MemberRoleAdminEdit extends AbstractPageAdminProject{
	@Property
	private Project curProj;
	@Property
	private ProjUser pUser;
	private int pUserId;
	@Property
	private User user;
	@Property
	private boolean notifyMember;
	private ProjRole prevPRole;
	private String prevCustomRole;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private PageRenderLinkSource linkSource;

	void onActivate(EventContext ec){
		pUserId = ec.get(Integer.class, 0);
	}
	Object[] onPassivate(){
		return new Object[]{pUserId};
	}
	
	void setupRender(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		pUser = projDAO.getProjUserById(pUserId);
		if(pUser==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "projUserID", pUserId));
		}
		curProj = pUser.getProject();
		user = pUser.getUser();
		
		prevPRole = pUser.getRole();
		
	}
	
	void onPrepareForSubmitFromForm(){
		pUser = projDAO.getProjUserById(pUserId);
		curProj = pUser.getProject();
		user = pUser.getUser();
		prevPRole = pUser.getRole();
		prevCustomRole = pUser.getCustomRoleName();
	}
	
	public void onValidateFormFromForm() {
	
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		//pUser.setRole(role);
		//pUser.setCustomRoleName(cusRoleName);
		projDAO.saveProject(curProj);
		
		//email to notify the user
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					(new Date()).toString(), (new Date()).toString(),
					pUser.getRole().getDisplayName() + (pUser.getCustomRoleName()==null? "("+pUser.getCustomRoleName()+")":""),
					user.getDisplayName(), 
					getCurUser().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, Home.class, curProj.getId()),
					prevPRole.getDisplayName() + (prevCustomRole==null? "("+prevCustomRole+")" : ""),
					pUser.getCustomRoleName());
			List<User> uList = new ArrayList<User>();
			uList.add(user);
			try{
				emailManager.sendHTMLMail(uList, EmailTemplateConstants.PROJECT_MEMBER_REASSIGN, var);
				appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(uList)));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(ManageAdminMember.class, curProj.getId()); 
		
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
	
}
