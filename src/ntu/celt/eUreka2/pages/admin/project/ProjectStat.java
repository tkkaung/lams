package ntu.celt.eUreka2.pages.admin.project;


import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ProjectStat extends AbstractPageAdminProject {
	@Property
	private String projId;
	@Property
	private Project proj;
	
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
	private LearningLogDAO llogDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private Messages messages;
	
	
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}
	void setupRender(){	
		proj = projDAO.getProjectById(projId);
		if(proj==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjId", projId));
		}
	}
	
	public String getBreadcrumbAdmin(){
		String s = "";
		if(canManageProjects())
			s = messages.get("admin-manage-project")+"=admin/project/manageadminprojects,";
		
		s += messages.get("manage-my-project")+"=admin/project/manageprojects";
		return s;
	}
	
	public int getNumView(){
		return proj.getNumVisit();
	}
	public int getNumMember(){
		return proj.getMembers().size();
	}
	public long getNumAdminAnnouncement(){
		return schlTypeAnnmtDAO.countSchlTypeAnnouncements(proj);
	}
	public long getNumAnnouncement(){
		return annmtDAO.countAnnouncements(proj);
	}
	public long getNumTransaction(){
		return budgetDAO.countTransactions(proj);
	}
	public long getNumBlog(){
		return blogDAO.countBlogs(proj);
	}
	public long getNumBlogComment(){
		return blogDAO.countBlogComments(proj);
	}
	public long getNumElog(){
		return elogDAO.countElogs(proj);
	}
	public long getNumElogComment(){
		return elogDAO.countElogComments(proj);
	}
	public long getNumFolder(){
		return resourceDAO.countFolders(proj);
	}
	public long getNumFile(){
		return resourceDAO.countFiles(proj);
	}
	public long getNumLink(){
		return resourceDAO.countLinks(proj);
	}
	public int getNumDownload(){
		return resourceDAO.countNumDownloads(proj);
	}
	public long getNumForum(){
		return forumDAO.getTotalForums(proj);
	}
	public long getNumThread(){
		return forumDAO.getTotalThreads(proj);
	}
	public long getNumThreadReply(){
		return forumDAO.getTotalThreadReplies(proj);
	}
	public long getNumThreadReflection(){
		return forumDAO.getTotalThreadReflection(proj, getCurUser());
	}
	public long getNumMilestone(){
		return scheduleDAO.countMilestones(proj);
	}
	public long getNumPhase(){
		return scheduleDAO.countPhases(proj);
	}
	public long getNumTask(){
		return scheduleDAO.countTasks(proj);
	}
	public long getNumLlog(){
		return llogDAO.countLlogs(proj);
	}
	public long getNumAssessment(){
		return assmtDAO.countAssessmentsByProject(proj);
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
		return getModule(PredefinedNames.MODULE_ANNOUNCEMENT, proj);
	}
	public Module getMessageModule(){
		return getModule(PredefinedNames.MODULE_MESSAGE, proj);
	}
	public Module getResourceModule(){
		return getModule(PredefinedNames.MODULE_RESOURCE, proj);
	}
	public Module getSchedulingModule(){
		return getModule(PredefinedNames.MODULE_SCHEDULING, proj);
	}
	public Module getForumModule(){
		return getModule(PredefinedNames.MODULE_FORUM, proj);
	}
	public Module getBlogModule(){
		return getModule(PredefinedNames.MODULE_BLOG, proj);
	}
	public Module getElogModule(){
		return getModule(PredefinedNames.MODULE_ELOG, proj);
	}
	public Module getBudgetModule(){
		return getModule(PredefinedNames.MODULE_BUDGET, proj);
	}
	public Module getLLogModule(){
		return getModule(PredefinedNames.MODULE_LEARNING_LOG, proj);
	}
	public Module getAssmtModule(){
		return getModule(PredefinedNames.MODULE_ASSESSMENT, proj);
	}
	
}
