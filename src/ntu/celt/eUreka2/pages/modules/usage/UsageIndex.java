package ntu.celt.eUreka2.pages.modules.usage;

import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.RateType;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.usage.UsageType;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class UsageIndex extends AbstractPageUsage{
	@Property
	private Project proj;
	@Property
	private String projId;
	@Property
	private List<User> users;
	@SuppressWarnings("unused")
	@Property
	private List<User> usersDisplay;
	@Property
	private User user;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@Property
	private int rowIndex;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ResourceDAO resourceDAO;
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private LearningLogDAO llogDAO;
	@Inject
	private Messages messages;
	
	void onActivate(String id) {
		projId = id;
		
		proj = projDAO.getProjectById(projId);
		if(proj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
	}
	String onPassivate() {
		return projId;
	}
	public int getRowNum(){
		return (curPageNo-1)*getRowsPerPage()+ rowIndex + 1;
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public int getTotalSize(){
		return users.size();
	}
	
	
	void setupRender() {
		if(!canViewMemberContributions(proj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		users = proj.getUsers();
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), users.size());
		usersDisplay = users.subList(fromIndex, toIndex);
	}
	
	
	private Module getModule(String name, Project proj){
		for(ProjModule pm : proj.getProjmodules()){
			if(pm.getModule().getName().equalsIgnoreCase(name)){
				return pm.getModule();
			}
		}
		return null;
	}
	@Cached
	public Module getAnnmtModule(){
		return getModule(PredefinedNames.MODULE_ANNOUNCEMENT, proj);
	}
	@Cached
	public Module getResourceModule(){
		return getModule(PredefinedNames.MODULE_RESOURCE, proj);
	}
	@Cached
	public Module getSchedulingModule(){
		return getModule(PredefinedNames.MODULE_SCHEDULING, proj);
	}
	@Cached
	public Module getForumModule(){
		return getModule(PredefinedNames.MODULE_FORUM, proj);
	}
	@Cached
	public Module getBlogModule(){
		return getModule(PredefinedNames.MODULE_BLOG, proj);
	}
	@Cached
	public Module getElogModule(){
		return getModule(PredefinedNames.MODULE_ELOG, proj);
	}
	
	
	public long getNumAnnouncement(){
		return annmtDAO.countAnnouncements(proj, user);
	}
	public long getNumThread(){
		return forumDAO.getTotalThreads(proj, user);
	}
	public long getNumThreadLike(){
		return forumDAO.getTotalThreadsRate(proj, user, RateType.GOOD);
	}
	public long getNumThreadYourLike(){
		return forumDAO.getTotalThreadsYouRate(proj, user, RateType.GOOD, getCurUser());
	}
	public long getNumThreadDislike(){
		return forumDAO.getTotalThreadsRate(proj, user, RateType.BAD);
	}
	public long getNumThreadYourDislike(){
		return forumDAO.getTotalThreadsYouRate(proj, user, RateType.BAD, getCurUser());
	}
	public long getNumThreadReply(){
		return forumDAO.getTotalThreadReplies(proj, user);
	}
	public long getNumThreadReplyLike(){
		return forumDAO.getTotalThreadRepliesRate(proj, user, RateType.GOOD);
	}
	public long getNumThreadReplyYourLike(){
		return forumDAO.getTotalThreadRepliesYouRate(proj, user, RateType.GOOD, getCurUser());
	}
	public long getNumThreadReplyDislike(){
		return forumDAO.getTotalThreadRepliesRate(proj, user, RateType.BAD);
	}
	public long getNumThreadReplyYourDislike(){
		return forumDAO.getTotalThreadRepliesYouRate(proj, user, RateType.BAD, getCurUser());
	}
	public long getNumThreadReflection(){
		return llogDAO.countForumReflections(proj, user, getCurUser());
	}
	public long getNumBlog(){
		return blogDAO.countBlogs(proj, user, BlogStatus.PUBLISHED, getCurUser());
	}
	public long getNumBlogComment(){
		return blogDAO.countBlogComments(proj, user);
	}
	public long getNumBlogReflection(){
		return llogDAO.countBlogReflections(proj, user, getCurUser());
	}
	public long getNumFolder(){
		return resourceDAO.countFolders(proj, user);
	}
	public long getNumFile(){
		return resourceDAO.countFiles(proj, user);
	}
	public long getNumFileVersion(){
		return resourceDAO.countFileVersions(proj, user);
	}
	public long getNumLink(){
		return resourceDAO.countLinks(proj, user);
	}
	public long getNumMilestone(){
		return scheduleDAO.countMilestones(proj, user);
	}
	public long getNumPhase(){
		return scheduleDAO.countPhases(proj, user);
	}
	public long getNumTask(){
		return scheduleDAO.countTasks(proj, user);
	}
	public long getNumTaskAssigned(){
		return scheduleDAO.countTasksAssigned(proj, user);
	}
	public long getNumElog(){
		return elogDAO.countElogs(proj, user, getCurUser());
	}
	public long getNumElogComment(){
		return elogDAO.countElogComments(proj, user);
	}
	public long getNumElogReflection(){
		return llogDAO.countElogReflections(proj, user, getCurUser());
	}
	
	
	public Object[] getCxt(UsageType utype){
		return new Object[]{projId, user.getId(), utype};
	}
	public Object[] getCxtAnnmt(){
		return getCxt(UsageType.ANNOUNCEMENT);
	}
	public Object[] getCxtThread(){
		return getCxt(UsageType.FORUM_THREAD);
	}
	public Object[] getCxtThreadLike(){
		return getCxt(UsageType.FORUM_THREAD_LIKE);
	}
	public Object[] getCxtThreadYourLike(){
		return getCxt(UsageType.FORUM_THREAD_YOU_LIKE);
	}
	public Object[] getCxtThreadDislike(){
		return getCxt(UsageType.FORUM_THREAD_DISLIKE);
	}
	public Object[] getCxtThreadYourDislike(){
		return getCxt(UsageType.FORUM_THREAD_YOU_DISLIKE);
	}
	public Object[] getCxtThreadReply(){
		return getCxt(UsageType.FORUM_THREAD_REPLY);
	}
	public Object[] getCxtThreadReplyLike(){
		return getCxt(UsageType.FORUM_THREAD_REPLY_LIKE);
	}
	public Object[] getCxtThreadReplyYourLike(){
		return getCxt(UsageType.FORUM_THREAD_REPLY_YOU_LIKE);
	}
	public Object[] getCxtThreadReplyDislike(){
		return getCxt(UsageType.FORUM_THREAD_REPLY_DISLIKE);
	}
	public Object[] getCxtThreadReplyYourDislike(){
		return getCxt(UsageType.FORUM_THREAD_REPLY_YOU_DISLIKE);
	}
	public Object[] getCxtThreadReflection(){
		return getCxt(UsageType.FORUM_REFLECTION);
	}
	public Object[] getCxtBlog(){
		return getCxt(UsageType.BLOG);
	}
	public Object[] getCxtBlogComment(){
		return getCxt(UsageType.BLOG_COMMENT);
	}
	public Object[] getCxtBlogReflection(){
		return getCxt(UsageType.BLOG_REFLECTION);
	}
	public Object[] getCxtFolder(){
		return getCxt(UsageType.RESOURCE_FOLDER);
	}
	public Object[] getCxtFile(){
		return getCxt(UsageType.RESOURCE_FILE);
	}
	public Object[] getCxtFileVersion(){
		return getCxt(UsageType.RESOURCE_FILE_VERSION);
	}
	public Object[] getCxtLink(){
		return getCxt(UsageType.RESOURCE_LINK);
	}
	public Object[] getCxtMilestone(){
		return getCxt(UsageType.SCHEDULING_MILESTONE);
	}
	public Object[] getCxtPhase(){
		return getCxt(UsageType.SCHEDULING_PHASE);
	}
	public Object[] getCxtTask(){
		return getCxt(UsageType.SCHEDULING_TASK);
	}
	public Object[] getCxtTaskAssigned(){
		return getCxt(UsageType.SCHEDULING_ASSIGNED_TASK);
	}
	public Object[] getCxtElog(){
		return getCxt(UsageType.ELOG);
	}
	public Object[] getCxtElogComment(){
		return getCxt(UsageType.ELOG_COMMENT);
	}
	public Object[] getCxtElogReflection(){
		return getCxt(UsageType.ELOG_REFLECTION);
	}
}
