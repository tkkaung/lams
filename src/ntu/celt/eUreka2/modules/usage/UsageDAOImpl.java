package ntu.celt.eUreka2.modules.usage;


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


public class UsageDAOImpl implements UsageDAO {

	private AnnouncementDAO annmtDAO;
	private AssessmentDAO assmtDAO;
	private ForumDAO forumDAO;
	private ResourceDAO resourceDAO;
	private SchedulingDAO scheduleDAO;
	private BlogDAO blogDAO;
	private ElogDAO elogDAO;
	private LearningLogDAO llogDAO;
	private BudgetDAO budgetDAO;
	
	
	
	public UsageDAOImpl(AnnouncementDAO annmtDAO, AssessmentDAO assmtDAO,
			ForumDAO forumDAO, ResourceDAO resourceDAO,
			SchedulingDAO scheduleDAO, BlogDAO blogDAO, ElogDAO elogDAO,
			LearningLogDAO llogDAO, BudgetDAO budgetDAO) {
		super();
		this.annmtDAO = annmtDAO;
		this.assmtDAO = assmtDAO;
		this.forumDAO = forumDAO;
		this.resourceDAO = resourceDAO;
		this.scheduleDAO = scheduleDAO;
		this.blogDAO = blogDAO;
		this.elogDAO = elogDAO;
		this.llogDAO = llogDAO;
		this.budgetDAO = budgetDAO;
	}



	@Override
	public boolean hasItemInProj(Project proj) {
		
		if(annmtDAO.countAnnouncements(proj)>0
			|| assmtDAO.countAssessmentsByProject(proj)>0
			|| budgetDAO.countTransactions(proj)>0
			|| blogDAO.countBlogs(proj)>0
			|| elogDAO.countElogs(proj)>0
			|| resourceDAO.countFiles(proj)>0
			|| resourceDAO.countLinks(proj)>0
			|| forumDAO.getTotalThreads(proj)>0
			|| scheduleDAO.countMilestones(proj)>0
			|| scheduleDAO.countPhases(proj)>0
			|| scheduleDAO.countTasks(proj)>0
			|| llogDAO.countLlogs(proj)>0)
		{	
			return true;
		}
		return false;
	}
  	
	
	
} 
