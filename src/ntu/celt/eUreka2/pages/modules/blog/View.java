package ntu.celt.eUreka2.pages.modules.blog;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.pages.modules.learninglog.LearningLogEdit;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
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
import org.apache.tapestry5.services.Response;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;

public class View extends AbstractPageBlog  {
	private String bid;
	@Property
	private Blog blog;
	@Property
	private Project curProj;
	@SuppressWarnings("unused")
	@Property
	private BlogFile attFile;
	@SuppressWarnings("unused")
	@Property
	private LLogFile lLogFile;
	@SuppressWarnings("unused")
	@Property
	private BlogComment cmt;
	@Property
	private List<BlogComment> cmts;
	@SuppressWarnings("unused")
	@Property
	private List<BlogComment> cmtsDisplay;
	@Property
	private String newCmtContent;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@Property
	private boolean notifyMember = true;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private BlogDAO bDAO;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Logger logger;
	@Inject
	private Request request;
	@Inject
	private RequestGlobals requestGlobals;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Response response;
	
	@SuppressWarnings("unused")
	@Property
	private String tempTag;
	@SuppressWarnings("unused")
	@Property
	private int tempIndex;
	@SuppressWarnings("unused")
	@Property
	private LogEntry reflt; //reflection (from learningLog)
	
	
	void onActivate(String id) {
		bid = id;
		
		blog = bDAO.getBlogById(bid);
		if(blog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "BlogId", bid));
		curProj = blog.getProject();
		
	}
	String onPassivate() {
		return bid;
	}
	
	public void setupRender() {
		if(!canViewBlog(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		cmts = blog.getComments();
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*getRowsPerPage();
		int toIndex = Math.min(curPageNo*getRowsPerPage(), cmts.size());
		cmtsDisplay = cmts.subList(fromIndex, toIndex);
		

		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
													// e.g. youtube iframe was added
	}
	
	public int getTotalSize() {
		if(cmts==null) return 0;
		return cmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	@CommitAfter
	public Object onActionFromDeleteBlog(String bid){
		if(!canDeleteBlog(blog)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", blog.getSubject() ));
		}
		if(blog.getComments().size()!=0){
			appState.recordErrorMsg(messages.get("cant-delete-blog-with-comment"));
		}
		else{
			try{
				for(BlogFile bf : blog.getAttaches()){
					attFileManager.removeAttachedFile(bf);
				}
				bDAO.deleteBlog(blog);
				return linkSource.createPageRenderLinkWithContext(Home.class, curProj.getId());
			}
			catch(ConstraintViolationException e){
				appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", blog.getSubject()));
			}
		}
		return null; //stay at same page
	}
	
	
	@CommitAfter
	public void onActionFromDeleteComment(int cid){
		BlogComment cmt = bDAO.getBlogCommentById(cid);
		if(cmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "CommentId", cid));
		blog = cmt.getBlog();
		curProj = blog.getProject();
		
		if(!canDeleteComment(cmt)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", cmt.getSubject() ));
		}
		blog.removeComment(cmt);
		bDAO.updateBlog(blog);
	}
	
	public void onPrepareForSubmit() {
		
	}
	@CommitAfter
	Object onSuccessFromCmtForm() {
		if(!canAddComment(blog)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-add-x", messages.get("comment")));
		}
		
		BlogComment newCmt = new BlogComment();
		newCmt.setAuthor(getCurUser());
		newCmt.setBlog(blog);
		newCmt.setCdate(new Date());
		newCmt.setMdate(new Date());
		newCmt.setDisabled(false);
		newCmt.setIp(requestGlobals.getHTTPServletRequest().getRemoteAddr());
		newCmt.setSubject("");
		newCmt.setContent(Util.filterOutRestrictedHtmlTags(newCmtContent));
		blog.addComment(newCmt);
		bDAO.updateBlog(blog);
		
		if(notifyMember){
			Set<User> recipients = new HashSet<User>();
			recipients.add(blog.getAuthor());
			for(BlogComment bc : blog.getComments()){
				recipients.add(bc.getAuthor());
			}
			recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						newCmt.getCdate().toString(), newCmt.getMdate().toString(),
						newCmt.getSubject(), 
						Util.truncateString(Util.stripTags(newCmt.getContent()), Config.getInt("max_content_lenght_in_email")), 
						newCmt.getAuthor().getDisplayName(),
						curProj.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, blog.getId()),
						null, null);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.BLOG_COMMENT_ADDED, var);
					appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
			}
		}	
		
		return null; //return to the same page
	}
	
	
	
	
	public boolean canDeleteRef(LogEntry reflection){
		if(reflection.getCreator().equals(getCurUser()))
			return true;
		return false;
	}
	public boolean canEditRef(LogEntry reflection){
		if(reflection.getCreator().equals(getCurUser())
			&& hasLearningLogMod()
		)
			return true;
		return false;
	}
	@Cached
	public boolean hasLearningLogMod(){
		return curProj.hasModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
	@Cached
	public ProjModule getLearningLogModule(){
		return curProj.getProjModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
	
	
	@Inject
	private LearningLogDAO llogDAO;
	public long countReflections(){
		return llogDAO.countBlogReflections(bid, getCurUser());
	}
	@Cached
	public List<LogEntry> getReflections(){
		return llogDAO.getBlogReflections(bid, getCurUser());
	}
	
	@Property
	private String refContent;
	@Property
	private boolean refShared = true;
	@Property
	private boolean refNotifyMember = true; //default value
	
	@Property
	@Persist
	private boolean showRef;
	@InjectComponent
	private Zone refltsZone;
	@InjectComponent
	private Zone showRefLinkZone;
	Object onActionFromShowhideRef(){
		showRef = !showRef;
		
		return new MultiZoneUpdate("showRefLinkZone", showRefLinkZone.getBody()).add("refltsZone",refltsZone.getBody());
	}
	
	@CommitAfter
	void onSuccessFromWriteRefForm(){
		LogEntry ref = new LogEntry();
		ref.setCdate(new Date());
		ref.setContent(Util.textarea2html(refContent));
		ref.setCreator(getCurUser());
		ref.setBlogId(bid);
		ref.setMdate(new Date());
		ref.setProject(curProj);
		ref.setShared(refShared);
		ref.setTitle("REF:"+blog.getSubject());
		ref.setType(LogEntry.TYPE_BLOG_REFLECTION);
		
		llogDAO.saveLogEntry(ref);
		
		if(refNotifyMember && ref.isShared()){
			Set<User> recipients = new HashSet<User>();
			recipients.add(blog.getAuthor());
			for(BlogComment bc : blog.getComments()){
				recipients.add(bc.getAuthor());
			}
			//recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						ref.getCdate().toString(), ref.getMdate().toString(),
						ref.getTitle(), 
						Util.truncateString(Util.stripTags(ref.getContent()), Config.getInt("max_content_lenght_in_email")), 
						ref.getCreator().getDisplayName(),
						curProj.getDisplayName(), 
						emailManager.createLinkBackURL(request, this.getClass(), bid),
						blog.getSubject(), ref.getType());
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.BLOG_REFLECTION_ADDED, var);
					appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
			}
		}
	}
	
	@CommitAfter
	public void onActionFromDeleteRef(long rid){
		LogEntry ref = llogDAO.getLogEntryById(rid);
		if(ref==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "RefId", rid));

		if(!canDeleteRef(ref)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", ref.getTitle() ));
		}
		llogDAO.deleteLogEntry(ref);
	}
	@InjectPage
	private LearningLogEdit llogEdit;
	Object onActionFromEditRef(long rid){
		llogEdit.setContext(curProj.getId(), rid, LogEntry.TYPE_BLOG_REFLECTION);
		return llogEdit;
	}
	public StreamResponse onRetrieveLLogFile(String id) {
		return llogEdit.onRetrieveFile(id);
	}
}
