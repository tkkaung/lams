package ntu.celt.eUreka2.modules.backuprestore;


import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.entities.ProjCAOExtraInfo;
import ntu.celt.eUreka2.entities.ProjFYPExtraInfo;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.HibernateUtil;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.Transaction;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogComment;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.ThreadReplyUser;
import ntu.celt.eUreka2.modules.forum.ThreadUser;
import ntu.celt.eUreka2.modules.learninglog.LLogFile;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;

public class Migrate2DAOImpl implements Migrate2DAO {

	private UserDAO userDAO;
	private ProjectDAO projDAO;
	private ProjStatusDAO projStatusDAO;
	private ProjTypeDAO projTypeDAO;
	private ProjRoleDAO projRoleDAO;
	private SchoolDAO schoolDAO;
	private SysRoleDAO sysRoleDAO;
	private ModuleDAO moduleDAO;
	private Logger logger;
	private ForumDAO forumDAO;
	private BudgetDAO budgetDAO;
	private SchedulingDAO schedulingDAO;
	private LearningLogDAO llogDAO;
	private ResourceDAO resourceDAO;
	private BlogDAO blogDAO;
	private ElogDAO elogDAO;
	private AssessmentDAO assmtDAO;
	

	public Migrate2DAOImpl(UserDAO userDAO, ProjectDAO projDAO,
			ProjStatusDAO projStatusDAO, ProjTypeDAO projTypeDAO,
			ProjRoleDAO projRoleDAO, SchoolDAO schoolDAO,
			SysRoleDAO sysRoleDAO, ModuleDAO moduleDAO, Logger logger,
			ForumDAO forumDAO, BudgetDAO budgetDAO,
			SchedulingDAO schedulingDAO, LearningLogDAO llogDAO,
			ResourceDAO resourceDAO, BlogDAO blogDAO, ElogDAO elogDAO,
			AssessmentDAO assmtDAO) {
		super();
		this.userDAO = userDAO;
		this.projDAO = projDAO;
		this.projStatusDAO = projStatusDAO;
		this.projTypeDAO = projTypeDAO;
		this.projRoleDAO = projRoleDAO;
		this.schoolDAO = schoolDAO;
		this.sysRoleDAO = sysRoleDAO;
		this.moduleDAO = moduleDAO;
		this.logger = logger;
		this.forumDAO = forumDAO;
		this.budgetDAO = budgetDAO;
		this.schedulingDAO = schedulingDAO;
		this.llogDAO = llogDAO;
		this.resourceDAO = resourceDAO;
		this.blogDAO = blogDAO;
		this.elogDAO = elogDAO;
		this.assmtDAO = assmtDAO;
	}

	@Override
	public Project getProjectById(String projId) {
		if(projId==null)
			return null; 
		
		Session session2 = HibernateUtil.getSessionFactory3().getCurrentSession();
		session2.beginTransaction();
		
		Project p = (Project) session2.get(Project.class, projId);
		
		//session2.getTransaction().commit(); //don't call, to allow Lazy loading info later
		
		return p;
	}
	
	@Override
	public User getOrImportUser(User u){
		if(u==null)
			return null;
		
		User u2 = userDAO.getUserByUsername(u.getUsername());
		if(u2==null){
			u2 = new User();
			//u.setId(id)
			u2.setExternalKey(u.getExternalKey());
			u2.setUsername(u.getUsername());
			u2.setPassword(u.getPassword());
			u2.setTitle(u.getTitle());
			u2.setFirstName(u.getFirstName());
			u2.setLastName(u.getLastName());
			u2.setJobTitle(u.getJobTitle());
			u2.setSchool(getOrImportSchool(u.getSchool()));
			u2.setSysRole(sysRoleDAO.getSysRoleByName(u.getSysRole().getName()));
			u2.setOrganization(u.getOrganization());
			u2.setPhone(u.getPhone());
			u2.setMphone(u.getMphone());
			u2.setEmail(u.getEmail());
			u2.setEnabled(u.isEnabled());
			u2.setCreateDate(u.getCreateDate());
			u2.setModifyDate(u.getModifyDate());
			u2.setIp(u.getIp());
			u2.setRemarks(u.getRemarks());
			//u2.setProjects(projects)
			for(SysroleUser su : u.getExtraRoles()){
				SysroleUser sru = new SysroleUser();
				sru.setSysRole(sysRoleDAO.getSysRoleByName(su.getSysRole().getName()));
				sru.setUser(u2);
				sru.setParam(su.getParam());
				u2.addExtraRole(sru);
			}
			userDAO.save(u2);
		}
		return u2;
	}
	@Override
	public School getOrImportSchool(School schoolFrom){
		if(schoolFrom==null)
			return null;
		
		School s = schoolDAO.getFirstSchoolByDescriptionWithoutDuplicateCheck(schoolFrom.getDes());
		if(s==null){
			s = new School();
			s.setName(schoolFrom.getName());
			s.setAlias(schoolFrom.getAlias());
			s.setDes(schoolFrom.getDes());
			schoolDAO.save(s);
		}
		return s;
	}
	
	@Override
	public Project importProjInfo(Project p, Project p2){
		p2.setId(p.getId());
		p2.setName(p.getName());
		p2.setDescription(p.getDescription());
		p2.setCdate(p.getCdate());
		p2.setMdate(p.getMdate());
		p2.setSdate(p.getSdate());
		p2.setEdate(p.getEdate());
		p2.setCourseId(p.getCourseId());
		p2.setGroupId(p.getGroupId());
		p2.setSeqNo(p.getSeqNo());
		p2.setShared(p.isShared());
		p2.setRemarks(p.getRemarks());
		p2.setLastAccess(p.getLastAccess());
		p2.setNumVisit(p.getNumVisit());
		p2.setLastStatusChange(p.getLastStatusChange());
		p2.setNoAutoChangeStatus(p.isNoAutoChangeStatus());
		p2.setCompanyInfo(p.getCompanyInfo());
		
		p2.setCreator(getOrImportUser(p.getCreator()));
		p2.setEditor(getOrImportUser(p.getEditor()));
		ProjType t = projTypeDAO.getTypeByName(p.getType().getName());
	//	if(t==null)
	//		t = importProjType(p.getType());
		p2.setType(t);
		p2.setSchool(getOrImportSchool(p.getSchool()));
		ProjStatus st = projStatusDAO.getStatusByName(p.getStatus().getName());
		p2.setStatus(st);
		
		for(ProjUser pu : p.getMembers()){
			User u = getOrImportUser(pu.getUser());
			p2.addMember(new ProjUser(p2, u
					, projRoleDAO.getRoleByName(pu.getRole().getName())));
		}
		for(ProjModule pm : p.getProjmodules()){
			p2.addModule(new ProjModule(p2
					, moduleDAO.getModuleByName(pm.getModule().getName())));
		}
		for(ProjectAttachedFile at : p.getAttachedFiles()){
			ProjectAttachedFile att = at.clone();
			att.setCreator(getOrImportUser(att.getCreator()));
			att.setProj(p2);
			p2.addAttachFile(att);
		}
		for(String kw : p.getKeywords()){
			p2.addKeywords(kw);
		}
		for(ProjCAOExtraInfo cao : p.getProjCAOExtraInfos()){
			ProjCAOExtraInfo caot = cao.clone();
			caot.setUser(getOrImportUser(caot.getUser()));
			caot.setApprover(getOrImportUser(caot.getApprover()));
			caot.setProject(p2);
			p2.getProjCAOExtraInfos().add(caot);
		}
		for(ProjFYPExtraInfo fyp : p.getProjFYPExtraInfos()){
			ProjFYPExtraInfo fypt = fyp.clone();
			fypt.setProject(p2);
			p2.getProjFYPExtraInfos().add(fypt);
		}
		
		Project proj = projDAO.getProjectById(p2.getId());
		if(proj!=null){ //found project with same ID, then set to NULL to re-generate when save to database
			p2.setId(null);
		}
		projDAO.immediateSaveProject(p2);

		return p2;
	}
	
	
	
	@Override
	public void importProjModules(Project p, Project p2){
		/*
		 * basically, what we do here is: 
		 * creating a clone of the item, then add to project. 
		 * but the entities (e.g User, Project...) still point to entities in the source database,
		 * we need to change them to point to entities in destination database.
		 * we assume to keep the attached file path and copy to file over in the backend server.
		 * (And want to change path, we should use code to copy the file (not coded yet))
		 * */
		/*
		 * variables naming here: number2 is destination, e.g: p means projectSource, p2 means projectDestination */
		for(Announcement a : p.getAnnouncements()){
			Announcement a2 = a.clone();
			a2.setProject(p2);
			a2.setCreator(getOrImportUser(a2.getCreator()));
			p2.getAnnouncements().add(a2);
		}
		for(Forum f : p.getForums()){
			Forum f2 = f.clone();
			f2.setProject(p2);
			f2.setCreator(getOrImportUser(f2.getCreator()));
			for(Thread t2 : f2.getThreads()){
				if(forumDAO.getThreadById(t2.getId())!=null){
					t2.setId(Util.generateLongID());
				}
				t2.setAuthor(t2.getAuthor());
				t2.setForum(f2);
				
				for(ThreadUser tu2 : t2.getThreadUsers()){
					tu2.setThread(t2);
					tu2.setUser(getOrImportUser(tu2.getUser()));
					//t2.addThreadUsers(tu2); //NO NEED TO ADD, (it's been done when cloning)
				}
				for(ForumAttachedFile at2 : t2.getAttachedFiles()){
					at2.setThread(t2);
					at2.setCreator(getOrImportUser(at2.getCreator()));
					if(forumDAO.getAttachedFileById(at2.getId())!=null){ //if attachment ID exist, use new one
						at2.setId(Util.generateUUID());
					}
				}
				for(ThreadReply tr2 : t2.getReplies()){
					tr2 = setThreadReply(t2, null, tr2);
				}
				//f2.addThread(t2); //NO NEED TO ADD
			}
			p2.getForums().add(f2);
		}
		for(Schedule s : p.getSchedules()){
			Schedule s2 = s.clone();
			Schedule activeSchedule = schedulingDAO.getActiveSchedule(p2);
			Schedule destinationSchd = s2;
			if(activeSchedule!=null && s2.isActive()){
				destinationSchd = activeSchedule;
			}else{
				s2.setProject(p2);
				s2.setUser(getOrImportUser(s2.getUser()));
			}
			
			for(Milestone m2 : s2.getMilestones()){
				m2.setManager(getOrImportUser(m2.getManager()));
				m2.setSchedule(destinationSchd);
				for(Phase ph2 : m2.getPhases()){
					ph2.setManager(getOrImportUser(ph2.getManager()));
					ph2.setMilestone(m2);
					for(Task t2 : ph2.getTasks()){
						t2.setEditor(getOrImportUser(t2.getEditor()));
						t2.setManager(getOrImportUser(t2.getManager()));
						t2.setMilestone(m2);
						t2.setPhase(ph2);
						Set<User> uList = t2.getAssignedPersons();
						t2.getAssignedPersons().clear();
						for(User u : uList){
							t2.addAssignedPerson(getOrImportUser(u));
						}
					}
				}
				for(Task t2 : m2.getNoPhaseTasks()){
					t2.setEditor(getOrImportUser(t2.getEditor()));
					t2.setManager(getOrImportUser(t2.getManager()));
					t2.setMilestone(m2);
					Set<User> uList = t2.getAssignedPersons();
					t2.getAssignedPersons().clear();
					for(User u : uList){
						t2.addAssignedPerson(getOrImportUser(u));
					}
				}
				
				if(activeSchedule!=null && s2.isActive()){
					activeSchedule.addMilestone(m2);
				}		
			}
			if(activeSchedule!=null && s2.isActive()){
				//update activeSchedule
				schedulingDAO.saveSchedule(activeSchedule);
			}else{
				p2.getSchedules().add(s2);
			}
			
		}
		for(Budget b : p.getBudgets()){
			Budget b2 = b.clone();
			Budget activeBudget = budgetDAO.getActiveBudget(p2);
			
			if(activeBudget!=null && b2.isActive()){
				for(Transaction t2 : b2.getTransactions()){
					t2.setBudget(activeBudget);
					t2.setCreator(getOrImportUser(t2.getCreator()));
					t2.setEditor(getOrImportUser(t2.getEditor()));
					activeBudget.addTransaction(t2);
				}
				budgetDAO.saveBudget(activeBudget);
			}
			else{
				b2.setProject(p2);
				for(Transaction t2 : b2.getTransactions()){
					t2.setBudget(b2);
					t2.setCreator(getOrImportUser(t2.getCreator()));
					t2.setEditor(getOrImportUser(t2.getEditor()));
				}
				p2.getBudgets().add(b2);
			}
		}
		for(Message m : p.getMessages()){
			Message m2 = m.clone();
			m2.setOwner(getOrImportUser(m2.getOwner()));
			m2.setProj(p2);
			m2.setSender(getOrImportUser(m2.getSender()));
			
			Set<User> uList = m2.getRecipients();
			m2.getRecipients().clear();
			for(User u : uList){
				m2.addRecipients(getOrImportUser(u));
			}
			p2.getMessages().add(m2);
		}
		for(LogEntry l : p.getLogEntrys()){
			LogEntry l2 = l.clone();
			l2.setCreator(getOrImportUser(l2.getCreator()));
			for(LLogFile at : l2.getFiles()){
				at.setLogEntry(l2);
				at.setCreator(getOrImportUser(at.getCreator()));
				if(llogDAO.getLLogFileById(at.getId())!=null){
					at.setId(Util.generateUUID());
				}
			}
			l2.setProject(p2);
			
			p2.getLogEntrys().add(l2);
		}
		for(Resource r : p.getResources()){
			if(!r.isShared())
				continue;
			Resource r2 = r.clone();
			
			//NOTE: note exactly sure why this way works. just resourceID is same with source and destination,
			//      once ID exist in destination, then trying to import again will cause 
			//       "a different object with the same identifier value was already associated with the session"
			
			if(resourceDAO.getResourceById(r2.getId())!=null){
				r2.setId(Util.generateUUID());
			}
			r2.setProj(p2);
			r2.setOwner(getOrImportUser(r2.getOwner()));
			r2.setEditor(getOrImportUser(r2.getEditor()));
			if(r2.isFile()){
				ResourceFile f2 = r2.toFile();
				f2.setLockedBy(getOrImportUser(f2.getLockedBy()));
				for(ResourceFileVersion rfv : f2.getFileVersions()){
					rfv.setOwner(getOrImportUser(rfv.getOwner()));
					rfv.setRfile(f2);
				}
			}
			p2.getResources().add(r2);
		}
		for(Blog b : p.getBlogs()){
			Blog b2 = b.clone();
			if(blogDAO.getBlogById(b2.getId())!=null){
				b2.setId(Util.generateUUID());
			}
			b2.setAuthor(getOrImportUser(b2.getAuthor()));
			b2.setProject(p2);
			for(BlogComment c : b2.getComments()){
				c.setAuthor(getOrImportUser(c.getAuthor()));
				c.setBlog(b2);
			}
			for(BlogFile at : b2.getAttaches()){
				at.setBlog(b2);
				at.setCreator(getOrImportUser(at.getCreator()));
				if(blogDAO.getBlogFileById(at.getId())!=null){
					at.setId(Util.generateUUID());
				}
			}
			
			p2.getBlogs().add(b2);
		}
		for(Elog e : p.getElogs()){
			Elog e2 = e.clone();
			if(elogDAO.getElogById(e2.getId())!=null){
				e2.setId(Util.generateUUID());
			}
			e2.setAuthor(getOrImportUser(e2.getAuthor()));
			e2.setProject(p2);
			for(ElogComment c : e2.getComments()){
				c.setAuthor(getOrImportUser(c.getAuthor()));
				c.setElog(e2);
			}
			for(ElogFile at : e2.getFiles()){
				at.setElog(e2);
				at.setCreator(getOrImportUser(at.getCreator()));
				if(elogDAO.getElogFileById(at.getId())!=null){
					at.setId(Util.generateUUID());
				}
			}
			
			p2.getElogs().add(e2);
		}
		for(Assessment a : p.getAssessments()){
			Assessment a2 = a.clone();
			a2.setCreator(getOrImportUser(a2.getCreator()));
			a2.setEditor(getOrImportUser(a2.getEditor()));
			a2.setProject(p2);
			if(a2.getRubric()!=null){
				a2.getRubric().setOwner(getOrImportUser(a2.getRubric().getOwner()));
				assmtDAO.addRubric(a2.getRubric());
			}
			for(AssessCriteria ac : a2.getCriterias()){
				ac.setAssessment(a2);
				for(AssessCriterion acr : ac.getCriterions()){
					if(assmtDAO.getAssessCriterionById(acr.getId())!=null){
						acr.setId(Util.generateLongID());
					}
					acr.setCriteria(ac);
				}
			}
			for(AssessmentUser au : a2.getAssmtUsers()){
				au.setAssessee(getOrImportUser(au.getAssessee()));
				au.setAssessor(getOrImportUser(au.getAssessor()));
				au.setAssessment(a2);
				//for(AssessCriterion acr : au.getSelectedCriterions()){
				//}
			}
			
			p2.getAssessments().add(a2);
		}
		
		projDAO.updateProject(p2);
		logger.debug("migrated proj.Id="+p.getId());
	/*	

		private Set<Assessment> assessments = new HashSet<Assessment>();
		private Set<BackupEntry> backupEntrys = new HashSet<BackupEntry>();
		*/
		
	}
	
	
	private ThreadReply setThreadReply(Thread t, ThreadReply parent, ThreadReply tr){
		tr.setAuthor(getOrImportUser(tr.getAuthor()));
		tr.setThread(t);
		tr.setParent(parent);
		
		for(ThreadReplyUser tru : tr.getThreadReplyUsers()){
			tru.setThreadReply(tr);
			tru.setUser(getOrImportUser(tru.getUser()));
		}
		
		for(ThreadReply c : tr.getChildren()){
			setThreadReply(t, tr, c);
		}
		for(ForumAttachedFile at2 : tr.getAttachedFiles()){
			at2.setThreadReply(tr);
			at2.setCreator(getOrImportUser(at2.getCreator()));
			if(forumDAO.getAttachedFileById(at2.getId())!=null){ //if attachment ID exist, use new one
				at2.setId(Util.generateUUID());
			}
		}
		
		return tr;
	}
}
