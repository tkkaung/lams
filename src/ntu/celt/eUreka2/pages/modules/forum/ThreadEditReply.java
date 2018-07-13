package ntu.celt.eUreka2.pages.modules.forum;

import java.util.Date;

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
import org.slf4j.Logger;

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
import ntu.celt.eUreka2.modules.forum.ThreadType;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadUser;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

public class ThreadEditReply extends AbstractPageForum {
	@Property
	private Project curProj;
	@SessionState
	private AppState appState;
	@Property
	private Forum forum;
	@Property
	private ThreadReply thR;
	@Property
	private Thread th;
	private ThreadReply pthR;
	private Long thRId;
	private Long thId;
	private Long pthRId;
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
	@Property
	private boolean notifyMember;
	private String tempThreadMsg;
	
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
			thRId = ec.get(Long.class, 0);
		}
		if(ec.getCount()==2){
			thId = ec.get(Long.class, 0);
			pthRId = ec.get(Long.class, 1);
		}
	}
	Object[] onPassivate() {
		if(thRId!=null)
			return new Object[] {thRId};
		return new Object[] {thId, pthRId};
	}
	public boolean isCreateMode(){
		if(thRId==null)
			return true;
		return false;
	}
	
	void setupRender(){
		if(isCreateMode()){
			th = forumDAO.getThreadById(thId);
			if(th==null){ 
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadID", thId));
			}
			forum = th.getForum();
			curProj = forum.getProject();
			
			if(!canCreateThreadReply(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
			thR = new ThreadReply();
			if(pthRId!=0){
				pthR = forumDAO.getThreadReplyById(pthRId);
				if(pthR==null){
					throw new RecordNotFoundException(messages.format("entity-not-exists", "pThreadReplyID", pthRId));
				}
				thR.setName((pthR.getName().startsWith("RE:")?"":"RE: ") + pthR.getName());
			}
			else{
				thR.setName((th.getName().startsWith("RE:")?"":"RE: ") + th.getName());
			}
		}
		else{
			thR = forumDAO.getThreadReplyById(thRId);
			if(thR==null){
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadReplyID", thRId));
			}
			th = thR.getThread();
			forum = th.getForum();
			curProj = forum.getProject();
			
			if(!canEditThreadReply(thR)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
		}
	}
	
	void onPrepareForSubmitFromForm(){
		if(isCreateMode()){
			th = forumDAO.getThreadById(thId);
			forum = th.getForum();
			curProj = forum.getProject();
			if(pthRId!=0)
				pthR = forumDAO.getThreadReplyById(pthRId);
		}
		else{
			thR = forumDAO.getThreadReplyById(thRId);
			th = thR.getThread();
			forum = th.getForum();
			curProj = forum.getProject();
			
			tempThreadMsg = thR.getMessage(); //save the value for check later
		}
	}

	
	
	
	@CommitAfter
	Object onSuccessFromForm(){
		if(isCreateMode()){
			thR.setAuthor(getCurUser());
			thR.setCreateDate(new Date());
			thR.setParent(pthR);
			thR.setThread(th);
			if(pthR!=null)
				pthR.addChildren(thR);
			th.addReplies(thR);
			
			for(ThreadUser tu : th.getThreadUsers()){
				tu.setHasRead(false);
			}
		}
		else{
			if(!thR.getMessage().equals(tempThreadMsg)){
				for(ThreadUser tu : th.getThreadUsers()){
					tu.setHasRead(false);
				}
			}
		}
		
		thR.setModifyDate(new Date());
		thR.setMessage(Util.filterOutRestrictedHtmlTags(thR.getMessage()));

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

		
		forumDAO.saveThreadReply(thR);
		if(isCreateMode()){
			if(pthR!=null)
				forumDAO.saveThreadReply(pthR);
			forumDAO.saveThread(th);
		}
		
		EmailTemplateVariables var = new EmailTemplateVariables(
				thR.getCreateDate().toString(), thR.getModifyDate().toString(),
				thR.getName(), 
				Util.truncateString(Util.stripTags(thR.getMessage()), Config.getInt("max_content_lenght_in_email")), 
				thR.getAuthorDisplayName(),
				curProj.getDisplayName(), 
				emailManager.createLinkBackURL(request, ThreadView.class, th.getId()),
				th.getName(), thR.getType().toString());
		
		if(notifyMember){
			try{
				if(isCreateMode()){
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_THREAD_REPLIED, var);
				}else{
					emailManager.sendHTMLMail(curProj.getUsers(), EmailTemplateConstants.FORUM_THREAD_REPLY_EDIT, var);
				}
				appState.recordInfoMsg(messages.get("notification-sent-to-members"));
			}
			catch(SendEmailFailException e){
				logger.warn(e.getMessage(), e);
				appState.recordWarningMsg(e.getMessage());
			}
		}
		
		return linkSource.createPageRenderLinkWithContext(ThreadView.class, th.getId());
	}
	
	private void saveFile(UploadedFile upFile, String fileAlias){
		ForumAttachedFile attFile = new ForumAttachedFile();
		attFile.setId(Util.generateUUID());
		attFile.setFileName(upFile.getFileName());
		attFile.setAliasName(fileAlias);
		attFile.setContentType(upFile.getContentType());
		attFile.setSize(upFile.getSize());
		attFile.setCreator(getCurUser());
		attFile.setPath(attFileManager.saveAttachedFile(upFile,  attFile.getId(),
			this.getModuleName(), curProj.getId()));
		
		thR.addAttachFile(attFile);
	}
	
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveFile(String attachId){
		ForumAttachedFile attFile = forumDAO.getAttachedFileById(attachId);
		if(attFile==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", attachId));
		thR = attFile.getThreadReply();
		
		if(isCreateMode()){
			if(!canCreateThreadReply(curProj)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		else{
			if(!canEditThreadReply(thR)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
		}
		
		//remove actual file
		attFileManager.removeAttachedFile(attFile);
		
		thR.removeAttachFile(attFile);
		forumDAO.saveThreadReply(thR);
		
		if(request.isXHR())
			return attachedFilesZone.getBody();
		return null;
	}
	
	
	public BeanModel<ThreadReply> getModel(){
		BeanModel<ThreadReply> model = beanModelSource.createEditModel(ThreadReply.class, messages);
        model.include("name","type","message");
        model.add("attachment", null);
        if(forum.isAnonymousPost()){
        	model.add("anonymous", null);
        }
        model.add("notifyMember", null);
        
        return model;
	}
	
	
	public String getTitle(){
		if(isCreateMode())
			return messages.get("reply")+" "+messages.get("thread");
		return messages.get("edit")+" "+messages.get("thread-reply");
			
	}
	public String getBreadcrumbLink(){
		if(isCreateMode())
			return messages.get("reply")+" "+messages.get("thread")+"=modules/forum/threadeditreply?"+thId+":"+pthRId; 
		return messages.get("edit") +" "+messages.get("thread-reply") +"=modules/forum/threadeditreply?"+ thRId;
		
	}
	
	
	
	
	public ThreadType getThType0() { return ThreadType.DISCUSS_TOPIC; }
	public ThreadType getThType1() { return ThreadType.ASK_A_QUESTION; }
	public ThreadType getThType2() { return ThreadType.SUGGEST_IMPROVMENTS; }
	public ThreadType getThType3() { return ThreadType.DEVELOPING_SOLUTIONS; }
	public ThreadType getThType4() { return ThreadType.IDENTITFY_PROBLEMS; }
	public ThreadType getThType5() { return ThreadType.ADD_OPINION_OR_FEEDBACK; }
		
}
