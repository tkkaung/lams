package ntu.celt.eUreka2.pages.modules.elog;

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
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogComment;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
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

public class View extends AbstractPageElog  {
	private String eid;
	@Property
	private Elog elog;
	@Property
	private Project project;
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	@SuppressWarnings("unused")
	@Property
	private LLogFile lLogFile;
	@SuppressWarnings("unused")
	@Property
	private ElogComment cmt;
	@Property
	private List<ElogComment> cmts;
	@SuppressWarnings("unused")
	@Property
	private List<ElogComment> cmtsDisplay;
	@Property
	private ElogComment newCmt;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@Property
	private boolean notifyMember = true;
	@SuppressWarnings("unused")
	@Property
	private LogEntry reflt; //reflection (from learningLog)
	
	
	@SessionState
	private AppState appState ;
	
	@Inject
	private ElogDAO eDAO;
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
	
	void onActivate(String id) {
		eid = id;
		
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		
		project = elog.getProject();
	}
	String onPassivate() {
		return eid;
	}
	
	public String getBreadcrumb(){
		if(elog.getAuthor().equals(getCurUser()))
			return messages.get("manage")+"=modules/elog/manage?"+project.getId()
				+"," + messages.get("view")+" " + messages.get("elog")+"=modules/elog/view?"+elog.getId();
		return messages.get("view")+" " +messages.get("elog")+"=modules/elog/view?"+elog.getId();
	}
	
	
	public void setupRender() {
		if(!canViewElog(elog)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if(newCmt==null)
			newCmt = new ElogComment();
		
		cmts = elog.getComments();
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*getRowsPerPage();
		int toIndex = Math.min(curPageNo*getRowsPerPage(), cmts.size());
		cmtsDisplay = cmts.subList(fromIndex, toIndex);
		
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
													// e.g. youtube iframe was added
	}
	
	public boolean canViewElog(Elog e){
		if(e.getProject().isReference() && e.getStatus().equals(ElogStatus.APPROVED) && e.isPublished())
			return true;
		if(e.getAuthor().equals(getCurUser()))
			return true;
		if(canAdminAccess(e.getProject()))
			return true;
		ProjUser pu = e.getProject().getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.VIEW_APPROVAL_LIST))
			return true;
		//if(e.getStatus().equals(ElogStatus.APPROVED) && pu.hasPrivilege(PrivilegeElog.VIEW_ELOG))
		//	return true;
		if(e.isPublished() && pu.hasPrivilege(PrivilegeElog.VIEW_ELOG))
			return true;
		
		return false;
	}
	
	
	
	
	public boolean isApproved(Elog elog){
		if(elog.getStatus().equals(ElogStatus.APPROVED))
			return true;
		return false;
	}
	
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public int getTotalSize() {
		if(cmts==null) return 0;
		return cmts.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	@CommitAfter
	public Object onActionFromDeleteElog(String eid){
		elog = eDAO.getElogById(eid);
		if(elog==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ElogId", eid));
		project = elog.getProject();
		
		if(!canDeleteElog(elog)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", elog.getSubject() ));
		}
		if(elog.getComments().size()!=0){
			appState.recordErrorMsg(messages.get("cant-delete-elog-with-comment"));
		}
		else{
			try{
				for(ElogFile ef : elog.getFiles()){
					attFileManager.removeAttachedFile(ef);;
				}
				
				eDAO.deleteElog(elog);
				if(getCurUser().equals(elog.getAuthor()))
					return linkSource.createPageRenderLinkWithContext(Manage.class, project.getId());
				else
					return linkSource.createPageRenderLinkWithContext(Home.class, project.getId());
			}
			catch(ConstraintViolationException e){
				appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", elog.getSubject()));
			}
		}
		return null; //stay at same page
	}
	
	@CommitAfter
	public void onActionFromDeleteComment(int cid){
		ElogComment cmt = eDAO.getElogCommentById(cid);
		if(cmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "CommentId", cid));
		elog = cmt.getElog();
		project = elog.getProject();
		
		if(!canDeleteComment(cmt)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", cmt.getSubject() ));
		}
		elog.removeComment(cmt);
		eDAO.updateElog(elog);
	}
	
	public void onPrepareForSubmitFromCmtForm() {
		if(newCmt==null)
			newCmt = new ElogComment();
		
		if(!canAddComment(elog)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
	}
	@CommitAfter
	Object onSuccessFromCmtForm() {
		newCmt.setAuthor(getCurUser());
		newCmt.setElog(elog);
		newCmt.setCdate(new Date());
		newCmt.setMdate(new Date());
		newCmt.setDisabled(false);
		newCmt.setIp(requestGlobals.getHTTPServletRequest().getRemoteAddr());
		newCmt.setSubject("");
		newCmt.setContent(Util.filterOutRestrictedHtmlTags(newCmt.getContent()));
		elog.addComment(newCmt);
		eDAO.updateElog(elog);
		
		if(notifyMember){
			Set<User> recipients = new HashSet<User>();
			recipients.add(elog.getAuthor());
			for(ElogComment bc : elog.getComments()){
				recipients.add(bc.getAuthor());
			}
			recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						newCmt.getCdate().toString(), newCmt.getMdate().toString(),
						newCmt.getSubject(),
						Util.truncateString(Util.stripTags(newCmt.getContent()), Config.getInt("max_content_lenght_in_email")), 
						newCmt.getAuthor().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, View.class, elog.getId()),
						null, null);
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_COMMENT_ADDED, var);
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
		return project.hasModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
	@Cached
	public ProjModule getLearningLogModule(){
		return project.getProjModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
	
	@Inject
	private LearningLogDAO llogDAO;
	public long countReflections(){
		return llogDAO.countElogReflections(eid, getCurUser());
	}
	@Cached
	public List<LogEntry> getReflections(){
		return llogDAO.getElogReflections(eid, getCurUser());
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
		ref.setElogId(eid);
		ref.setMdate(new Date());
		ref.setProject(project);
		ref.setShared(refShared);
		ref.setTitle("REF:"+elog.getSubject());
		ref.setType(LogEntry.TYPE_ELOG_REFLECTION);
		
		llogDAO.saveLogEntry(ref);
		
		if(refNotifyMember && ref.isShared()){
			Set<User> recipients = new HashSet<User>();
			recipients.add(elog.getAuthor());
			for(ElogComment bc : elog.getComments()){
				recipients.add(bc.getAuthor());
			}
			//recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						ref.getCdate().toString(), ref.getMdate().toString(),
						ref.getTitle(), 
						Util.truncateString(Util.stripTags(ref.getContent()), Config.getInt("max_content_lenght_in_email")), 
						ref.getCreator().getDisplayName(),
						project.getDisplayName(), 
						emailManager.createLinkBackURL(request, this.getClass(), eid),
						elog.getSubject(), ref.getType());
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.ELOG_REFLECTION_ADDED, var);
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
		llogEdit.setContext(project.getId(), rid, LogEntry.TYPE_ELOG_REFLECTION);
		return llogEdit;
	}
	public StreamResponse onRetrieveLLogFile(String id) {
		return llogEdit.onRetrieveFile(id);
	}
}
