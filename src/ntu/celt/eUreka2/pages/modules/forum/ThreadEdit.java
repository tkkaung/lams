package ntu.celt.eUreka2.pages.modules.forum;

import java.util.Date;

import org.slf4j.Logger;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadType;
import ntu.celt.eUreka2.modules.forum.ThreadUser;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

public class ThreadEdit extends AbstractPageForum {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	@Property
	private Forum forum;
	private Long fId;
	@Property
	private Thread thread;
	private Long thId;
	@Property
    private UploadedFile file1;
	@Property
    private UploadedFile file2;
	@Property
    private UploadedFile file3;
	@Property
    private UploadedFile file4;
	@Property
    private UploadedFile file5;
	@SuppressWarnings("unused")
	@Property
	private ForumAttachedFile tempAttFile;
	private String tempThreadMsg;
	@Property
	private boolean notifyMember;
	
	
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Request request;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			thId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			fId = ec.get(Long.class, 0);
			thId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(fId==null)
			return new Object[] {thId};
		else return new Object[] {fId, thId};
	}
	
	public boolean isCreateMode(){
		if(thId==0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){ 
			forum = forumDAO.getForumById(fId);
			if(forum==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ForumID", fId));
			curProj = forum.getProject();
			
			if(!canCreateThread(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
		}
		else{ 
			thread = forumDAO.getThreadById(thId);
			if(thread==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadID", thId));
			forum = thread.getForum();
			curProj = forum.getProject();
			
			if(!canEditThread(thread)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
		}
	}
	
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){ 
			forum = forumDAO.getForumById(fId);
			curProj = forum.getProject();
			
			if(thread==null)
				thread = new Thread();
			
		}
		else{ 
			thread = forumDAO.getThreadById(thId);
			forum = thread.getForum();
			curProj = forum.getProject();
			
			tempThreadMsg = thread.getMessage(); //save the value for check later
		}
	}
	
	
	
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(isCreateMode() ){ 
			thread.setForum(forum);
			thread.setAuthor(getCurUser());
			thread.setCreateDate(new Date());
			forum.addThread(thread);
			ThreadUser tu = new ThreadUser();
			tu.setThread(thread);
			tu.setUser(getCurUser());
			thread.addThreadUsers(tu);
		}
		else{ //Edit
			
			if(!thread.getMessage().equals(tempThreadMsg)){
				for(ThreadUser tu : thread.getThreadUsers()){
					tu.setHasRead(false);
				}
			}
		}
		thread.setModifyDate(new Date());
		thread.setMessage(Util.filterOutRestrictedHtmlTags(thread.getMessage()));
		
		//handle upload attachment
		if(file1!=null)
			saveFile(file1, null);
		if(file2!=null)
			saveFile(file2, null);
		if(file3!=null)
			saveFile(file3, null);
		if(file4!=null)
			saveFile(file4, null);
		if(file5!=null)
			saveFile(file5, null);
		
		forumDAO.saveForum(forum);
	
		if(notifyMember){
			EmailTemplateVariables var = new EmailTemplateVariables(
					thread.getCreateDate().toString(), thread.getModifyDate().toString(),
					thread.getName(), 
					Util.truncateString(Util.stripTags(thread.getMessage()), Config.getInt("max_content_lenght_in_email")), 
					thread.getAuthorDisplayName(),
					curProj.getDisplayName(), 
					emailManager.createLinkBackURL(request, ThreadView.class, thread.getId()),
					thread.getType().toString(), thread.getForum().getName());
			
			try{
				if(isCreateMode()){
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_THREAD_ADDED, var);
				}else {
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_THREAD_EDITED, var);
				}
				appState.recordInfoMsg(messages.get("notification-sent-to-members"));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}	
		
		if(isCreateMode())
			return linkSource.createPageRenderLinkWithContext(ForumView.class, forum.getId());
		else
			return linkSource.createPageRenderLinkWithContext(ThreadView.class, thread.getId());
	}
	
	private void saveFile(UploadedFile upFile, String fileAlias){
		ForumAttachedFile attFile = new ForumAttachedFile();
		attFile.setId(Util.generateUUID());
		attFile.setFileName(upFile.getFileName());
		attFile.setAliasName(fileAlias);
		attFile.setContentType(upFile.getContentType());
		attFile.setSize(upFile.getSize());
		attFile.setCreator(getCurUser());
		attFile.setPath(attFileManager.saveAttachedFile(upFile, attFile.getId(), getModuleName(), curProj.getId()));
		
		thread.addAttachFile(attFile);
	}
	
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveFile(String attachId){
		ForumAttachedFile attFile = forumDAO.getAttachedFileById(attachId);
		if(attFile==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", attachId));
		thread = attFile.getThread();
		
		if(isCreateMode()){
			if(!canCreateThread(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		else{
			if(!canEditThread(thread)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		
		//remove actual file
		attFileManager.removeAttachedFile(attFile);
		
		thread.removeAttachFile(attFile);
		forumDAO.saveThread(thread);
		if(request.isXHR())
			return attachedFilesZone.getBody();
		return null;
	}
	
	public String getTitle(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("thread");
		return messages.get("edit")+" "+messages.get("thread");
			
	}
	public String getBreadcrumbLink(){
		if(isCreateMode())
			return messages.get("add-new")+" "+messages.get("thread")+"=modules/forum/threadedit?"+forum.getId()+":0"; 
		return encode(truncateStr(thread.getName()))+"=modules/forum/threadview?"+thread.getId()
				+ ","+ messages.get("edit") +"=modules/forum/threadedit?"+ thread.getId();
		
	}
	public BeanModel<Thread> getModel(){
		BeanModel<Thread> model = beanModelSource.createEditModel(Thread.class, messages);
        model.include("name","type","message");
        model.add("attachment", null);
        
        if(forum.isAnonymousPost()){
        	model.add("anonymous", null);
        }
        ProjUser pu = curProj.getMember(getCurUser());
        if( pu!=null && pu.hasPrivilege(PrivilegeForum.EDIT_THREAD)){
        	model.add("pinned", null);
        }
        model.add("notifyMember", null);
        
        return model;
	}
	
	public ThreadType getThType0() { return ThreadType.DISCUSS_TOPIC; }
	public ThreadType getThType1() { return ThreadType.ASK_A_QUESTION; }
	public ThreadType getThType2() { return ThreadType.SUGGEST_IMPROVMENTS; }
	public ThreadType getThType3() { return ThreadType.DEVELOPING_SOLUTIONS; }
	public ThreadType getThType4() { return ThreadType.IDENTITFY_PROBLEMS; }
	public ThreadType getThType5() { return ThreadType.ADD_OPINION_OR_FEEDBACK; }
	
	
}
