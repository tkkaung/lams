package ntu.celt.eUreka2.pages.modules.forum;


import java.util.Date;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

public class ForumEdit extends AbstractPageForum {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	@Property
	private String projId ;
	
	@Property
	private Forum forum;
	private Long forumId;
	@Property
	private boolean notifyMember;
	
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private SysRoleDAO sysRoleDAO;
	
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			forumId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			projId = ec.get(String.class, 0);
			forumId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(projId==null)
			return new Object[] {forumId};
		else
			return new Object[] {projId, forumId};
	}
	public boolean isCreateMode(){
		if(forumId == 0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){ //create new
			curProj = projDAO.getProjectById(projId);
			if(curProj==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
			}
			if(!canCreateForum(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
		}
		else{ //edit
			forum = forumDAO.getForumById(forumId);
			if(forum==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ForumId", forumId));
			curProj = forum.getProject();
			
			if(! canEditForum(forum)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		
		
	}
	
	void onPrepareFromForm(){
		if(isCreateMode()){
			curProj = projDAO.getProjectById(projId);
			if(forum==null)
				forum = new Forum();
		}
		else{
			forum = forumDAO.getForumById(forumId);
			curProj = forum.getProject();
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(isCreateMode()){ //creating new
			forum.setProject(curProj);
			forum.setCreator(getCurUser());
			forum.setCreateDate(new Date());
			
		}
		forum.setModifyDate(new Date());
		forumDAO.saveForum(forum);
		
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					forum.getCreateDate().toString(), forum.getModifyDate().toString(),
					forum.getName(), 
					Util.truncateString(Util.stripTags(forum.getDescription() ), Config.getInt("max_content_lenght_in_email")), 
					forum.getCreator().getDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, ForumView.class, forum.getId())
					, null, null);
			try{
				if(isCreateMode()){
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_ADDED, var);
				}
				else {
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_EDITED, var);
				}
				appState.recordInfoMsg(messages.get("notification-sent-to-members"));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(ForumIndex.class, curProj.getId());
	}
	
	
	
}
