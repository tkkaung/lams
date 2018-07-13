package ntu.celt.eUreka2.pages.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.components.TreeNode;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.ProjTypeSetting;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.TransactionStatus;
import ntu.celt.eUreka2.modules.budget.TransactionType;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.pages.modules.scheduling.TaskView;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

@Import(library={"context:modules/resources/js/context-menu.js"
		,"context:modules/resources/js/drag-drop-folder-tree.js"
		}
	,stylesheet={"Home.css","context:modules/resources/css/drag-drop-folder-tree.css"
		,"context:modules/resources/css/context-menu.css"})
public class Home extends AbstractPageProject {
	@Property
	private String projId;
	@Property
	private Project project;
	@Property
	private List<ProjRole> projRoles;
	@SuppressWarnings("unused")
	@Property
	private ProjRole projRole;
	@SuppressWarnings("unused")
	@Property
	private ProjectAttachedFile tempAttFile;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private SchlTypeAnnouncementDAO schlTypeAnnmtDAO;
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
	private BudgetDAO budgetDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private ProjExtraInfoDAO projExtraInfoDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	
	private final int PROJ_HOME_MAX_RESULT = Config.getInt("PROJ_HOME_MAX_RESULT");
	private final int PROJ_HOME_LAST_NUM_DAY = Config.getInt("PROJ_HOME_LAST_NUM_DAY");
	
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender(){	
		project = projDAO.getProjectById(projId);
		if(project==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
		projRoles = projRoleDAO.getAllRole();
		projRoles = filterProjRoles(projRoles);
	}
	private List<ProjRole> filterProjRoles(List<ProjRole> projRoles){
		if(hasSettingHideMemberList(project)){
			for(int i= projRoles.size()-1; i>=0; i--){
				ProjRole prole = projRoles.get(i);
				if(!prole.getPrivileges().contains(new Privilege(PrivilegeProject.IS_LEADER))){
					projRoles.remove(i);
				}
			}
		}
		return projRoles;
	}
	
	private Module getModule(String name, Project proj){
		for(ProjModule pm : proj.getProjmodules()){
			if(pm.getModule().getName().equalsIgnoreCase(name)){
				return pm.getModule();
			}
		}
		return null;
	}
	public Module getAnnmtModule(){
		return getModule(PredefinedNames.MODULE_ANNOUNCEMENT, project);
	}
	public Module getMessageModule(){
		return getModule(PredefinedNames.MODULE_MESSAGE, project);
	}
	public Module getResourceModule(){
		return getModule(PredefinedNames.MODULE_RESOURCE, project);
	}
	public Module getSchedulingModule(){
		return getModule(PredefinedNames.MODULE_SCHEDULING, project);
	}
	public Module getForumModule(){
		return getModule(PredefinedNames.MODULE_FORUM, project);
	}
	public Module getBlogModule(){
		return getModule(PredefinedNames.MODULE_BLOG, project);
	}
	public Module getElogModule(){
		return getModule(PredefinedNames.MODULE_ELOG, project);
	}
	public Module getBudgetModule(){
		return getModule(PredefinedNames.MODULE_BUDGET, project);
	}
	
	
	
	@SuppressWarnings("unused")
	@Property
	private ProjUser pUser;
	private int userNo = 0;  //note: can cause trouble if want to use navPager
	public int getNo(){ //can use only on 1 location of the page
		userNo++;
		return userNo;
	}
	
	
	@SuppressWarnings("unused")
	@Property
	private Announcement annmt;
	@SuppressWarnings("unused")
	@Property
	private SchlTypeAnnouncement schlTypeAnnmt;
	
	@Cached
	public List<Announcement> getAnnmts(){
		return annmtDAO.getActiveAnnouncements(project, PROJ_HOME_MAX_RESULT, getCurUser());
	}
	@Cached
	public List<SchlTypeAnnouncement> getSchlTypeAnnmts(){
		return	schlTypeAnnmtDAO.getActiveSchlTypeAnnouncements(project, getCurUser());
		
	}
	public boolean hasAnnmts(){
		return (getAnnmts().size() + getSchlTypeAnnmts().size()) > 0;
	}
	
	
	
	
	
	@SuppressWarnings("unused")
	@Property
	private TreeNode treeNode;
	private List<TreeNode> treeNodes;
	
	//this part of code copied from ntu/celt/eUreka2/pages/modules/resources/HOME
	@Cached
	public List<TreeNode> getResourceTreeNodes(){ 
		List<ResourceFolder> sharedFolders = resourceDAO.getSharedRootFolders(project);
		List<ResourceFolder> privateFolders = resourceDAO.getPrivateRootFolders(getCurUser());
		
		List<ResourceFolder> rootFolders = new ArrayList<ResourceFolder>();
		rootFolders.addAll(sharedFolders);
		rootFolders.addAll(privateFolders);
		
		treeNodes = new ArrayList<TreeNode>();
		int depth = 1;
		for (ResourceFolder fd : rootFolders) {
			String icon = "";
			if(fd.isShared())
				icon = "folder_shared.gif";
			else
				icon = "folder_private.gif";
			
			treeNodes.add(new TreeNode(fd.getId(), fd.getName(), fd.getDes(), depth, icon));
			initChildNode(fd, depth + 1); 
		}
		return treeNodes;
	}
	private void initChildNode(ResourceFolder fd, int d) {
		List<ResourceFolder> folders = resourceDAO.getFoldersByParent(fd);
		for (ResourceFolder rs : folders) {
			treeNodes.add(new TreeNode(rs.getId(), rs.getName(), rs.getDes(), d));
			initChildNode(rs, d + 1); 
		}
	}
	public long countResourcesByFolder(String folderId){
		ResourceFolder folder = resourceDAO.getFolderById(folderId);
		return resourceDAO.getCountResourcesByFolder(folder);
	}
	
	
	
	
	
	
	
	@Inject
	@Path("context:lib/img/icon_urgent.png")
	private Asset warning_gif;
	public String getSchedulingOutput(){
		StringBuffer str = new StringBuffer();
		List<Task> tList = new ArrayList<Task>();
		List<Milestone> mList = new ArrayList<Milestone>();
		Schedule schdl = scheduleDAO.getActiveSchedule(project);
		if(schdl!=null){
			tList = scheduleDAO.getUnfinishedTasks(schdl, getCurUser());
			mList = schdl.getMilestones();
		}
		int percentDone = 0;
		int countTotal = 0;
		int countCompleted = 0;
		int countNotStarted = 0;
		int countInprogress = 0;
		int countNotStartedOverdue = 0;
		int countInprogressOverdue = 0;
		Date now = new Date();
		//initialize parameters
		for(Milestone m : mList){
			for(Task t: m.getTasks()){
				countTotal++;
				percentDone += t.getPercentageDone();
				if(t.getPercentageDone()==100)
					countCompleted++;
				else if(t.getPercentageDone()==0){
					countNotStarted++;
					if(t.getEndDate().before(now))
						countNotStartedOverdue++;
				}
				else if(t.getPercentageDone()<100 && t.getPercentageDone()>0){
					countInprogress++;
					if(t.getEndDate().before(now))
						countInprogressOverdue++;
				}
			}
		}
				if(countTotal != 0)
			percentDone = Math.round(percentDone / countTotal);
		
		str.append("<table class='nomargin'><tr><td valign='top' width='49%'>");
		
		if(tList==null || tList.isEmpty()){
			str.append(messages.get("no-task"));
		}
		else{
			
			str.append("<h3 class='altColor'>"+messages.get("your-current-task")+":</h3>");
			str.append("<ul>");
			for(int i=0; i<tList.size() && i<PROJ_HOME_MAX_RESULT; i++){
				Task t = tList.get(i);
				str.append("<li>"
						+"<a href=\""+linkSource.createPageRenderLinkWithContext(TaskView.class, t.getId())+"\">"
						+ ((t.getEndDate().before(new Date()))? "<img src=\""+warning_gif+"\" title='"+messages.get("overdue")+"' />" : "")
						+ "&nbsp;"+ t.getName()
						+"</a>"
						+ "&nbsp; - " + messages.get("due-date")+": <span class='date'>"+t.getEndDateDisplay()+"</span>"
						
					+"<li>");
			}
			str.append("</ul>");
		}
		str.append("</td>");
		
		
		int statusWidth = 300; //width to display percentage status
		
		str.append("<td valign='top'>");
		str.append("<h3 class='altColor'>"+messages.get("overall-tasks-status")+"</h3>");
		str.append("<table><tr><td colspan='2'>"
				+ "<div class='schd_statusBar "+((countNotStartedOverdue+countInprogressOverdue)>0 ? "overdue":"")
						+"' style='width: "+statusWidth+"px;' >"
				+ "<div class='done' "
					+ "style='width: "+ Math.round(percentDone*statusWidth/100) +"px;' "
				+ "></div>"
				+ "</div>"
				+ "</td></tr>"
				+ "<tr>"
				+ "<td align='left'><span class='taskCompleted'>"+percentDone+"% "+messages.get("task-completed")+"</span></td>"
				+ "<td align='right'><span class='taskOutstanding "+((countNotStartedOverdue+countInprogressOverdue)>0 ? "overdue":"")+"'>"+(100-percentDone)+"% "+messages.get("task-outstanding")+"</span></td>"
				+ "</tr></table>"
			);
		
		//if(percentDone!=0 && percentDone!=100){
		if(countTotal != 0){
			str.append("<div>");
			str.append("<h3 class='altColor'>"+messages.get("breakdown-tasks")+"</h3>");
			str.append(messages.get("total")+" = " + countTotal
				+ "<br/>"+messages.get("completed")+" = " + countCompleted  
				+ "<br/>"+messages.get("not-started")+" = " + countNotStarted + (countNotStartedOverdue==0? "": " ("+ countNotStartedOverdue+" "+messages.get("overdue")+")")
				+ "<br/>"+messages.get("in-progress")+" = " + countInprogress + (countInprogressOverdue==0? "": " ("+ countInprogressOverdue+" "+messages.get("overdue")+")")
				);
			str.append("</div>");
		}
		
		str.append("</td>");
		str.append("</tr></table>");
		
		return str.toString();
	}
	
	@SuppressWarnings("unused")
	@Property
	private Thread thrd;
	public List<Thread> getThreads(){
		
		List<Thread> thList = forumDAO.getLatestThreads(project, PROJ_HOME_MAX_RESULT, PROJ_HOME_LAST_NUM_DAY) ;
		return thList;
	}
	@SuppressWarnings("unused")
	@Property
	private ThreadReply thrdR;
	public List<ThreadReply> getThreadRs(){
		List<ThreadReply> thRList = forumDAO.getLatestThreadReplies(project, PROJ_HOME_MAX_RESULT, PROJ_HOME_LAST_NUM_DAY) ;
		return thRList;
	}
	public String getNoThreadMessage(){
		return messages.format("no-thread-in-x-day", PROJ_HOME_LAST_NUM_DAY);
	}
	public String getNoThreadReplyMessage(){
		return messages.format("no-thread-reply-in-x-day", PROJ_HOME_LAST_NUM_DAY);
	}
	
	public long getTotalForum(){
		return forumDAO.getTotalForums(project);
	}
	public long getTotalRootThread(){
		return forumDAO.getTotalThreads(project);	
	}
	public long getTotalThreadReply(){
		return forumDAO.getTotalThreadReplies(project);
	}
	public long getTotalThreadReflection(){
		return forumDAO.getTotalThreadReflection(project, getCurUser());
	}
	
	
	@SuppressWarnings("unused")
	@Property
	private Blog blog;
	public List<Blog> getBlogs(){
		List<Blog> bList = blogDAO.getActiveBlogs(project, getCurUser());
		if(bList != null && bList.size() > PROJ_HOME_MAX_RESULT)
			bList = bList.subList(0, PROJ_HOME_MAX_RESULT);
		return bList;
	}
	public long getTotalBlog(){
		List<Blog> bList = blogDAO.getActiveBlogs(project, getCurUser());
		if(bList!=null)
			return bList.size();
		else
			return 0;
	}
	public long getTotalBlogComment(){
		return blogDAO.getTotalBlogComment(project, getCurUser());
	}
	
	@SuppressWarnings("unused")
	@Property
	private Elog elog;
	public List<Elog> getElogs(){
		List<Elog> bList = elogDAO.getActiveElogs(project, getCurUser());
		if(bList != null && bList.size() > PROJ_HOME_MAX_RESULT)
			bList = bList.subList(0, PROJ_HOME_MAX_RESULT);
		return bList;
	}
	public long getTotalElog(){
		List<Elog> bList = elogDAO.getActiveElogs(project, getCurUser());
		if(bList!=null)
			return bList.size();
		else
			return 0;
	}
	public int getCountPendingElog(){
		List<Elog> elogs = elogDAO.searchElogs(null, null, project, null, ElogStatus.PENDING,null, null, null, null, null, getCurUser());
		return elogs.size();
	}
	public long getTotalElogComment(){
		return elogDAO.countElogComments(project);
	}
	public boolean isElogPublish(Elog elog){
		if(elog.getStatus().equals(ElogStatus.APPROVED))
			return true;
		return false;
	}
	
	public long getCountInflow(){
		return budgetDAO.getCountTransactionsByType(project, TransactionType.IN_FLOW);
	}
	public String getTotalInflow(){
		return Util.formatCurrency(budgetDAO.getTotalTransactionsByType(project, TransactionType.IN_FLOW));
	}
	public String getTotalInflowPending(){
		return Util.formatCurrency(budgetDAO.getTotalTransactionsByTypeAndStatus(project, TransactionType.IN_FLOW, TransactionStatus.PENDING));
	}
	public boolean hasInflowPending(){
		if(0 == budgetDAO.getTotalTransactionsByTypeAndStatus(project, TransactionType.IN_FLOW, TransactionStatus.PENDING))
				return false;
		return true;
	}
	public long getCountOutflow(){
		return budgetDAO.getCountTransactionsByType(project, TransactionType.OUT_FLOW);
	}
	public String getTotalOutflow(){
		return Util.formatCurrency(budgetDAO.getTotalTransactionsByType(project, TransactionType.OUT_FLOW));
	}
	public String getTotalOutflowPending(){
		return Util.formatCurrency(budgetDAO.getTotalTransactionsByTypeAndStatus(project, TransactionType.OUT_FLOW, TransactionStatus.PENDING));
	}
	public boolean hasOutflowPending(){
		if(0 == budgetDAO.getTotalTransactionsByTypeAndStatus(project, TransactionType.OUT_FLOW, TransactionStatus.PENDING))
				return false;
		return true;
	}
	public String getCurrentBalance(){
		double balance = budgetDAO.getTotalTransactionsByType(project, TransactionType.IN_FLOW)
						- budgetDAO.getTotalTransactionsByType(project, TransactionType.OUT_FLOW);
		return Util.formatCurrency(balance);
	}
	
	
	
	
	
	public String getEmailsAsString(){
		String emails = "";
		for(ProjUser pu : project.getMembers()){
			emails += pu.getUser().getEmail()+",";
		}
		
		return Util.removeLastSeparator(emails, ",");
	}
	
	
	public ProjUser getProjUser(User user, Project proj){
		return proj.getMember(user);
	}
	@Cached
	public ProjCAOExtraInfo getProjExtraInfo(){
		ProjCAOExtraInfo projExtraInfo = projExtraInfoDAO.getProjExtraInfoByProject(project);
		if(projExtraInfo==null)
			projExtraInfo = new ProjCAOExtraInfo();
		return projExtraInfo;
	}
	public boolean hasProjExtrInfo(Project project){
		ProjCAOExtraInfo projExtraInfo = projExtraInfoDAO.getProjExtraInfoByProject(project);
		if(projExtraInfo==null)
			return false;
		return true;
	}
	
	public boolean hasSettingHideMemberList(Project project){
		if(project.getType().getSettings().contains(ProjTypeSetting.HIDE_MEMBER_LIST))
			return true;
		return false;
	}
	
	private final String overListUserClass = "oUser";
	public String getOverListUserClass(){
		if(userNo >= PROJ_HOME_MAX_RESULT)
			return overListUserClass;
		return "";
	}
	public boolean hasOverListUser(){
		if(userNo > PROJ_HOME_MAX_RESULT)
			return true;
		return false;
	}
	public String getNumMoreText(){
		int num = userNo - PROJ_HOME_MAX_RESULT;
		return messages.format("x-more", num);
	}
}
