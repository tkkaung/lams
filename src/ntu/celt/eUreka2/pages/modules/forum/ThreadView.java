package ntu.celt.eUreka2.pages.modules.forum;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
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
import org.apache.tapestry5.services.Response;

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
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.forum.RateType;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.ThreadReplyUser;
import ntu.celt.eUreka2.modules.forum.ThreadType;
import ntu.celt.eUreka2.modules.forum.ThreadUser;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.pages.modules.learninglog.LearningLogEdit;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateConstants;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;
import ntu.celt.eUreka2.services.email.SendEmailFailException;

public class ThreadView  extends AbstractPageForum {
	@SessionState
	private AppState appState;
	@Property
	private Project curProj;
	@Property
	private Forum forum;
	@Property
	private Thread thread;
	private Long thId;
	@SuppressWarnings("unused")
	@Property
	private ThreadReply threadR;
	@Property
	private List<ThreadReply> threadRs;
	@SuppressWarnings("unused")
	@Property
	private List<ThreadReply> threadRsDisplay;
	@SuppressWarnings("unused")
	@Property
	private LogEntry reflt; //reflection (from learningLog)
	
	
	@SuppressWarnings("unused")
	@Property
	private LLogFile lLogFile;
	@SuppressWarnings("unused")
	@Property
	private AttachedFile tempAttFile;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@Property
	private String qReplyContent;
	@Property
	private boolean notifyMember = true;
	
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Messages messages;
	@Inject
	private EmailManager emailManager;
	@Inject
	private Response response;
	
	void onActivate(Long id) {
		thId = id;
		
		thread = forumDAO.getThreadById(thId);
		if(thread==null) 
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadID", thId));
		forum = thread.getForum();
		curProj = forum.getProject();
	}
	Long onPassivate() {
		return thId;
	}
	
	void setupRender(){
		
		if(!canViewThread(curProj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		threadRs = thread.getRepliesOrdered();
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*getRowsPerPage();
		int toIndex = Math.min(curPageNo*getRowsPerPage(), threadRs.size());
		threadRsDisplay = threadRs.subList(fromIndex, toIndex);
		
		updateThreadUser(thread);
		
		response.setIntHeader("X-XSS-Protection", 0); //to let browser execute script after the script was added, 
													// e.g. youtube iframe was added
	}
	void onPrepareForSubmit(){
		
	}
	
	@CommitAfter
	private void updateThreadUser(Thread th){
		long countviewTimeGap = Long.parseLong(messages.get("countview-time-gap"));
		
		ThreadUser tu = th.getThreadUser(getCurUser());
		if(tu == null){
			tu = new ThreadUser();
			tu.setThread(th);
			tu.setUser(getCurUser());
			tu.setHasRead(true);
			tu.setNumView(1);
			tu.setModifyDate(new Date());
			th.addThreadUsers(tu);
		}
		else{
			tu.setHasRead(true);
			if(tu.getModifyDate()==null || (new Date()).getTime() - tu.getModifyDate().getTime() > countviewTimeGap){
				tu.setNumView(tu.getNumView() + 1);
			}
			tu.setModifyDate(new Date());
		}
		
		forumDAO.saveThreadUser(tu);
	}
	
	
	
	
	
	
	
	
	
	
	@CommitAfter
	void onActionFromFlag(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		tu.setFlagged(true);
		forumDAO.saveThreadUser(tu);
	}
	@CommitAfter
	void onActionFromUnFlag(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		tu.setFlagged(false);
		forumDAO.saveThreadUser(tu);
	}
	@CommitAfter
	Object onActionFromRemoveThrd(Long thId){
		Thread th = forumDAO.getThreadById(thId);
		if(th==null) 
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadID", thId));
		forum = th.getForum();
		if(!canDeleteThread(th)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", th.getName()));
		}
		
		for(AttachedFile attFile : th.getAttachedFiles()){
			attFileManager.removeAttachedFile(attFile);
		}
		forumDAO.deleteThread(th);
		//forum.removeThread(th);
		//forumDAO.saveForum(forum); //no need to do
		
		
		return linkSource.createPageRenderLinkWithContext(ForumView.class, forum.getId());
	}
	@CommitAfter
	Object onActionFromRemoveThrdR(Long thRId){
		ThreadReply thR = forumDAO.getThreadReplyById(thRId);
		if(thR==null) 
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ThreadReplyID", thRId));
		thread = thR.getThread();
		if(!canDeleteThreadReply(thR)){
			throw new NotAuthorizedAccessException(messages.format("not-allow-to-delete", thR.getName() )); //not allow to delete
		}
		
		for(AttachedFile attFile : thR.getAttachedFiles()){
			attFileManager.removeAttachedFile(attFile);
		}
		forumDAO.deleteThreadReply(thR);
		//thread.removeReplies(thR);  //cannot do this, already has database cascade to do it 
		//forumDAO.saveThread(thread); 
		
		return linkSource.createPageRenderLinkWithContext(ThreadView.class, thread.getId());
	}
	@CommitAfter
	void onUnRate(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		tu.setRate(null);
		forumDAO.saveThreadUser(tu);
	}
	@CommitAfter
	void onUnRateR(Long thRId){
		ThreadReplyUser tu = forumDAO.getThreadReplyUser(thRId, getCurUser().getId());
		tu.setRate(null);
		forumDAO.saveThreadReplyUser(tu);
	}
	@CommitAfter
	void onRatePositive(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		tu.setRate(RateType.GOOD);
		forumDAO.saveThreadUser(tu);
	}
	@CommitAfter
	void onRateNegative(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		tu.setRate(RateType.BAD);
		forumDAO.saveThreadUser(tu);
	}
	@CommitAfter
	void onRatePositiveR(Long thRId){
		ThreadReplyUser tu = forumDAO.getThreadReplyUser(thRId, getCurUser().getId());
		if(tu==null){
			tu = new ThreadReplyUser();
			tu.setThreadReply(forumDAO.getThreadReplyById(thRId));
			tu.setUser(getCurUser());
		}
		tu.setRate(RateType.GOOD);
		forumDAO.saveThreadReplyUser(tu);
	}
	@CommitAfter
	void onRateNegativeR(Long thRId){
		ThreadReplyUser tu = forumDAO.getThreadReplyUser(thRId, getCurUser().getId());
		if(tu==null){
			tu = new ThreadReplyUser();
			tu.setThreadReply(forumDAO.getThreadReplyById(thRId));
			tu.setUser(getCurUser());
		}
		tu.setRate(RateType.BAD);
		forumDAO.saveThreadReplyUser(tu);
	}
	public boolean hasRated(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		if(tu==null || tu.getRate()==null)
			return false;
		return true;
	}
	public boolean hasRatedR(Long thRId){
		ThreadReplyUser tu = forumDAO.getThreadReplyUser(thRId, getCurUser().getId());
		if(tu==null || tu.getRate()==null)
			return false;
		return true;
	}
	public boolean isRatedPos(Long thId){
		ThreadUser tu = forumDAO.getThreadUser(thId, getCurUser().getId());
		if(tu==null || tu.getRate()==null)
			return false;
		if(tu.getRate().equals(RateType.GOOD))
			return true;
		return false;
	}
	public boolean isRatedRPos(Long thRId){
		ThreadReplyUser tu = forumDAO.getThreadReplyUser(thRId, getCurUser().getId());
		if(tu==null || tu.getRate()==null)
			return false;
		if(tu.getRate().equals(RateType.GOOD))
			return true;
		return false;
	}
	
	
	
	@CommitAfter
	void onSuccessFromQReplyForm(){
		
		ThreadReply thR = new ThreadReply();
		thR.setAuthor(getCurUser());
		thR.setCreateDate(new Date());
		thR.setModifyDate(new Date());
		thR.setParent(null);
		thR.setThread(thread);
		thR.setMessage(Util.filterOutRestrictedHtmlTags(qReplyContent));
		thR.setName((thread.getName().startsWith("RE:")?"":"RE: ") + thread.getName());
		thR.setType(ThreadType.DISCUSS_TOPIC);
		thread.addReplies(thR);
		
		for(ThreadUser tu : thread.getThreadUsers()){
			tu.setHasRead(false);
		}
		
		forumDAO.saveThreadReply(thR);
		forumDAO.saveThread(thread);
		
		if(notifyMember){
			Set<User> recipients = new HashSet<User>();
			recipients.add(thread.getAuthor());
			for(ThreadReply thR2 : thread.getReplies()){
				recipients.add(thR2.getAuthor());
			}
			for(ThreadUser thU : thread.getThreadUsers()){
				if(RateType.GOOD.equals(thU.getRate())){
					recipients.add(thU.getUser());
				}
			}
			recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						thR.getCreateDate().toString(), thR.getModifyDate().toString(),
						thR.getName(), 
						Util.truncateString(Util.stripTags(thR.getMessage()), Config.getInt("max_content_lenght_in_email")), 
						thR.getAuthorDisplayName(),
						thread.getForum().getProject().getDisplayName(), 
						emailManager.createLinkBackURL(request, ThreadView.class, thread.getId()),
						thread.getName(), thR.getType().toString());
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.FORUM_THREAD_REPLIED, var);
					appState.recordInfoMsg(messages.format("notification-sent-to-x", Util.extractDisplayNames(recipients)));
				}
				catch(SendEmailFailException e){
					logger.warn(e.getMessage(), e);
					appState.recordWarningMsg(e.getMessage());
				}
			}
		}
	}
	
	
	
	public String getNegativeRateUsers(Thread th){
		String str = "";
		List<ThreadUser> thUList = forumDAO.getThUserByRateType(th, RateType.BAD);
		for(ThreadUser thU : thUList){
			str = thU.getUser().getDisplayName()+", ";
		}
		str = Util.removeLastSeparator(str, ", ");
		
		return str;
	}
	
	
	public long getTotalNegativeRate(Thread th){
		return forumDAO.getTotalRateByType(th, RateType.BAD);
	}
	public long getTotalPositiveRate(Thread th){
		return forumDAO.getTotalRateByType(th, RateType.GOOD);
	}
	public long getTotalNegativeRateR(ThreadReply thR){
		return forumDAO.getTotalRateByType(thR, RateType.BAD);
	}
	public long getTotalPositiveRateR(ThreadReply thR){
		return forumDAO.getTotalRateByType(thR, RateType.GOOD);
	}
	public boolean isFlagged(Thread th){
		ThreadUser tu = th.getThreadUser(getCurUser());
		if(tu!=null && tu.isFlagged())
			return true;
		return false;
	}
	
	public int getDepth(ThreadReply th){
		int count = 1;
		ThreadReply parent = th.getParent();
		while(parent != null){
			parent = parent.getParent();
			count++;
		}
		return count;
	}
	
	
	
	public int getTotalSize() {
		if(threadRs==null) return 0;
		return threadRs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int num){
		appState.setRowsPerPage(num);
	}
	
	
	
	
	
	
	@Cached
	public ProjModule getLearningLogModule(){
		return curProj.getProjModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
	
	
	@Inject
	private LearningLogDAO llogDAO;
	public long countReflections(){
		return llogDAO.countForumReflections(thread.getId(), getCurUser());
	}
	@Cached
	public List<LogEntry> getReflections(){
		return llogDAO.getForumReflections(thread.getId(), getCurUser());
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
		ref.setForumThreadId(thId);
		ref.setMdate(new Date());
		ref.setProject(curProj);
		ref.setShared(refShared);
		ref.setTitle("REF:"+thread.getName());
		ref.setType(LogEntry.TYPE_FORUM_REFLECTION);
		
		llogDAO.saveLogEntry(ref);
		
		if(refNotifyMember && ref.isShared()){
			Set<User> recipients = new HashSet<User>();
			recipients.add(thread.getAuthor());
			for(ThreadReply thR2 : thread.getReplies()){
				recipients.add(thR2.getAuthor());
			}
			for(ThreadUser thU : thread.getThreadUsers()){
				if(RateType.GOOD.equals(thU.getRate())){
					recipients.add(thU.getUser());
				}
			}
			//recipients.remove(getCurUser());
			
			if(!recipients.isEmpty()){
				EmailTemplateVariables var = new EmailTemplateVariables(
						ref.getCdate().toString(), ref.getMdate().toString(),
						ref.getTitle(), 
						Util.truncateString(Util.stripTags(ref.getContent()), Config.getInt("max_content_lenght_in_email")), 
						ref.getCreator().getDisplayName(),
						curProj.getDisplayName(), 
						emailManager.createLinkBackURL(request, ThreadView.class, thId),
						thread.getName(), ref.getType());
				try{
					emailManager.sendHTMLMail(recipients, EmailTemplateConstants.FORUM_THREAD_REFLECTION, var);
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
		llogEdit.setContext(curProj.getId(), rid, LogEntry.TYPE_FORUM_REFLECTION);
		return llogEdit;
	}
	public StreamResponse onRetrieveLLogFile(String id) {
		return llogEdit.onRetrieveFile(id);
	}
}
