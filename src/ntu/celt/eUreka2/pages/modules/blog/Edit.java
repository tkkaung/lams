package ntu.celt.eUreka2.pages.modules.blog;

import java.util.Date;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;

public class Edit extends AbstractPageBlog {
	@Property
	private Project project;
	@Property
	private Blog blog;
	@Property
	private String pid;
	@Property
	private String bid;
	
	@Property
	private String subject;
	@Property
	private String content;
	@Property
	private String tags;
	@SuppressWarnings("unused")
	@Property
	private String tempTag;
	@Property
	private boolean notifyMember;
	
	@SuppressWarnings("unused")
	@Property
	private BlogFile tempFile;
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
		
	
	@SessionState
	private AppState appState;
	@Inject
	private BlogDAO bDAO;
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private Request request;
	@Inject
	private RequestGlobals requestGlobals;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Logger logger;
	@Inject
	private EmailManager emailManager;
	
	
	void onActivate(EventContext ec) {
		if (ec.getCount() == 1) {
			bid = ec.get(String.class, 0);
		}
		else if (ec.getCount() == 2) {
			pid = ec.get(String.class, 0);
			bid = ec.get(String.class, 1);
		}
	}
	Object[] onPassivate() {
		if (pid == null) {
		   return new Object[] {bid};
		}
		else {
			return new Object[] {pid, bid};
		}
	}
	
	public boolean isCreateMode(){
		if(bid==null || "".equals(bid))
			return true;
		return false;
	}
	
	 public String getTitle(){
    	if(isCreateMode())
    		return messages.get("add-new")+" "+messages.get("blog");
    	return messages.get("edit")+" "+messages.get("blog");
    }
	
	
	void setupRender() {
		if(isCreateMode()){
			project = pDAO.getProjectById(pid);
			if(project==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
			
			if(!canCreateBlog(project)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			
			blog = new Blog();
			subject = messages.get("untitled");
			content = messages.get("edit-your-content-here");
			blog.setShared(true);
		}
		else{
			blog = bDAO.getBlogById(bid);
			if(blog==null)
				throw new RecordNotFoundException(messages.format("entity-not-exists", "BlogId", bid));
			project = blog.getProject();
			if(!canEditBlog(blog)){
				throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
			}
			if(subject==null)
				subject = blog.getSubject();
			if(content==null)
				content = blog.getContent();
			
		}
		
	}
	void onPrepareForSubmitFromBlogForm(){
		if(isCreateMode()){
			project = pDAO.getProjectById(pid);
			blog = new Blog();
		}
		else{
			blog = bDAO.getBlogById(bid);
			project = blog.getProject();
		}
	}
		
	
	void onValidateFormFromBlogForm(){

	}
	
	@CommitAfter
	Object onSuccessFromBlogForm() {
		if(isCreateMode()){
			blog.setCdate(new Date());
			blog.setAuthor(getCurUser());
			blog.setProject(project);
		}
		blog.setMdate(new Date());
		blog.setIp(requestGlobals.getHTTPServletRequest().getRemoteAddr());
		boolean shared = Boolean.parseBoolean(request.getParameter("shared"));
		blog.setShared(shared);
		blog.setSubject(Util.filterOutRestrictedHtmlTags(Util.nvl(subject)));
		blog.setContent(Util.filterOutRestrictedHtmlTags(Util.nvl(content)));
		logger.info("content="+content);
		
		if (request.getParameter("mode").equals("auto")) {
			blog.setStatus(BlogStatus.DRAFT);
		}
		else {
			blog.setStatus(BlogStatus.PUBLISHED);
		}
		
		if(tags!=null){
			String tagsStr[] = tags.split(",");
			for(String tag : tagsStr){
				tag = tag.trim();
				if(!tag.isEmpty())
					blog.addTags(tag);
			}
			
		}
		
		
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
		
		String emailTemplateType;
		if(isCreateMode()){
			bDAO.addBlog(blog);  //commit to database at this point, ID is initialized
			emailTemplateType = EmailTemplateConstants.BLOG_ADDED;
		}
		else{
			bDAO.updateBlog(blog);
			emailTemplateType = EmailTemplateConstants.BLOG_EDITED;
		}
		
		if (("auto").equalsIgnoreCase(request.getParameter("mode"))) {
			appState.recordInfoMsg(messages.format("auto-save-on-x", Util.formatDateTime2(new Date())));
			return linkSource.createPageRenderLinkWithContext(this.getClass(), blog.getId()); 
		}
		else{
			if(notifyMember){
				EmailTemplateVariables var = new EmailTemplateVariables(
					blog.getCdate().toString(), blog.getMdate().toString(),
					blog.getSubject(), 
					Util.truncateString(Util.stripTags(blog.getContent()), Config.getInt("max_content_lenght_in_email")), 
					blog.getAuthor().getDisplayName(),
					project.getDisplayName(), 
					emailManager.createLinkBackURL(request, View.class, blog.getId()),
					null, null);
				
				try{
					emailManager.sendHTMLMail(project.getUsers(), emailTemplateType, var);
					appState.recordInfoMsg(messages.get("notification-sent-to-members"));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
			}	
		}
		return linkSource.createPageRenderLinkWithContext(View.class, blog.getId()); 
		
	}
	
	
	
	
	private void saveFile(UploadedFile upFile, String aliasFileName){
		BlogFile f = new BlogFile();
		f.setId(Util.generateUUID());
		f.setFileName(upFile.getFileName());
		f.setAliasName(aliasFileName);
		f.setContentType(upFile.getContentType());
		f.setSize(upFile.getSize());
		f.setCreator(getCurUser());
		f.setPath(attFileManager.saveAttachedFile(upFile, f.getId(), getModuleName(), project.getId()));
		
		blog.addFile(f);
	}
	@InjectComponent
	private Zone attachedFilesZone;
	@CommitAfter
	Object onActionFromRemoveFile(String fId){
		BlogFile f = bDAO.getBlogFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "FileID", fId));
		blog = f.getBlog();
		if(!canEditBlog(blog))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		attFileManager.removeAttachedFile(f);
		blog.removeFile(f);
		bDAO.updateBlog(blog);
		if(request.isXHR())
			return attachedFilesZone.getBody();
		return null;
	}
	
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private boolean showTags;
	@InjectComponent
	private Zone tagsZone;
	Object onToggleUsedTags(String projId){
		project = pDAO.getProjectById(projId);
		showTags = !showTags;
		return tagsZone.getBody();
	}
	@InjectComponent
	private Zone selectedTagsZone;
	@CommitAfter
	Object onRemoveTag(String blogId, String tagName){
		Blog b = bDAO.getBlogById(blogId);
		if(b==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BlogID", blogId));
		if(!canEditBlog(b))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		b.removeTags(tagName);
		bDAO.updateBlog(b);
		blog = b;
		return selectedTagsZone.getBody();
	}
	
	
}
