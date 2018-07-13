package ntu.celt.eUreka2.pages.modules.usage;


import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogComment;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.RateType;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;
import ntu.celt.eUreka2.modules.resources.ResourceLink;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.usage.UsageType;
import ntu.celt.eUreka2.pages.modules.resources.FileView;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;

public class UsageDetail extends AbstractPageUsage{
	@Property
	private Project proj;
	@Property
	private String projId;
	@Property
	private User user;
	@Property
	private int userId;
	@Property
	private UsageType type;
	
	@Property
	private List<Announcement> annmtList;
	@Property
	private Announcement annmt;
	@Property
	private List<Thread> thrdList;
	@Property
	private Thread thrd;
	@Property
	private List<ThreadReply> thrdRList;
	@Property
	private ThreadReply thrdR;
	@Property
	private List<LogEntry> reflctList;
	@Property
	private LogEntry reflct;
	@Property
	private List<Blog> blogList;
	@Property
	private Blog blog;
	@Property
	private List<BlogComment> blogCmtList;
	@Property
	private BlogComment blogCmt;
	@Property
	private List<Elog> elogList;
	@Property
	private Elog elog;
	@Property
	private List<ElogComment> elogCmtList;
	@Property
	private ElogComment elogCmt;
	@Property
	private List<ResourceFolder> rsrcFdList;
	@Property
	private ResourceFolder rsrcFd;
	@Property
	private List<ResourceLink> rsrcLkList;
	@Property
	private ResourceLink rsrcLk;
	@Property
	private List<ResourceFile> rsrcFileList;
	@Property
	private ResourceFile rsrcFile;
	@Property
	private List<ResourceFileVersion> rsrcFileVerList;
	@Property
	private ResourceFileVersion rsrcFileVer;
	@Property
	private List<Milestone> schdMstList;
	@Property
	private Milestone schdMst;
	@Property
	private List<Phase> schdPhsList;
	@Property
	private Phase schdPhs;
	@Property
	private List<Task> schdTskList;
	@Property
	private Task schdTsk;
	
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int totalSize;
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private UserDAO userDAO;
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
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
   
    
	void onActivate(EventContext ec) {
		projId = ec.get(String.class, 0);
		userId = ec.get(Integer.class, 1);
		type = ec.get(UsageType.class, 2);
		
		proj = projDAO.getProjectById(projId);
		if(proj==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", projId));
		user = userDAO.getUserById(userId);
		if(user==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserId", userId));
	}
	Object[] onPassivate() {
		return new Object[] {projId, userId, type};
	}
	void setupRender() {
		if(!canViewMemberContributions(proj)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		loadUsageDetails();
	}
	
	
	public int getRowNum(){
		return rowIndex+1;
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public String getTypeDisplay(){
		return getTypeDisplay(type);
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
	public boolean isPublished(Blog b){
		if(b.getStatus().equals(BlogStatus.PUBLISHED))
			return true;
		return false;
	}
	public boolean isApproved(Elog e){
		if(e.getStatus().equals(ElogStatus.APPROVED))
			return true;
		return false;
	}
	public String getForumThreadTitleById(long thId){
		Thread th = forumDAO.getThreadById(thId);
		if(th!=null)
			return th.getName();
		return "";
	}
	public String getBlogTitleById(String id){
		Blog b = blogDAO.getBlogById(id);
		if(b!=null)
			return b.getSubject();
		return "";
	}
	public String getElogTitleById(String id){
		Elog e = elogDAO.getElogById(id);
		if(e!=null)
			return e.getSubject();
		return "";
	}
	public long countResourcesByFolder(ResourceFolder folder){
		return resourceDAO.getCountResourcesByFolder(folder);
	}
	@InjectPage
	private FileView fileViewPage;
	public Asset getTypeIcon(Resource resrc){
		return fileViewPage.getTypeIcon(resrc);
	}
	public Asset getRFVTypeIcon(ResourceFileVersion rfv){
		return fileViewPage.getRFVTypeIcon(rfv);
	}
	
	public void loadUsageDetails(){
		
		switch(type){
		case ANNOUNCEMENT:
			annmtList = annmtDAO.getAnnouncements(proj, user);
			totalSize = annmtList.size();
			break;
		case FORUM_THREAD:
			thrdList = forumDAO.getThreads(proj, user);
			totalSize = thrdList.size();
			break; 
		case FORUM_THREAD_LIKE:
			thrdList = forumDAO.getThreadsRate(proj, user, RateType.GOOD);
			totalSize = thrdList.size();
			break; 
		case FORUM_THREAD_YOU_LIKE:
			thrdList = forumDAO.getThreadsYouRate(proj, user, RateType.GOOD, getCurUser());
			totalSize = thrdList.size();
			break; 
		case FORUM_THREAD_DISLIKE:
			thrdList = forumDAO.getThreadsRate(proj, user, RateType.BAD);
			totalSize = thrdList.size();
			break; 
		case FORUM_THREAD_YOU_DISLIKE:
			thrdList = forumDAO.getThreadsYouRate(proj, user, RateType.BAD, getCurUser());
			totalSize = thrdList.size();
			break; 
		case FORUM_THREAD_REPLY:
			thrdRList = forumDAO.getThreadReplies(proj, user);
			totalSize = thrdRList.size();
			break; 
		case FORUM_THREAD_REPLY_LIKE:
			thrdRList = forumDAO.getThreadRepliesRate(proj, user, RateType.GOOD);
			totalSize = thrdRList.size();
			break; 
		case FORUM_THREAD_REPLY_YOU_LIKE:
			thrdRList = forumDAO.getThreadRepliesYouRate(proj, user, RateType.GOOD, getCurUser());
			totalSize = thrdRList.size();
			break; 
		case FORUM_THREAD_REPLY_DISLIKE:
			thrdRList = forumDAO.getThreadRepliesRate(proj, user, RateType.BAD);
			totalSize = thrdRList.size();
			break; 
		case FORUM_THREAD_REPLY_YOU_DISLIKE:
			thrdRList = forumDAO.getThreadRepliesYouRate(proj, user, RateType.BAD, getCurUser());
			totalSize = thrdRList.size();
			break; 
		case FORUM_REFLECTION:
			reflctList = llogDAO.getForumReflections(proj, user, getCurUser());
			totalSize = reflctList.size();
			break; 
		case BLOG:
			blogList = blogDAO.getBlogs(proj, user, BlogStatus.PUBLISHED, getCurUser());
			totalSize = blogList.size();
			break; 
		case BLOG_COMMENT:
			blogCmtList = blogDAO.getBlogComments(proj, user);
			totalSize = blogCmtList.size();
			break; 
		case BLOG_REFLECTION:
			reflctList = llogDAO.getBlogReflections(proj, user, getCurUser());
			totalSize = reflctList.size();
			break; 
		case ELOG:
			elogList = elogDAO.getElogs(proj, user, getCurUser());
			totalSize = elogList.size();
			break; 
		case ELOG_COMMENT:
			elogCmtList = elogDAO.getElogComments(proj, user);
			totalSize = elogCmtList.size();
			break; 
		case ELOG_REFLECTION:
			reflctList = llogDAO.getElogReflections(proj, user, getCurUser());
			totalSize = reflctList.size();
			break; 
		case RESOURCE_FOLDER:
			rsrcFdList = resourceDAO.getFolders(proj, user);
			totalSize = rsrcFdList.size();
			break; 
		case RESOURCE_LINK:
			rsrcLkList = resourceDAO.getLinks(proj, user);
			totalSize = rsrcLkList.size();
			break; 
		case RESOURCE_FILE:
			rsrcFileList = resourceDAO.getFiles(proj, user);
			totalSize = rsrcFileList.size();
			break; 
		case RESOURCE_FILE_VERSION:
			rsrcFileVerList = resourceDAO.getFileVersions(proj, user);
			totalSize = rsrcFileVerList.size();
			break; 
		case SCHEDULING_MILESTONE:
			schdMstList = scheduleDAO.getMilestones(proj, user);
			totalSize = schdMstList.size();
			break; 
		case SCHEDULING_PHASE:
			schdPhsList = scheduleDAO.getPhases(proj, user);
			totalSize = schdPhsList.size();
			break; 
		case SCHEDULING_TASK:
			schdTskList = scheduleDAO.getTasks(proj, user);
			totalSize = schdTskList.size();
			break; 
		case SCHEDULING_ASSIGNED_TASK:
			schdTskList = scheduleDAO.getTasksAssigned(proj, user);
			totalSize = schdTskList.size();
			break; 
		 
		}
		
		
	}

	
	public BeanModel<Announcement> getAnnmtModel() {
		BeanModel<Announcement> model = beanModelSource.createEditModel(Announcement.class, messages);
        model.include("subject","startDate","endDate","createDate","modifyDate");
        model.add("no",null);
        model.add("creator", propertyConduitSource.create(Announcement.class, "creator.displayName")); 
		model.reorder("no","subject","creator");
        
        return model;
    }
	public BeanModel<Thread> getThreadModel() {
		BeanModel<Thread> model = beanModelSource.createEditModel(Thread.class, messages);
        model.include("name","createDate","modifyDate");
        model.add("no",null);
        model.add("replies",propertyConduitSource.create(Thread.class, "replies.size()"));
        model.add("author", propertyConduitSource.create(Thread.class, "author.displayName"));
        model.add("rates", null);
        model.reorder("no","name","rates","replies","author");
        
        return model;
    }
	public BeanModel<ThreadReply> getThreadReplyModel() {
		BeanModel<ThreadReply> model = beanModelSource.createEditModel(ThreadReply.class, messages);
        model.include("name","createDate","modifyDate");
        model.add("no",null);
        model.add("author", propertyConduitSource.create(ThreadReply.class, "author.displayName"));
        model.add("thread", propertyConduitSource.create(ThreadReply.class, "thread.name"));
        model.add("rates", null);
        model.reorder("no","name","thread","rates","author");
        
        return model;
    }
	public BeanModel<LogEntry> getReflectionModel() {
		BeanModel<LogEntry> model = beanModelSource.createEditModel(LogEntry.class, messages);
        model.include("content","cdate","mdate","shared");
        model.add("no",null);
        model.add("creator", propertyConduitSource.create(LogEntry.class, "creator.displayName"));
    	model.add("type",null);
    	switch(type){
    	case FORUM_REFLECTION:
    		model.get("type").label(messages.get("thread"));
    		break;
    	case BLOG_REFLECTION:
    		model.get("type").label(messages.get("blog"));
    		break;
    	case ELOG_REFLECTION:
    		model.get("type").label(messages.get("elog"));
    		break;
    	}
        
    	model.reorder("no","content","type","creator","shared");
        
        return model;
    }
	public BeanModel<Blog> getBlogModel() {
		BeanModel<Blog> model = beanModelSource.createEditModel(Blog.class, messages);
        model.include("subject","cdate","mdate");
        model.add("no",null);
        model.add("comments", propertyConduitSource.create(Blog.class, "comments.size()"));
        model.add("author", propertyConduitSource.create(Blog.class, "author.displayName"));
        model.reorder("no","subject","comments","author");
        
        return model;
    }
	
	public BeanModel<BlogComment> getBlogCommentModel() {
		BeanModel<BlogComment> model = beanModelSource.createEditModel(BlogComment.class, messages);
        model.include("content","cdate");
        model.add("no",null);
        model.get("content").label(messages.get("comment"));
        model.add("author", propertyConduitSource.create(BlogComment.class, "author.displayName"));
        model.add("blog", propertyConduitSource.create(BlogComment.class, "blog.subject"));
        model.reorder("no","content","blog","author");
        
        return model;
    }
	
	public BeanModel<Elog> getElogModel() {
		BeanModel<Elog> model = beanModelSource.createEditModel(Elog.class, messages);
        model.include("subject","cdate","mdate");
        model.add("no",null);
        model.add("comments", propertyConduitSource.create(Elog.class, "comments.size()"));
        model.add("author", propertyConduitSource.create(Elog.class, "author.displayName"));
        model.reorder("no","subject","comments","author");
        return model;
    }
	
	public BeanModel<ElogComment> getElogCommentModel() {
		BeanModel<ElogComment> model = beanModelSource.createEditModel(ElogComment.class, messages);
        model.include("content","cdate");
        model.add("no",null);
        model.get("content").label(messages.get("comment"));
        model.add("author", propertyConduitSource.create(ElogComment.class, "author.displayName"));
        model.add("elog", propertyConduitSource.create(ElogComment.class, "elog.subject"));
        model.reorder("no","content","elog","author");
        return model;
    }
	
	public BeanModel<ResourceFolder> getResourceFolderModel() {
		BeanModel<ResourceFolder> model = beanModelSource.createEditModel(ResourceFolder.class, messages);
        model.include("name","des","cdate","mdate");
        model.add("no",null);
        model.add("owner", propertyConduitSource.create(ResourceFolder.class, "owner.displayName"));
        model.add("fileCount", null);
        model.reorder("no","name","des","fileCount","owner");
        return model;
    }
	public BeanModel<ResourceLink> getResourceLinkModel() {
		BeanModel<ResourceLink> model = beanModelSource.createEditModel(ResourceLink.class, messages);
        model.include("name","des","url","cdate","mdate");
        model.add("no",null);
        model.add("owner", propertyConduitSource.create(ResourceLink.class, "owner.displayName"));
        model.add("inFolder", propertyConduitSource.create(ResourceLink.class, "parent.name"));
        model.reorder("no","name","des","url","inFolder","owner");
        return model;
    }
	public BeanModel<ResourceFile> getResourceFileModel() {
		BeanModel<ResourceFile> model = beanModelSource.createEditModel(ResourceFile.class, messages);
        model.include("name","des","cdate","mdate");
        model.add("no",null);
        model.add("typeIcon",null).label("");
        model.add("owner", propertyConduitSource.create(ResourceFile.class, "owner.displayName"));
        model.add("inFolder", propertyConduitSource.create(ResourceFile.class, "parent.name"));
        model.add("latestVersion", propertyConduitSource.create(ResourceFile.class, "getLatestVersion()"));
        model.add("totalDownloaded", propertyConduitSource.create(ResourceFile.class, "getTotalNumDownload()"));
        model.reorder("no","typeIcon","name","des","inFolder","owner");
        return model;
    }
	public BeanModel<ResourceFileVersion> getResourceFileVersionModel() {
		BeanModel<ResourceFileVersion> model = beanModelSource.createEditModel(ResourceFileVersion.class, messages);
        model.include("name","size","cmmt","cdate","numDownload","version");
        model.add("no",null);
        model.get("cmmt").label(messages.get("comment"));
        model.add("typeIcon",null).label("");
        model.add("owner", propertyConduitSource.create(ResourceFileVersion.class, "owner.displayName"));
        model.add("file", propertyConduitSource.create(ResourceFileVersion.class, "rfile.name"));
        model.reorder("no","typeIcon","name","cmmt","file","version","owner");
        return model;
    }
	public BeanModel<Milestone> getSchdMilestoneModel() {
		BeanModel<Milestone> model = beanModelSource.createEditModel(Milestone.class, messages);
        model.include("name","urgency","comment","deadline","createDate","modifyDate");
        model.add("no",null);
        model.add("manager", propertyConduitSource.create(Milestone.class, "manager.displayName"))
        	.label(messages.get("creator-label"));
        model.reorder("no","name","comment","urgency","manager");
        return model;
    }
	public BeanModel<Phase> getSchdPhaseModel() {
		BeanModel<Phase> model = beanModelSource.createEditModel(Phase.class, messages);
        model.include("name","comment","startDate","endDate","createDate","modifyDate");
        model.add("no",null);
        model.add("manager", propertyConduitSource.create(Phase.class, "manager.displayName"))
        	.label(messages.get("creator-label"));
        model.reorder("no","name","comment","manager");
        return model;
    }
	public BeanModel<Task> getSchdTaskModel() {
		BeanModel<Task> model = beanModelSource.createEditModel(Task.class, messages);
        model.include("name","urgency","comment","percentageDone","startDate","endDate","createDate");
        model.add("no",null);
        model.add("manager", propertyConduitSource.create(Task.class, "manager.displayName"))
        	.label(messages.get("creator-label"));
        model.reorder("no","name","comment","urgency","manager");
        return model;
    }
	
	
}
