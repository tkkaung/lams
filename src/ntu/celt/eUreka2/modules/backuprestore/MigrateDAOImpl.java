package ntu.celt.eUreka2.modules.backuprestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.HibernateUtil;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.backuprestore.MigratedProjInfo;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MMember;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MMilestone;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MPhase;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MProject;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MTask;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MThread;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MUser;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumAttachedFile;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.ThreadType;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceFile;
import ntu.celt.eUreka2.modules.resources.ResourceFileVersion;
import ntu.celt.eUreka2.modules.resources.ResourceFolder;
import ntu.celt.eUreka2.modules.resources.ResourceLink;
import ntu.celt.eUreka2.modules.resources.ResourceType;
import ntu.celt.eUreka2.modules.scheduling.Milestone;
import ntu.celt.eUreka2.modules.scheduling.Phase;
import ntu.celt.eUreka2.modules.scheduling.ReminderType;
import ntu.celt.eUreka2.modules.scheduling.Schedule;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.Task;
import ntu.celt.eUreka2.modules.scheduling.UrgencyLevel;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings({ "deprecation" , "unchecked"})
public class MigrateDAOImpl implements MigrateDAO{
	
	private ProjectDAO projDAO;
	private UserDAO userDAO;
	private SysRoleDAO sysRoleDAO;
	private ProjRoleDAO projRoleDAO;
	private ProjStatusDAO projStatusDAO;
	private SchoolDAO schoolDAO;
	private ProjTypeDAO projTypeDAO;
	private ModuleDAO moduleDAO;
	private AnnouncementDAO annmtDAO;
	private SchedulingDAO schdDAO;
	private ForumDAO forumDAO;
	private BlogDAO blogDAO;
	private LearningLogDAO learninglogDAO;
	private ResourceDAO resrcDAO;
	private AttachedFileManager attFileManager;
	private Session session;
	
	private Logger logger;
	
	private final String REPO_MAP_DEFAULT_URL = Config.getString("REPO_MAP_DEFAULT_URL");  
	private final String REPO_DEAFAULT_DIR = Config.getString("REPO_DEAFAULT_DIR"); 
	
	
	
	public MigrateDAOImpl(ProjectDAO projDAO, UserDAO userDAO,
			SysRoleDAO sysRoleDAO, ProjRoleDAO projRoleDAO,
			ProjStatusDAO projStatusDAO, SchoolDAO schoolDAO,
			ProjTypeDAO projTypeDAO, ModuleDAO moduleDAO,
			AnnouncementDAO annmtDAO, SchedulingDAO schdDAO, ForumDAO forumDAO,
			BlogDAO blogDAO, LearningLogDAO learninglogDAO,
			ResourceDAO resrcDAO, AttachedFileManager attFileManager,
			Session session, Logger logger) {
		super();
		this.projDAO = projDAO;
		this.userDAO = userDAO;
		this.sysRoleDAO = sysRoleDAO;
		this.projRoleDAO = projRoleDAO;
		this.projStatusDAO = projStatusDAO;
		this.schoolDAO = schoolDAO;
		this.projTypeDAO = projTypeDAO;
		this.moduleDAO = moduleDAO;
		this.annmtDAO = annmtDAO;
		this.schdDAO = schdDAO;
		this.forumDAO = forumDAO;
		this.blogDAO = blogDAO;
		this.learninglogDAO = learninglogDAO;
		this.resrcDAO = resrcDAO;
		this.attFileManager = attFileManager;
		this.session = session;
		this.logger = logger;
	}
	
	/*functions to load from eureka1*/
	@Override
	public long getCountProjs(String status) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT count(c.campaign_id) FROM mbcampaign c " 
				+ ((status!=null)? " WHERE c.status = :status " : "" ) 	
				)
				;
		if(status!=null)
			q.setString("status", status);
		
		Object count = q.uniqueResult();
		session2.getTransaction().commit();
		
		if(count==null)
			return 0;
		if(count instanceof BigInteger)
			return ((BigInteger) count).longValue();
		if(count instanceof Integer)
			return new Long((Integer) count);
		
		return (Long) count;
	}
	@Override
	public List<String> getProjIDsByStatus(String status, Integer firstResult, Integer maxResult) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT c.campaign_id FROM mbcampaign c " 
				+ ((status!=null)? " WHERE c.status = :status " : "" ) 
				+ " ORDER BY createDate asc "
				)
				;
		if(status!=null)
			q.setString("status", status);
		if(firstResult!=null)
			q.setFirstResult(firstResult);
		if(maxResult!=null)	
			q.setMaxResults(maxResult);
			
		q.addScalar("campaign_id");
			
		List<Object> objList = q.list();
		session2.getTransaction().commit();

		List<String> idList = new ArrayList<String>();
		for(Object obj : objList ){
			idList.add((String) obj);
		}
		return idList;
	}
	
	@Override
	public List<MProject> getProjectsByUser(String username){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT c.* FROM mbcampaign c " 
					+ " JOIN mbmember m ON c.campaign_id = m.roll_id " 
					+ " JOIN mbuser u ON m.principal_id = u.user_id " 
					+ " WHERE u.userName = :userName "
					+ " AND m.manager = 1 "
					)
					.setString("userName", username);
		q.addScalar("campaign_id");
	
	
		List<Object> objList = q.list();
		session2.getTransaction().commit();

		List<MProject> mProjList = new ArrayList<MProject>();
		for(Object obj : objList ){
			MProject p = this.getMProjectById((String) obj);	
			if(p!=null)
				mProjList.add(p);
		}
		
		return mProjList;
	}
	@Override
	public long getCountSysRoles(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(*) AS total FROM mbgroup g " 
					+ " WHERE g.system = :true ")
					.setBoolean("true", true);
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	@Override
	public long getCountProjRoles(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(*) AS total FROM mbgroup g " 
				+ " WHERE g.system = :false ")
				.setBoolean("false", false);
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	@Override
	public long getCountProjStatuss(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(DISTINCT status) AS total FROM mbcampaign ");
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	@Override
	public long getCountProjTypes(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(DISTINCT campaign_type) AS total FROM mbcampaign ");
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	@Override
	public long getCountSchools(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(*) AS total FROM ntuschools s ");
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	@Override
	public long getCountUsers(){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(*) AS total FROM mbuser u  "); //...WHERE u.deleted=false
		q.addScalar("total", Hibernate.LONG);
		Long totalRecord = (Long) q.uniqueResult();
		
		session2.getTransaction().commit();
		return totalRecord;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllSysRoles(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT * FROM mbgroup g " 
					+ " WHERE g.system = :true ")
					.setBoolean("true", true);
		q.addScalar("group_id")
			.addScalar("fullName");
		
		List<Object []> objList = q.list();
		
		for(Object[] obj : objList){
			String roleId = (String) obj[0];
			SysRole r = sysRoleDAO.getSysRoleByName(roleId);
			if(r==null){ //role not exist, simple add new
				r = new SysRole();
				result.addAddedList(roleId);
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(roleId);
				}else{	
					result.addIgnoredList(roleId);
					continue;
				}
			}
			
			r.setName(roleId);
			r.setDescription((String) obj[1]);
			//r.setPrivileges(privileges);
			sysRoleDAO.save(r);
		}
		result.setWarnMessage("Please manually set Privileges for each role, as eureka1 does not contain these info");
		
		session2.getTransaction().commit();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllProjRoles(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT * FROM mbgroup g " 
					+ " WHERE g.system = :false ")
					.setBoolean("false", false);
		q.addScalar("group_id")
			.addScalar("fullName");
		
		List<Object []> objList = q.list();
		
		for(Object[] obj : objList){
			String roleId = (String) obj[0];
			ProjRole r = projRoleDAO.getRoleByName(roleId);
			if(r==null){ //role not exist, simple add new
				r = new ProjRole();
				result.addAddedList(roleId);
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(roleId);
				}else{	
					result.addIgnoredList(roleId);
					continue;
				}
			}
			r.setName(roleId);
			r.setDes((String) obj[1]);
			//r.setPrivileges(privileges);
			projRoleDAO.updateRole(r);
		}
		result.setWarnMessage("Please manually set Privileges for each role, as eureka1 does not contain these info");
		
		session2.getTransaction().commit();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllProjStatus(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT DISTINCT status FROM mbcampaign " );
			q.addScalar("status");
		
		List<Object> objList = q.list();
		
		for(Object obj : objList){
			String statusName = (String) obj;
			ProjStatus r = projStatusDAO.getStatusByName(statusName);
			if(r==null){ //status not exist, simple add new
				r = new ProjStatus();
				result.addAddedList(statusName);
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(statusName);
				}else{	
					result.addIgnoredList(statusName);
					continue;
				}
			}
			r.setName(statusName);
			//r.setDescription(description);
			projStatusDAO.updateStatus(r);
		}
		
		session2.getTransaction().commit();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllProjTypes(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT DISTINCT campaign_type FROM mbcampaign " );
			q.addScalar("campaign_type");
		
		List<Object> objList = q.list();
		
		for(Object obj : objList){
			String typeName = (String) obj;
			ProjType r = projTypeDAO.getTypeByName(typeName);
			if(r==null){ //type not exist, simple add new
				r = new ProjType();
				result.addAddedList(typeName);
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(typeName);
				}else{	
					result.addIgnoredList(typeName);
					continue;
				}
			}
			r.setName(typeName);
			//r.setModules(modules)
			//r.setDes(description);
			projTypeDAO.updateType(r);
		}
		
		session2.getTransaction().commit();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllSchools(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT * FROM ntuschools s ");
		q.addScalar("school_id")
			.addScalar("name")
			.addScalar("description");
		
		List<Object []> objList = q.list();
		
		for(Object[] obj : objList){
			String schoolId = (String) obj[0];
			School r = schoolDAO.getSchoolByName(schoolId);
			if(r==null){ //school not exist, simply add new
				r = new School();
				result.addAddedList(schoolId);
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(schoolId);
				}else{	
					result.addIgnoredList(schoolId);
					continue;
				}
			}
			r.setName(schoolId);
			r.setDes((String) obj[1]);
			schoolDAO.save(r);
		}
		
		session2.getTransaction().commit();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAllUsers(boolean replaceIfExist){
		ResultSummary result = new ResultSummary();
		Long totalRecord = this.getCountUsers();
		
		int MAX_RESULT = 500;
		int firstResult = 0;
		School defaultSchool = null;
		SysRole defaultSysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER);
		SysRole extUserSysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_EXTERNAL_USER);
		
		while(firstResult<=totalRecord){
			logger.info("Importing users......index from "+firstResult +" to "+(firstResult+MAX_RESULT-1)+"...total="+totalRecord);
		
		List<MUser> mUsers = this.getAllUsers(firstResult, MAX_RESULT);
		
		for(MUser mu : mUsers){
			User u = userDAO.getUserByUsername(mu.getUserName());
			if(u==null){ //u not exist, simple add new
				u = new User();
				result.addAddedList(mu.getUserName());
			}
			else{
				if(replaceIfExist){
					result.addReplacedList(mu.getUserName());
				}else{	
					result.addIgnoredList(mu.getUserName());
					continue;
				}
			}
			u.setCreateDate(mu.getCreateDate());
			u.setEmail(mu.getEmail());
			u.setEnabled(!mu.isDisabled());
			u.setExternalKey(mu.getExternal_person_key());
			u.setFirstName(mu.getFirstName());
			//u.setId(id);
			//u.setIp(ip);
			u.setTitle(mu.getTitle());
			u.setJobTitle(mu.getJob_title());
			u.setLastName(mu.getLastName());
			u.setUsername(mu.getUserName());
			u.setModifyDate(mu.getLastModifiedDate());
			//u.setMphone(mphone);
			//u.setOrganization(organization);
			u.setPassword(Util.generateHashValue(Util.nvl(mu.getPassword())));
			u.setPhone(mu.getContactNumber());
			//u.setProjects(projects);
			String remarks = "Migrated from eUreka1 on "+ Util.formatDateTime2(new Date());
			u.setRemarks(remarks);
			
			//set School
			School s = schoolDAO.getFirstSchoolByDescription(mu.getDepartment());
			if(s==null){
				s = defaultSchool;
			}
			u.setSchool(s);
			
			//set sysRole
			SysRole r = null;
			if(mu.isExternalUser())
				r = extUserSysRole;
			else
				r = sysRoleDAO.getSysRoleByName(mu.getInstitution_role());
			if(r==null){
				r = defaultSysRole;
			}
			u.setSysRole(r);
			
			
			
			
			userDAO.save(u);
			
		}
		
		firstResult += MAX_RESULT;
		}
		
		return result;
	}
	
	
	@CommitAfter
	@Override
	public Project importProjInfo(String mProjId) {
		MProject mP = this.getMProjectById(mProjId);	
		Project p = new Project();
		p.setName(mP.getName());
		p.setCdate(mP.getCreateDate());
		p.setCourseId(mP.getCourseId());
		MUser muser = this.getMUser(mP.getCreatorId());
		if(muser==null){
			//p.setCreator(null);
			logger.error("Invalidate Data, Not found creatorID: "+mP.getCreatorId()+" , eureka1 ProjectID: "+mProjId);
			return null;
		}
		else{
			User creator = userDAO.getUserByUsername(muser.getUserName());
			if(creator==null)
				creator = this.importUser(mP.getCreatorId(), false);
			p.setCreator(creator);
		}
		p.setDescription(Util.filterOutRestrictedHtmlTags(mP.getDesc()));
		p.setEdate(mP.getEndDate());
	//	p.setId(id)
		p.setMdate(mP.getLastModifiedDate());
		//p.setRemarks("");
		p.setSdate(mP.getStartDate());
		p.setShared(!mP.isPrivateProj());
		School s = schoolDAO.getSchoolByName(mP.getSchoolId()); 
		//assume school List already imported
		if(s==null){
			s = this.importSchool(mP.getSchoolId(), false);
		}
		p.setSchool(s); 
		
		
		ProjStatus st = null;
		if("ref".equals(mP.getStatus()))
			st =  projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_REFERENCE);
		else if("open".equals(mP.getStatus()))
			st =  projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE);
		else if("del".equals(mP.getStatus()))
			st =  projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_DELETED);
		else if("arc-done".equals(mP.getStatus()))
			st =  projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ARCHIVED);
		if(st!=null){
			p.setStatus(st);
			p.setLastStatusChange(mP.getStatusChangedDate());
		}
		else{
			p.setStatus(projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE));
			p.setLastStatusChange(new Date());
		}
		
		if("Senate".equalsIgnoreCase(mP.getSchoolId())){
			mP.setProjType(PredefinedNames.PROJTYPE_SENATE);
			s = schoolDAO.getSchoolByName("OAS");
			if(s!=null)
				p.setSchool(s); 
		}
		if(mP.isCao()){
			mP.setProjType(PredefinedNames.PROJTYPE_CAO);
		}
		else if("fyp".equals(mP.getProjType())){
			mP.setProjType(PredefinedNames.PROJTYPE_FYP);
		}
		else if("adhoc".equals(mP.getProjType())){
			mP.setProjType(PredefinedNames.PROJTYPE_ADHOC);
		}
		
		ProjType t = projTypeDAO.getTypeByName(mP.getProjType());
		if(t!=null)
			p.setType(t);
		else
			p.setType(projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_ADHOC));
		
		
		p = importProjMembers(mProjId, p);
		
		//projModule is added when importing the module
		
		
		projDAO.saveProject(p);
		
		return p;
	}
	@CommitAfter
	@Override
	public Project importProjMembers(String mProjId, Project toProject){
		ProjRole leaderRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_LEADER);
		ProjRole memberRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_MEMBER);
		
		String projType = toProject.getType().getName();
		if(projType.equals(PredefinedNames.PROJTYPE_SENATE)){
			leaderRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SECRETARY);
			memberRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SENATOR);
		}
		else if(projType.equals(PredefinedNames.PROJTYPE_CAO)){
			leaderRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SCHOOL_TUTOR);
			memberRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
		}
		else if(projType.equals(PredefinedNames.PROJTYPE_FYP)){
			leaderRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SUPERVISOR);
			memberRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_STUDENT);
		}
		else if(projType.equals(PredefinedNames.PROJTYPE_ADHOC)){
			
		}
		
		
		List<MMember> mMembers = this.getMembersByProj(mProjId);
		for(MMember mMem : mMembers){
			User u = userDAO.getUserByUsername(mMem.getUsername());
			if(u==null)
				u = this.importUser(mMem.getUserId(), false);
			if(!toProject.hasMember(u))
				toProject.addMember(new ProjUser(toProject, u, (mMem.isManager()? leaderRole : memberRole)));
			
		}
		projDAO.saveProject(toProject);
		
		return toProject;
	}
	
	
	@CommitAfter
	@Override
	public ResultSummary importAnnouncements(String mProjId, Project proj) {
		ResultSummary result = new ResultSummary();
		proj = assignModuleToProject(PredefinedNames.MODULE_ANNOUNCEMENT, proj);
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT n.*, u.userName AS creator FROM mbnews n "
				+ " JOIN mbuser u ON n.creator_id=u.user_id "
				+ " WHERE link_id = :projId ")
				.setString("projId", mProjId);
		q.addScalar("news_id")
			.addScalar("subject")
			.addScalar("body")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("creator")
			.addScalar("expiryDate", Hibernate.LONG)
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("launchDate", Hibernate.LONG)
			.addScalar("urgency")
			.addScalar("creator_id")
			;
		/*
		 news_id, link_id, subject, content, createDate, creator_id, expiryDate, lastModifiedDate, launchDate
		 */
		
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			String newsId = (String) obj[0];
			String subject = (String) obj[1];
			String body = (String) obj[2];
			Date createDate = longObjToDate( obj[3]);
			User creator = userDAO.getUserByUsername((String) obj[4]);
			if(creator==null)
				creator = importUser((String) obj[9], false); 
	
			Date expiryDate = longObjToDate( obj[5]);
			Date lastModifiedDate = longObjToDate( obj[6]);
			Date launchDate = longObjToDate( obj[7]);
			Boolean urgent = "yes".equalsIgnoreCase((String) obj[8])? true : false;
			
			Announcement ann = new Announcement();
								
			ann.setContent(body);
			ann.setCreateDate(createDate);
			ann.setCreator(creator);
			ann.setEndDate(expiryDate);
			//ann.setId(id);
			if(launchDate==null)
				launchDate = createDate;
			ann.setStartDate(launchDate);
			ann.setSubject(subject);
			ann.setUrgent(urgent);
			ann.setEnabled(true);
			ann.setModifyDate(lastModifiedDate);
			ann.setProject(proj);
			
			result.addAddedList(newsId + " : " +ann.getSubject());
			
			annmtDAO.save(ann);
		}
		
		return result;
	}
	@CommitAfter
	@Override
	public ResultSummary importActivityTasks(String mProjId, Project proj) {
		ResultSummary result = new ResultSummary();
		
		proj = assignModuleToProject(PredefinedNames.MODULE_SCHEDULING, proj);
		
		//create Schedule
		Schedule activeSchedule = schdDAO.getActiveSchedule(proj);
		if(activeSchedule!=null){
			activeSchedule.setActive(false);
			activeSchedule.setCreateDate(new Date());
			activeSchedule.setRemarks("AUTO-SAVED due to migrating new one");
			schdDAO.saveSchedule(activeSchedule);
		}
		Schedule schd = new Schedule();
		schd.setActive(true);
		schd.setCreateDate(new Date());
		//schd.setId(id);
		schd.setProject(proj);
		schd.setRemarks("Migrated from eUreka 1.0 on "+ Util.formatDateTime2(new Date()));
		
		List<MMilestone> mList = this.getMilestoneByProj(mProjId);
		for(MMilestone mm : mList){
			Milestone m = new Milestone();
			m.setComment(mm.getComments());
			m.setCreateDate(new Date()); 
			m.setModifyDate(new Date());
			m.setDeadline(mm.getDeadline());
			//m.setId(id);
			MUser mu = this.getMUser(mm.getOwner());
			User u = userDAO.getUserByUsername(mu.getUserName());
			if(u==null)
				u = this.importUser(mu.getUserId(), false);
			m.setManager(u);
			m.setName(mm.getName());
			m.setSchedule(schd);
		 	try{
				m.setUrgency(UrgencyLevel.valueOf(mm.getUrgency()));
		
			}catch(IllegalArgumentException e){
				m.setUrgency(UrgencyLevel.LOW);
			}
			for(MTask mt : mm.getTasks()){
				Task t = new Task();
				t.setComment(mt.getComments());
				t.setCompleteDate(mt.getDateCompleted());
				t.setCreateDate(new Date());
				//t.setDaysToRemind(daysEarlierToRemind) //TODO t.setDaysToRemind
				t.setEndDate(mt.getEndDate());
				//t.setId(id)
				MUser mu2 = this.getMUser(mt.getOwner_id());
				if(mu2==null)
					continue; //Owner_id not valid in eureka1
				User u2 = userDAO.getUserByUsername(mu2.getUserName());
				if(u2==null)
					u2 = this.importUser(mu2.getUserId(), false);
				t.setManager(u2);
				t.setMilestone(m);
				t.setModifyDate(t.getCreateDate());
				t.setName(mt.getName());
				t.setPercentageDone(mt.getPercentage());
				//t.setPhase(null);
				t.setReminderType(ReminderType.ALL_PROJECT_MEMBERS);
				t.setStartDate(mt.getStartDate());
				try{
					t.setUrgency(UrgencyLevel.valueOf(mt.getUrgency()));
				}catch(IllegalArgumentException e){
					t.setUrgency(UrgencyLevel.LOW);
				}
				for(String assignedMember : mt.getAssignedMembers()){
					MUser mu3 = this.getMUser(assignedMember);
					User u3 = userDAO.getUserByUsername(mu3.getUserName());
					if(u3==null)
						u3 = this.importUser(mu3.getUserId(), false);
					t.addAssignedPerson(u3);
					
				}
				result.addAddedList(mt.getTask_id() + " [task] "+mt.getName());
				m.addTask(t);
			}
			for(MPhase mp : mm.getPhases()){
				Phase p = new Phase();
				p.setComment(mp.getComments());
				p.setCreateDate(new Date());
				p.setModifyDate(new Date());
				p.setEndDate(mp.getEndDate());
				//p.setId(id)
				MUser mu2 = this.getMUser(mp.getOwner_id());
				if(mu2==null)
					continue; //Owner_id not valid in eureka1
				User u2 = userDAO.getUserByUsername(mu2.getUserName());
				if(u2==null)
					u2 = this.importUser(mu2.getUserId(), false);
				p.setManager(u2);
				p.setMilestone(m);
				p.setName(mp.getName());
				p.setStartDate(mp.getStartDate());
				
				for(MTask mt : mp.getTasks()){
					Task t = new Task();
					t.setComment(mt.getComments());
					t.setCompleteDate(mt.getDateCompleted());
					t.setCreateDate(new Date());
					//t.setDaysToRemind(daysEarlierToRemind)
					t.setEndDate(mt.getEndDate());
					//t.setId(id)
					MUser mu3 = this.getMUser(mt.getOwner_id());
					if(mu3==null)
						continue; //Owner_id not valid in eureka1
					User u3 = userDAO.getUserByUsername(mu3.getUserName());
					if(u3==null)
						u3 = this.importUser(mu3.getUserId(), false);
					t.setManager(u3);
					
					t.setMilestone(m);
					t.setModifyDate(t.getCreateDate());
					t.setName(mt.getName());
					t.setPercentageDone(mt.getPercentage());
					t.setPhase(p);
					t.setReminderType(ReminderType.ALL_PROJECT_MEMBERS);
					t.setStartDate(mt.getStartDate());
					try{
						t.setUrgency(UrgencyLevel.valueOf(mt.getUrgency()));
					}catch(IllegalArgumentException e){
						t.setUrgency(UrgencyLevel.LOW);
					}
						
					for(String assignedMember : mt.getAssignedMembers()){
						MUser mu4 = this.getMUser(assignedMember);
						User u4 = userDAO.getUserByUsername(mu4.getUserName());
						if(u4==null)
							u4 = this.importUser(mu4.getUserId(), false);
						t.addAssignedPerson(u4);
					}
					p.addTask(t);
					m.addTask(t); // add this because m.getTasks() include all tasks
					result.addAddedList(mt.getTask_id() + " [task] "+mt.getName());
				}
				
				result.addAddedList(mp.getPhase_id() + " [phase] "+mp.getName());
				m.addPhase(p);
			}
			
			result.addAddedList(mm.getMilestone_id() + " [milestone] "+mm.getName());
			schd.addMilestone(m);
		}
		schdDAO.saveSchedule(schd);  //it also cascade to save milestones, phases, and tasks
		
		return result;
	}
	@CommitAfter
	@Override
	public ResultSummary importFiles(String mProjId, Project proj) { //import Resources
		ResultSummary result = new ResultSummary();

		proj = assignModuleToProject(PredefinedNames.MODULE_RESOURCE, proj);
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		//get Root folder.
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.username AS creator, c.campaign_id "
				+ " FROM mbtaxonomynode f "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " JOIN mbcampaign c ON f.treeId=c.assetTreeId "
				+ " WHERE c.campaign_id = :projId "
				+ " AND f.parent_id IS NULL "
				)
				.setString("projId", mProjId);
		q.addScalar("tnode_id")
			.addScalar("name")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("creator")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("creator_id")
			;
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		for(Object[] obj : objList){
			ResourceFolder rootfolder = new ResourceFolder();
			String mFolderId = (String) obj[0]; 
			//rootfolder.setId(folderId);
			rootfolder.setName((String) obj[1]);
			rootfolder.setCdate(longObjToDate( obj[2]));
			User creator = userDAO.getUserByUsername((String) obj[3]);
			if(creator==null)
				creator = importUser(((String) obj[5]), false);
			rootfolder.setOwner(creator);
			rootfolder.setMdate(longObjToDate(obj[4]));
			rootfolder.setProj(proj);
			rootfolder.setType(ResourceType.Folder);
			//rootfolder.setParent(parent)
			//rootfolder.setEditor(editor)
			//rootfolder.setDes(des)
			
			
			Deque<ResourceFolder> queue = new LinkedList<ResourceFolder>();
			queue.push(rootfolder );
			Deque<String> idQueue = new LinkedList<String>();
			idQueue.push( mFolderId );
			
			
			while(!queue.isEmpty()){
				ResourceFolder fd = queue.poll();
				String mFdId = idQueue.poll();
				resrcDAO.addResource(fd);
				result.addAddedList(mFdId+" : [Folder] "+fd.getName());
				
				result = importFilesOfCurFolder(mFdId, fd, result);
				
				session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
				session2.beginTransaction();
				//import sub-folders
				q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.username AS creator FROM mbtaxonomynode f "
						+ " JOIN mbuser u ON f.creator_id=u.user_id "
						+ " WHERE f.parent_id = :parentId "
						)
						.setString("parentId", mFdId);
				q.addScalar("tnode_id")
					.addScalar("name")
					.addScalar("createDate", Hibernate.LONG)
					.addScalar("creator")
					.addScalar("lastModifiedDate", Hibernate.LONG)
					.addScalar("creator_id")
					;
				List<Object []> objList2 = q.list();
				session2.getTransaction().commit();
				for(Object[] obj2 : objList2){
					ResourceFolder childFolder = new ResourceFolder();
					String mFlderId = (String) obj2[0];
					//childFolder.setId((String) obj2[0]);
					childFolder.setName((String) obj2[1]);
					childFolder.setCdate(longObjToDate( obj2[2]));
					User creator2 = userDAO.getUserByUsername((String) obj2[3]);
					if(creator2==null)
						creator2 = importUser(((String) obj2[5]), false);
					childFolder.setOwner(creator2);
					childFolder.setMdate(longObjToDate(obj2[4]));
					childFolder.setProj(proj);
					childFolder.setType(ResourceType.Folder);
					if(!fd.equals(rootfolder)) //tweak to remove 1st root-level folder
						childFolder.setParent(fd);
					
					queue.add(childFolder);
					idQueue.add(mFlderId);
				}
			}
		}
		
		return result;
	}
	@CommitAfter
	private ResultSummary importFilesOfCurFolder(String folderId, ResourceFolder parent, ResultSummary result){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		MimetypesFileTypeMap mimetypeMap = new MimetypesFileTypeMap();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.userName AS creator," +
				" st.relativePath AS relativePath" +
				" FROM  mbtaxonomynodeobjectlink AS l, mbasset AS f   "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " JOIN mbstoreindex st ON f.asset_id=st.uuid "
				+ " WHERE f.asset_id = l.object_id "
				+ " AND l.tnode_id = :rTNodeId " 
				)
				.setString("rTNodeId", folderId);
		
		q.addScalar("asset_id")   
			.addScalar("creator")
			.addScalar("relativePath")
			.addScalar("filename") 
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("description")
			.addScalar("creator_id")
	//		.addScalar("modifier_id")	
	//		.addScalar("caption")	
	//		.addScalar("approved") 
			
			;
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			//NOTE File Path in eureka1
			//real file:      default_dir / relative_path / version_num1 / version_num2 / file_name
			//   eg (ignore the space):   z:/data/ntu-pm/file-repo /  0/0/1/FFFF9B45F0CD-115cc5794dc-XZ /  1/0  /  test-file.txt
			//in every file location, there is file 'revisioninfo.xml' about that file. 
			
			//because revisionControl in eureka1 is complicated to understand, 
			//we just attempt the get only version 1.0 here, and following versions in Project-Files:
			String versions[] = {"1\\0", "1\\2","1\\3","1\\4","1\\5","2\\0","2\\1","3\\0","4\\0","5\\0"};
			
			String creator_username = (String) obj[1];
			String creator_id = (String) obj[7];
			String relativePath = (String) obj[2];
			String fileName = (String) obj[3];
			Date cdate = longObjToDate(obj[4]);
			Date mdate = longObjToDate(obj[5]);
			String des = (String) obj[6];
			User creator = userDAO.getUserByUsername(creator_username);
			if(creator==null)
				creator = importUser(creator_id, false); 
	
			ResourceFile rFile = null;
			
			for(String ver : versions){
				String dirPath = REPO_DEAFAULT_DIR + relativePath + "\\"+ver+"\\"  ;
				File infoFile = new File(dirPath , "revisioninfo.xml");
				if(infoFile.exists()){
					//read filename inside revisioninfo.xml
					String versionFileName = null;
					String dateStr = null;
					Date date = null;
					String log = null;
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					try {
						docBuilder = docBuilderFactory.newDocumentBuilder();
						Document doc = docBuilder.parse(infoFile);
						doc.getDocumentElement().normalize();
					
						NodeList oNodeList = doc.getElementsByTagName("FileName");
						Element oElmt = (Element) oNodeList.item(0);
						NodeList textOList = oElmt.getChildNodes();
						versionFileName = ((Node) textOList.item(0)).getNodeValue();
						oNodeList = doc.getElementsByTagName("Date");
						oElmt = (Element) oNodeList.item(0);
						textOList = oElmt.getChildNodes();
						dateStr = ((Node) textOList.item(0)).getNodeValue();
						if(dateStr!=null && !dateStr.isEmpty())
							date = longObjToDate(Long.parseLong(dateStr));
						oNodeList = doc.getElementsByTagName("Log");
						oElmt = (Element) oNodeList.item(0);
						textOList = oElmt.getChildNodes();
						if(textOList.item(0)!=null)
							log = ((Node) textOList.item(0)).getNodeValue();
						
					} catch (ParserConfigurationException e1) {
						e1.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					File file = new File(dirPath, versionFileName);
					if(file.exists()){
						
						if(rFile==null){
							rFile = new ResourceFile();
							rFile.setCdate(cdate);
							rFile.setDes(des);
							//rFile.setId(id)
							//rFile.setLockedBy(lockedBy)
							rFile.setMdate(mdate);
							rFile.setName(fileName); 
							rFile.setOwner(creator);
							rFile.setParent(parent);
							rFile.setProj(parent.getProj());
							rFile.setType(ResourceType.File);
						}
						
						//copy file
						String rootDir = Config.getString(Config.VIRTUAL_DRIVE) + "/" + parent.getProj().getId() + "/"+PredefinedNames.MODULE_RESOURCE;
						File rootFolder = new File(rootDir);
						if (!rootFolder.exists() || !rootFolder.isDirectory()) {
							rootFolder.mkdirs();
						}
						File dirFile = new File(rootFolder + "/" + rFile.getId());
						if (!dirFile.exists() || !dirFile.isDirectory()) {
							dirFile.mkdirs();
						}
						String prefix = Util.formatDateTime(new Date(), "yyyyMMdd-HHmmss")+"_"+creator.getId();
						File toFile = new File (dirFile + "/" + prefix +"_"+ versionFileName.replace(" ", "_"));
						try{
							InputStream in = new FileInputStream(file);
							OutputStream out = new FileOutputStream(toFile);
							byte[] buf = new byte[1024];
							int len;
							while((len=in.read(buf))>0){
								out.write(buf, 0, len);
							}
							in.close();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						String path = toFile.getAbsolutePath().replace(Config.getString(Config.VIRTUAL_DRIVE), ""); 
						path = path.replace("\\", "/");
						
						ResourceFileVersion rfv = new ResourceFileVersion();
						rfv.setCdate(date);
						rfv.setName(versionFileName);
						rfv.setCmmt(log); 
						rfv.setContentType(mimetypeMap.getContentType(fileName));
						//rfv.setId(id)
						//rfv.setNumDownload(numDownload)
						rfv.setOwner(creator); //assume same as ResourceFile, because '<Author>' in revisioninfo.xml is empty
						rfv.setPath(path);
						rfv.setRfile(rFile);
						rfv.setSize(toFile.length());
						rfv.setVersion(rFile.getLatestVersion()+1);
						
						rFile.addFileVersion(rfv);
						
						result.addAddedList(file.getAbsolutePath() + " ...to... " +toFile.getAbsolutePath());
						
					}
				}
			}
			if(rFile!=null)
				resrcDAO.addResource(rFile);
			
		}
		return result;
		
	}
	
	@CommitAfter
	@Override
	public ResultSummary importDiscussionBoards(String mProjId, Project proj) {
		ResultSummary result = new ResultSummary();

		proj = assignModuleToProject(PredefinedNames.MODULE_FORUM, proj);
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.userName AS creator FROM mbforum f "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " WHERE f.link_id = :projId ")
				.setString("projId", mProjId);
		q.addScalar("forum_id")
			.addScalar("name")
			.addScalar("description")
			.addScalar("creator")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("creator_id")
			//	.addScalar("accessRestricted", Hibernate.BOOLEAN)
			//	.addScalar("systemForum", Hibernate.BOOLEAN)
			;
		/*
		 forum_id, link_id, name, description, creator_id, lastModifiedDate, createDate, accessRestricted, systemForum
		  */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			String forumId = (String) obj[0];
			String name = (String) obj[1];
			String description = (String) obj[2];
			User creator = userDAO.getUserByUsername((String) obj[3]);
			if(creator==null)
				creator = importUser(((String) obj[6]), false); 
			//		Date lastModifiedDate = longObjToDate( obj[4]);
			Date createDate = longObjToDate( obj[5]);
			
			Forum f = new Forum();
			f.setName(name);
			f.setDescription(description);
			f.setProject(proj);
			f.setCreator(creator);
			f.setCreateDate(createDate);
			List<MThread> mThs = getThreadByForumIdAndParentId(forumId, null); //use 'null' to get only rootThreads
			for(MThread mTh : mThs){
				Thread th = mThreadToForumThread(mTh, f);
				f.addThread(th);
			}
			
			//copy reflections is done when add Thread
			//copy attached files is done when add Thread
			
			result.addAddedList(forumId + " : " +f.getName());
			
			forumDAO.saveForum(f);
		}
		
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importWebBlogs(String mProjId, Project proj) {
		ResultSummary result = new ResultSummary();
		
		//import blog
		proj = assignModuleToProject(PredefinedNames.MODULE_BLOG, proj);
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT b.*, u.userName AS creator FROM mbweblog b "
				+ " JOIN mbuser u ON b.creator_id=u.user_id "
				+ " WHERE b.link_id = :projId "
				+ " AND b.parent_id IS NULL " //it is 'comment' if parent_id is not null
				)
				.setString("projId", mProjId);
		q.addScalar("weblog_id")
			.addScalar("subject")
			.addScalar("description")
			.addScalar("creator")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("accessRestricted")
			.addScalar("status")
			.addScalar("creator_id")
			;
		/*
		 weblog_id, link_id, parent_id, subject, description, lastModifiedDate, 
		 creator_id, createDate, accessRestricted, status, subject_draft, description_draft
		  */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			String wblogId = (String) obj[0];
			String subject = (String) obj[1];
			String description = (String) obj[2];
			User creator = userDAO.getUserByUsername((String) obj[3]);
			if(creator==null)
				creator = importUser(((String) obj[8]), false); 
			Date lastModifiedDate = longObjToDate( obj[4]);
			Date createDate = longObjToDate( obj[5]);
			Boolean accessRestricted = (Boolean) obj[6];
			String status = (String) obj[7];
			
			Blog b = new Blog();
			b.setSubject(subject);
			b.setContent(Util.filterOutRestrictedHtmlTags(description));
			b.setAuthor(creator);
			b.setCdate(createDate);
			b.setMdate(lastModifiedDate);
			b.setProject(proj);
			b.setShared(! accessRestricted);
			
			BlogStatus blogStatus = BlogStatus.PUBLISHED;
			if("drafting".equals(status))
				blogStatus = BlogStatus.DRAFT;
			b.setStatus(blogStatus);
			//b.setId(id)
			//b.setIp(ip)
			//b.setRemarks(remarks)
			//b.setTags(tags)
			
			importWebBlogComments(wblogId, b);
			
			importReflections(LogEntry.TYPE_BLOG_REFLECTION, wblogId, b.getId(), proj);
			importBlogAttachedFile(wblogId, b);
			
			result.addAddedList(wblogId + " : " +b.getSubject());
			
			blogDAO.addBlog(b);
		}
		
		
		return result;
	}
	
	private void importWebBlogComments(String wblogId, Blog blog){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT b.*, u.userName AS creator FROM mbweblog b "
				+ " JOIN mbuser u ON b.creator_id=u.user_id "
				+ " WHERE b.parent_id = :wblogId "
				)
				.setString("wblogId", wblogId);
		q.addScalar("weblog_id")
			.addScalar("description")
			.addScalar("creator")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("creator_id")
			;
		/*
		 weblog_id, link_id, parent_id, subject, description, lastModifiedDate, 
		 creator_id, createDate, accessRestricted, status, subject_draft, description_draft
		  */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
		//	String wbCmmtId = (String) obj[0];
			String description = (String) obj[1];
			User creator = userDAO.getUserByUsername((String) obj[2]);
			if(creator==null)
				creator = importUser(((String) obj[5]), false); 
			Date lastModifiedDate = longObjToDate( obj[3]);
			Date createDate = longObjToDate( obj[4]);
			
			BlogComment c = new BlogComment();
			c.setAuthor(creator);
			c.setBlog(blog);
			c.setCdate(createDate);
			c.setContent(Util.filterOutRestrictedHtmlTags(description));
			c.setMdate(lastModifiedDate);
			//c.setDisabled(disabled)
			//c.setId(id)
			//c.setIp(ip)
			//c.setRemarks(remarks)
			//c.setStatus(status)
			//c.setSubject(subject)
			
			blog.addComment(c);
			blogDAO.updateComment(c);
		}
	}
	private List<BlogFile> importBlogAttachedFile(String wblogId, Blog blog){
		List<BlogFile> attList = new ArrayList<BlogFile>();
		
		MimetypesFileTypeMap mimetypeMap = new MimetypesFileTypeMap();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.userName AS creator," +
				" st.relativePath AS relativePath" +
				" FROM  mbassetrepositoryassetlink AS repo, mbasset AS f   "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " JOIN mbstoreindex st ON f.asset_id=st.uuid "
				+ " WHERE f.asset_id = repo.asset_id "
				+ " AND repo.url LIKE :rURL "
				)
				.setString("rURL", REPO_MAP_DEFAULT_URL+wblogId+"%");
		
		//asset_id, creator_id, createDate, modifier_id, lastModifiedDate,
		//caption, description, filename, approved
			
		q.addScalar("asset_id")   
			.addScalar("creator")
			.addScalar("relativePath")
			.addScalar("filename") 
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("modifier_id")	
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("caption")	
			.addScalar("description")
			.addScalar("creator_id")
			//.addScalar("approved") 
			
			;
	
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			
			//NOTE File Path in eureka1
			//real file:      default_dir / relative_path / version_num1 / version_num2 / file_name
			//   eg (ignore the space):   z:/data/ntu-pm/file-repo /  0/0/1/FFFF9B45F0CD-115cc5794dc-XZ /  1/0  /  test-file.txt
			//in every file location, there is file 'revisioninfo.xml' about that file. 
			
			String creator_username = (String) obj[1];
			String creator_id = (String) obj[9];
			String relativePath = (String) obj[2];
			String fileName = (String) obj[3];
			User creator = userDAO.getUserByUsername(creator_username);
			if(creator==null)
				creator = importUser(creator_id, false); 
	
					
			String filePath = REPO_DEAFAULT_DIR + relativePath + "\\1\\0\\" + fileName  ;
			File file = new File(filePath);
			if(file.exists()){
				
				BlogFile att = new BlogFile();
				
				//att.setAliasName(null) 
				att.setFileName(fileName);
				att.setId(Util.generateUUID());
				att.setCreator(creator);
				String path = attFileManager.saveAttachedFile(file, att.getId(),
						PredefinedNames.MODULE_BLOG, blog.getProject().getId());
				
				att.setPath(path);
				att.setSize(file.length());
				att.setContentType(mimetypeMap.getContentType(fileName));
				att.setBlog(blog);
				blog.addFile(att);
				
				attList.add(att);
			}
		}
		return attList;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importLinks(String mProjId, Project proj) {
		ResultSummary result = new ResultSummary();
		
		proj = assignModuleToProject(PredefinedNames.MODULE_RESOURCE, proj);
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.username AS creator "
				+ " FROM mbwebsitefolder f "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " WHERE f.link_id = :projId "
				)
				.setString("projId", mProjId);
		q.addScalar("folder_id")
			.addScalar("name")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("creator")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("creator_id")
			;
		/*
		 folder_id, link_id, creator_id, name, createDate, lastModifiedDate
		 */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		for(Object[] obj : objList){
			ResourceFolder rootfolder = new ResourceFolder();
			String mFolderId = (String) obj[0]; 
			//rootfolder.setId(folderId);
			rootfolder.setName((String) obj[1]);
			rootfolder.setCdate(longObjToDate( obj[2]));
			User creator = userDAO.getUserByUsername((String) obj[3]);
			if(creator==null)
				creator = importUser(((String) obj[5]), false);
			rootfolder.setOwner(creator);
			rootfolder.setMdate(longObjToDate(obj[4]));
			rootfolder.setProj(proj);
			rootfolder.setType(ResourceType.Folder);
			//rootfolder.setParent(parent)
			//rootfolder.setEditor(editor)
			//rootfolder.setDes(des)
			
			resrcDAO.addResource(rootfolder);
			result.addAddedList(mFolderId+" : [Folder] "+rootfolder.getName());
				
			result = importLinksOfCurFolder(mFolderId, rootfolder, result);
				
				
		}
		return result;
	}
	private ResultSummary importLinksOfCurFolder(String mFolderId, ResourceFolder rFolder, ResultSummary result){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT l.*, u.username AS creator "
				+ " FROM mbwebsitelink l "
				+ " JOIN mbuser u ON l.creator_id=u.user_id "
				+ " WHERE l.link_id = :rFolderId "
				)
				.setString("rFolderId", mFolderId);
		q.addScalar("website_id")
			.addScalar("title")
			.addScalar("url")
			.addScalar("creator")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("creator_id")
			;
		/*
		website_id, link_id, creator_id, url, title, createDate, lastModifiedDate
		 */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		for(Object[] obj : objList){
			ResourceLink rLink = new ResourceLink();
			String website_id = (String) obj[0];
			//rLink.setId(id)
			rLink.setName((String) obj[1]);
			rLink.setUrl((String) obj[2]);
			User creator = userDAO.getUserByUsername((String) obj[3]);
			if(creator==null)
				creator = importUser(((String) obj[6]), false);
			rLink.setOwner(creator);
			rLink.setCdate(longObjToDate( obj[4]));
			rLink.setMdate(longObjToDate(obj[5]));
			rLink.setProj(rFolder.getProj());
			rLink.setType(ResourceType.Link);
			rLink.setParent(rFolder);
			
			resrcDAO.addResource(rLink);
			result.addAddedList(website_id+" : [Link] "+rLink.getUrl());
		}
		return result;
	}
	
	
	@CommitAfter
	@Override
	public ResultSummary importBudgets(String mProjId, Project proj) {
		// not implemented this because there is almost no data in eUreka1
		ResultSummary result = new ResultSummary();
		return result;
	}
	
	@CommitAfter
	@Override
	public ResultSummary importAssesments(String mProjId, Project proj) {
		// not implemented this because there is almost no data in eUreka1
		ResultSummary result = new ResultSummary();
		return result;
	}
	
	
	private MProject getMProjectById(String mProjId){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT c.* " 
				+ " FROM mbcampaign c " 
				+ " WHERE campaign_id = :projId ")
				.setString("projId", mProjId);
		
		q.addScalar("campaign_id")
		.addScalar("name")
		.addScalar("description")
		.addScalar("startDate", Hibernate.LONG)  //probably need to set to Hibernate.TIMESTAMP
		.addScalar("endDate", Hibernate.LONG) 
		.addScalar("creator_id")
		.addScalar("createDate", Hibernate.LONG)
		.addScalar("privateCampaign")
		.addScalar("status")
		.addScalar("statusSetDateMillis", Hibernate.LONG)
		.addScalar("school_id")
		.addScalar("course_id")
		.addScalar("project_no")
		.addScalar("academic_year")
		.addScalar("campaign_type")
		.addScalar("modifier_id")
		.addScalar("lastModifiedDate", Hibernate.LONG)
		.addScalar("cao")
		;
		
		/*	campaign_id, name, description, startDate, endDate, creator_id, 
		createDate, privateCampaign, logo, logoThumbnail, logoThumbnailWidth, 
		logoThumbnailHeight, link_id, closeDateMillis, assetTreeId, status, statusSetDateMillis,
		school_id, course_id, project_no, academic_year, isp, exam_date, exam_venue, semester_off, 
		campaign_type, modifier_id, lastModifiedDate, contentLastModifiedDate, new_converted_campaign_id,
		mark, cao
		 */	
		
		List<Object[]> objList = q.list(); 
		if(objList==null || objList.isEmpty())
			return null;
		//if(objList.size()>1) //expect return only 1 row
		//    throw new RuntimeException("found more than 1 record of Project for mProjId="+mProjId);
		Object[] obj = objList.get(0); 
		MProject p = new MProject();
		p.setId((String) obj[0]);
		p.setName((String) obj[1]);
		p.setDesc((String) obj[2]);
		p.setStartDate(longObjToDate(obj[3]));
		p.setEndDate(longObjToDate( obj[4]));
		p.setCreatorId((String) obj[5]);
		p.setCreateDate(longObjToDate( obj[6]));
		p.setPrivateProj((Boolean) obj[7]);
		p.setStatus((String) obj[8]);
		p.setStatusChangedDate(longObjToDate( obj[9]));       
		p.setSchoolId((String) obj[10]);
		p.setCourseId((String) obj[11]);
		p.setProjNo((String) obj[12]);
		p.setAcademicYear((String) obj[13]);
		p.setProjType((String) obj[14]);
		p.setModifierId((String) obj[15]);
		p.setLastModifiedDate(longObjToDate( obj[16]));
		p.setCao((Boolean) obj[17]);
		
		session2.getTransaction().commit();
		return p;
	}
	private List<MMember> getMembersByProj(String mProjId){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT m.*, u.userName AS username FROM mbmember m "
					+ " JOIN mbuser u ON m.principal_id = u.user_id " 
					+ " WHERE m.roll_id = :projId ")
					.setString("projId", mProjId);
		q.addScalar("username")
			.addScalar("manager", Hibernate.BOOLEAN)
			.addScalar("principal_id")
			;
		
		
		List<Object []> objList = q.list();
		List<MMember> mMembList = new ArrayList<MMember>();
		
		for(Object[] obj : objList){
			MMember m = new MMember();	
			m.setProjId(mProjId);
			m.setUsername((String) obj[0]);
			m.setManager((Boolean) obj[1]);
			m.setUserId((String) obj[2]);
			
			mMembList.add(m);
		}
		
		session2.getTransaction().commit();
		
		return mMembList;
	}
	private List<MUser> getAllUsers(int firstResult, int maxResult){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT u.user_id AS user_id FROM mbuser u "); //...WHERE u.deleted=false
		q.addScalar("user_id")
			;
		q.setFirstResult(firstResult);
		q.setMaxResults(maxResult);
		
		List<Object []> objList = q.list();
		List<MUser> mUserList = new ArrayList<MUser>();
		
		for(Object[] obj : objList){
			MUser mu = getMUser((String) obj[0]);
			mUserList.add(mu);
		}
		
		session2.getTransaction().commit();
		
		return mUserList;
	}
	
	@Override
	public MUser getMUser(String userId){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT * FROM mbuser u " +
				" WHERE user_id=:userId"); //...WHERE u.deleted=false
		q.setString("userId", userId);
		q.addScalar("external_person_key")
			.addScalar("userName")
			.addScalar("password")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("email")
			.addScalar("firstName")
			.addScalar("lastName")
			.addScalar("title")
			.addScalar("department")
			.addScalar("job_title")
			.addScalar("extUser")
			.addScalar("disabled" ) 
			.addScalar("contactNumber")
			.addScalar("institution_role")
			.addScalar("lastModifiedDate", Hibernate.LONG)
			;
		Object[] obj = (Object[]) q.uniqueResult();
		if(obj==null)
			return null;
		
		MUser mu = new MUser();	
		mu.setUserId(userId);
		mu.setExternal_person_key((String) obj[0]);
		mu.setUserName((String) obj[1]);
		mu.setPassword((String) obj[2]);
		mu.setCreateDate(longObjToDate( obj[3]));
		mu.setEmail((String) obj[4]);
		mu.setFirstName((String) obj[5]);
		mu.setLastName((String) obj[6]);
		mu.setTitle((String) obj[7]);
		mu.setDepartment((String) obj[8]);
		mu.setJob_title((String) obj[9]);
		mu.setExternalUser((Boolean) obj[10]);
		mu.setDisabled((Boolean) obj[11]);
		mu.setContactNumber((String) obj[12]);
		mu.setInstitution_role((String) obj[13]);
		mu.setLastModifiedDate(longObjToDate( obj[14]));
		
		if(mu.getUserName().isEmpty()){
			mu.setUserName(mu.getExternal_person_key());
			if("0".equals(mu.getExternal_person_key())){ //hard-code special case, 'System' and 'Anonymous'
				mu.setUserName(userId);
			}
		}
		
		return mu;
			
	}
	
	@Override
	public User importUser( String userId, boolean replaceIfExist){
		MUser mu = this.getMUser(userId);
			
		if(mu==null)
			return null;
		
		User u = userDAO.getUserByUsername(mu.getUserName());
		if(u==null){ //u not exist, simple add new
			u = new User();
		}
		else{
			if(! replaceIfExist){
				return u;
				//continue;
			}
		}
		School defaultSchool = null;
		SysRole defaultSysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_USER);
		SysRole extUserSysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_EXTERNAL_USER);
		
		u.setCreateDate(mu.getCreateDate());
		u.setEmail(mu.getEmail());
		u.setEnabled(!mu.isDisabled());
		u.setExternalKey(mu.getExternal_person_key());
		u.setFirstName(mu.getFirstName());
		//u.setId(id);
		//u.setIp(ip);
		u.setTitle(mu.getTitle());
		u.setJobTitle(mu.getJob_title());
		u.setLastName(mu.getLastName());
		u.setUsername(mu.getUserName());
		
		u.setModifyDate(mu.getLastModifiedDate());
		//u.setMphone(mphone);
		//u.setOrganization(organization);
		u.setPassword(Util.generateHashValue(Util.nvl(mu.getPassword())));
		u.setPhone(mu.getContactNumber());
		//u.setProjects(projects);
		String remarks = "Migrated from eUreka1 on "+ Util.formatDateTime2(new Date());
		u.setRemarks(remarks);
		
		//set School
		School s = schoolDAO.getFirstSchoolByDescription(mu.getDepartment());
		if(s==null){
			s = defaultSchool;
		}
		u.setSchool(s);
		
		//set sysRole
		SysRole r = null;
		if(mu.isExternalUser())
			r = extUserSysRole;
		else
			r = sysRoleDAO.getSysRoleByName(mu.getInstitution_role());
		if(r==null){
			r = defaultSysRole;
		}
		u.setSysRole(r);
		
		userDAO.save(u);
		
		
		return u;
	}
	
	@Override
	@CommitAfter
	public School importSchool( String school_id, boolean replaceIfExist){
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT * FROM ntuschools s "
				+" WHERE school_id = :schoolId")
				.setString("schoolId", school_id)
				;
		q.addScalar("school_id")
			.addScalar("name")
			.addScalar("description");
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		School s = null;
		for(Object[] obj : objList){
			String schoolId = (String) obj[0];
			s = schoolDAO.getSchoolByName(schoolId);
			if(s==null){ //school not exist, simply add new
				s = new School();
			}
			else{
				if(replaceIfExist){
				}else{	
					continue;
				}
			}
			s.setName(schoolId);
			s.setDes((String) obj[1]);
			schoolDAO.save(s);
		}
		
		
		return s;
	}
	
	private List<MTask> getTasksByMileStone(String milestoneId){
		List<MTask> tList = new ArrayList<MTask>();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT t.* FROM mbtask t "
				+ " WHERE t.milestone_id = :milestoneId "
				+ " AND t.phase = :false "
				+ " AND t.phase_link_id IS NULL ")
				.setString("milestoneId", milestoneId)
				.setBoolean("false", false)
				;
		q.addScalar("task_id")
			.addScalar("milestone_id")
			.addScalar("owner_id")
			.addScalar("name")
			.addScalar("comments")
			.addScalar("startDate", Hibernate.LONG)
			.addScalar("endDate", Hibernate.LONG)
			.addScalar("dateCompleted", Hibernate.LONG)
			.addScalar("urgency")
			.addScalar("notifyOwnerOfEndDate")
			.addScalar("percentage", Hibernate.INTEGER)
			;
		/*
		 task_id, milestone_id, owner_id, name, comments, startDate, endDate, dateCompleted,
		 urgency, notifyOwnerOfEndDate, ownerNotifiedOfEndDate, phase, phase_link_id, percentage
		 */
		List<Object []> objList = q.list();
		
		for(Object[] obj : objList){
			MTask t = new MTask();
			t.setTask_id((String) obj[0]);
			t.setMilestone_id((String) obj[1]);
			t.setOwner_id((String) obj[2]);
			t.setName((String) obj[3]);
			t.setComments((String) obj[4]);
			t.setStartDate(longObjToDate(obj[5]));
			t.setEndDate(longObjToDate(obj[6]));
			if(t.getStartDate()==null ){ 
				t.setStartDate(t.getEndDate());
			}
			t.setDateCompleted(longObjToDate(obj[7]));
			t.setUrgency(((String) obj[8]).toUpperCase());
			t.setNotifyOwnerOfEndDate((Boolean) obj[9]);
			t.setPercentage((Integer) obj[10]);
		
			SQLQuery q2 = (SQLQuery) session2.createSQLQuery("SELECT t.member_id AS member FROM mbtaskassignedidlink t "
					+ " WHERE t.object_id = :taskId ")
					.setString("taskId", t.getTask_id())
					;
			q2.addScalar("member");
			List<Object> assignedList = q2.list();
			for(Object assigned : assignedList){
				t.addAssignedMember((String) assigned);
			}
			
			
			tList.add(t);
		}
		session2.getTransaction().commit();
		
		return tList;
	}
	private List<MTask> getTasksByPhaseId(String phaseId){
		List<MTask> tList = new ArrayList<MTask>();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT t.* FROM mbtask t "
				+ " WHERE t.phase = :false "
				+ " AND t.phase_link_id = :phaseId ")
				.setString("phaseId", phaseId)
				.setBoolean("false", false);
		q.addScalar("task_id")
			.addScalar("milestone_id")
			.addScalar("owner_id")
			.addScalar("name")
			.addScalar("comments")
			.addScalar("startDate", Hibernate.LONG)
			.addScalar("endDate", Hibernate.LONG)
			.addScalar("dateCompleted", Hibernate.LONG)
			.addScalar("urgency")
			.addScalar("notifyOwnerOfEndDate")
			.addScalar("percentage", Hibernate.INTEGER)
			.addScalar("phase_link_id")
			;
		/*
		 task_id, milestone_id, owner_id, name, comments, startDate, endDate, dateCompleted,
		 urgency, notifyOwnerOfEndDate, ownerNotifiedOfEndDate, phase, phase_link_id, percentage
		 */
		List<Object []> objList = q.list();
		
		for(Object[] obj : objList){
			MTask t = new MTask();
			t.setTask_id((String) obj[0]);
			t.setMilestone_id((String) obj[1]);
			t.setOwner_id((String) obj[2]);
			t.setName((String) obj[3]);
			t.setComments((String) obj[4]);
			t.setStartDate(longObjToDate(obj[5]));
			t.setEndDate(longObjToDate(obj[6]));
			if(t.getStartDate()==null ){ 
				t.setStartDate(t.getEndDate());
			}
			t.setDateCompleted(longObjToDate(obj[7]));
			t.setUrgency(((String) obj[8]).toUpperCase());
			t.setNotifyOwnerOfEndDate((Boolean) obj[9]);
			t.setPercentage((Integer) obj[10]);
			t.setPhase_link_id((String) obj[11]);
			
			SQLQuery q2 = (SQLQuery) session2.createSQLQuery("SELECT t.member_id AS member FROM mbtaskassignedidlink t "
					+ " WHERE t.object_id = :taskId ")
					.setString("taskId", t.getTask_id())
					;
			q2.addScalar("member");
			List<Object> assignedList = q2.list();
			for(Object assigned : assignedList){
				t.addAssignedMember((String) assigned);
			}
			
			tList.add(t);
		}
		session2.getTransaction().commit();
		return tList;
	}
	private List<MPhase> getPhasesByMileStone(String milestoneId){
		List<MPhase> pList = new ArrayList<MPhase>();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT t.* FROM mbtask t "
				+ " WHERE t.milestone_id = :milestoneId "
				+ " AND t.phase = :true ")
				.setString("milestoneId", milestoneId)
				.setBoolean("true", true);
		q.addScalar("task_id")
			.addScalar("milestone_id")
			.addScalar("owner_id")
			.addScalar("name")
			.addScalar("comments")
			.addScalar("startDate", Hibernate.LONG)
			.addScalar("endDate", Hibernate.LONG)
			.addScalar("dateCompleted", Hibernate.LONG)
			.addScalar("urgency")
			.addScalar("percentage", Hibernate.INTEGER)
			;
		/*
		 task_id, milestone_id, owner_id, name, comments, startDate, endDate, dateCompleted,
		 urgency, notifyOwnerOfEndDate, ownerNotifiedOfEndDate, phase, phase_link_id, percentage
		 */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			MPhase p = new MPhase();
			p.setPhase_id((String) obj[0]);
			p.setMilestone_id((String) obj[1]);
			p.setOwner_id((String) obj[2]);
			p.setName((String) obj[3]);
			p.setComments((String) obj[4]);
			p.setStartDate(longObjToDate(obj[5]));
			p.setEndDate(longObjToDate(obj[6]));
			if(p.getStartDate()==null ){ 
				p.setStartDate(p.getEndDate());
			}
			p.setDateCompleted(longObjToDate(obj[7]));
			p.setUrgency(((String) obj[8]).toUpperCase());
			p.setPercentage((Integer) obj[9]);
			
			p.setTasks(getTasksByPhaseId(p.getPhase_id()));
			
			pList.add(p);
		}
		
		return pList;
	}
	private List<MMilestone> getMilestoneByProj(String mProjId){
		List<MMilestone> mList = new ArrayList<MMilestone>();
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT m.* FROM mbmilestone m "
				+ " WHERE campaign_id = :projId ")
				.setString("projId", mProjId);
		q.addScalar("milestone_id")
			.addScalar("owner_id")
			.addScalar("name")
			.addScalar("comments")
			.addScalar("deadline", Hibernate.LONG)
			.addScalar("urgency")
			;
		/*
		 	milestone_id, parent_id, campaign_id, owner_id, name, comments, deadline, dateCompleted, urgency
		 */
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			MMilestone m = new MMilestone();
			m.setMilestone_id((String) obj[0]);
			m.setOwner((String) obj[1]);
			m.setName((String) obj[2]);
			m.setComments((String) obj[3]);
			m.setDeadline(longObjToDate(obj[4]));
			if(m.getDeadline()==null && "u1".equals(m.getOwner())){ 
				//special case, there is negative value in database, (it is test data, can be safely ignored)
				continue;
			}
			m.setUrgency(((String) obj[5]).toUpperCase());
			m.setTasks(this.getTasksByMileStone(m.getMilestone_id()));
			m.setPhases(this.getPhasesByMileStone(m.getMilestone_id()));
			mList.add(m);
		}
		
		return mList;
	}
	
	/*
	 * set parentId to NULL to get only root threads
	 */
	private List<MThread> getThreadByForumIdAndParentId(String forumId, String parentId){
		List<MThread> thList = new ArrayList<MThread>();
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT th.*, u.userName AS creator FROM mbforummessage th "
				+ " JOIN mbuser u ON th.creator_id=u.user_id "
				+ " WHERE th.forum_id = :forumId "
				+ (parentId==null? " AND th.parent_id is NULL " : "AND th.parent_id=:parentId")
				)
				.setString("forumId", forumId);
		
		if(parentId!=null)
			q.setString("parentId", parentId);
		q.addScalar("message_id")
			.addScalar("creator")
			.addScalar("subject")
			.addScalar("body")
			.addScalar("views", Hibernate.INTEGER)
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("lastReplyDate", Hibernate.LONG)
			.addScalar("category")
			.addScalar("creator_id")
		;
	
		/* message_id, forum_id, parent_id, creator_id, subject, body, views, 
		lastModifiedDate, createDate, lastReplyDate, category
		*/
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			MThread t = new MThread();
			t.setMessage_id((String) obj[0]);
			t.setForum_id(forumId);
			t.setParent_id(parentId);
			t.setCreatorUsername((String) obj[1]);
			t.setCreatorId((String) obj[9]);
			t.setSubject((String) obj[2]);
			t.setBody((String) obj[3]);
			t.setViews((Integer) obj[4]);
			t.setLastModifiedDate(longObjToDate((Long) obj[5]));
			t.setCreateDate(longObjToDate((Long) obj[6]));
			t.setLastReplyDate(longObjToDate((Long) obj[7]));
			t.setCategory((String) obj[8]);
			t.setChildren(getThreadByForumIdAndParentId(forumId, t.getMessage_id()));
			
			//adding attachFile , reflections is done in mThreadToForumThread... 
			
			thList.add(t);
		}
		
		return thList;
	}
	private Thread mThreadToForumThread(MThread mTh, Forum forum){
		Thread th = new Thread();
	
		User creator = userDAO.getUserByUsername(mTh.getCreatorUsername());
		if(creator==null){
			creator = importUser(mTh.getCreatorId(), false); 
		}
		th.setAuthor(creator);
		th.setForum(forum);
		th.setCreateDate(mTh.getCreateDate());
		//th.setId(id);
		th.setMessage(mTh.getBody());
		th.setModifyDate(mTh.getLastModifiedDate());
		th.setName(mTh.getSubject());
		//th.setThreadUsers(threadUsers)
		th.setType(this.threadCategoryPictureToThreadType(mTh.getCategory()));
		
		importReflections(LogEntry.TYPE_FORUM_REFLECTION, mTh.getMessage_id(), Long.toString(th.getId()), th.getForum().getProject());
		th.setAttachedFiles(importForumAttachedFile(mTh.getMessage_id(), th, null));
		
		for(MThread mThCh : mTh.getChildren()){
			ThreadReply thCh = mThreadToForumThreadReply(mThCh, th, null);
			th.addReplies(thCh);
		}
		
		forum.addThread(th);
		
		
		return th;
	}
	private ThreadReply mThreadToForumThreadReply(MThread mTh, Thread rootTh, ThreadReply parent){
		ThreadReply thR = new ThreadReply();

		//thR.setAnonymous(anonymous);
		User creator = userDAO.getUserByUsername(mTh.getCreatorUsername());
		if(creator==null)
			creator = importUser(mTh.getCreatorId(), false); 
		thR.setAuthor(creator);
		thR.setParent(parent);
		thR.setThread(rootTh);
		thR.setCreateDate(mTh.getCreateDate());
		//thR.setId(id);
		thR.setMessage(mTh.getBody());
		thR.setModifyDate(mTh.getLastModifiedDate());
		thR.setName(mTh.getSubject());
		//thR.setThreadUsers(threadUsers)
		thR.setType(this.threadCategoryPictureToThreadType(mTh.getCategory()));
		
		importReflections(LogEntry.TYPE_FORUM_REFLECTION, mTh.getMessage_id(), Long.toString(rootTh.getId()), rootTh.getForum().getProject());
		importForumAttachedFile(mTh.getMessage_id(), rootTh, thR);
		
		
		for(MThread mThCh : mTh.getChildren()){
			ThreadReply thR1 = mThreadToForumThreadReply(mThCh, rootTh, thR);
			thR.addChildren(thR1);
		}
		
		
		
		return thR;
	}
	
	/**
	 * 
	 * @param mId
	 * @param th
	 * @param thR , if not null, attachments belongs to this ThreadReply, and param 'th' is rootThread. 
	 *           Otherwise, if null, attachements belong to rootThread
	 * @return
	 */
	private List<ForumAttachedFile> importForumAttachedFile(String mId, Thread th, ThreadReply thR){
		List<ForumAttachedFile> attList = new ArrayList<ForumAttachedFile>();
		
		MimetypesFileTypeMap mimetypeMap = new MimetypesFileTypeMap();
		
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.*, u.userName AS creator," +
				" st.relativePath AS relativePath" +
				" FROM  mbassetrepositoryassetlink AS repo, mbasset AS f   "
				+ " JOIN mbuser u ON f.creator_id=u.user_id "
				+ " JOIN mbstoreindex st ON f.asset_id=st.uuid "
				+ " WHERE f.asset_id = repo.asset_id "
				+ " AND repo.url LIKE :rURL "
				)
				.setString("rURL", REPO_MAP_DEFAULT_URL+mId+"%");
		
		//asset_id, creator_id, createDate, modifier_id, lastModifiedDate,
		//caption, description, filename, approved
			
		q.addScalar("asset_id")   
			.addScalar("creator")
			.addScalar("relativePath")
			.addScalar("filename") 
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("modifier_id")	
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("caption")	
			.addScalar("description")
			.addScalar("approved") 
			.addScalar("creator_id")
			
			;
	
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			
			//NOTE File Path in eureka1
			//real file:      default_dir / relative_path / version_num1 / version_num2 / file_name
			//   eg (ignore the space):   z:/data/ntu-pm/file-repo /  0/0/1/FFFF9B45F0CD-115cc5794dc-XZ /  1/0  /  test-file.txt
			//in every file location, there is file 'revisioninfo.xml' about that file. 
			
			String creator_username = (String) obj[1];
			String creator_id = (String) obj[10];
			String relativePath = (String) obj[2];
			String fileName = (String) obj[3];
			User creator = userDAO.getUserByUsername(creator_username);
			if(creator==null)
				creator = importUser(creator_id, false); 
	
					
			String filePath = REPO_DEAFAULT_DIR + relativePath + "\\1\\0\\" + fileName  ;
			File file = new File(filePath);
			if(file.exists()){
				
				ForumAttachedFile att = new ForumAttachedFile();
				
				//att.setAliasName(null) 
				att.setFileName(fileName);
				att.setId(Util.generateUUID());
				att.setCreator(creator);
		//	logger.info(".....2saveFile: fileName="+fileName+"...att.ID="+att.getId()+"...att.creatorId="+att.getCreatorId());			
				String path = attFileManager.saveAttachedFile(file, att.getId(),
						PredefinedNames.MODULE_FORUM, th.getForum().getProject().getId());
				
				att.setPath(path);
				att.setSize(file.length());
				att.setContentType(mimetypeMap.getContentType(fileName));
				if(thR!=null){
					att.setThreadReply(thR);
					thR.addAttachFile(att);
				}
				else{
					att.setThread(th);
					th.addAttachFile(att);
				}
				
				
				attList.add(att);
			}
		}
		return attList;
	}
	
	
	/**
	 * @param type : can be LogEntry.TYPE_BLOG_REFLECTION and LogEntry.TYPE_FORUM_REFLECTION
	 * @param objId : ID of the object (in eureka1) that linked to reflection
	 * @param destinateObjId : ID of Blog or Thread in eureka2
	 */
	private List<LogEntry> importReflections(String type, String objId, String destinateObjId, Project destinateProject){
		List<LogEntry> refList = new ArrayList<LogEntry>();
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT ref.*, u.userName AS creator FROM ntusummary ref "
				+ " JOIN mbuser u ON ref.creator_id=u.user_id "
				+ " WHERE ref.link_id = :objId "
				)
				.setString("objId", objId);
		
		//summary_id, link_id, creator_id, title, body, createDate, lastModifiedDate, accessRestricted	
		
			q.addScalar("summary_id")
			.addScalar("link_id")
			.addScalar("creator")
			.addScalar("title")
			.addScalar("body")
			.addScalar("createDate", Hibernate.LONG)
			.addScalar("lastModifiedDate", Hibernate.LONG)
			.addScalar("accessRestricted") 
			.addScalar("creator_id")
			;
	
		List<Object []> objList = q.list();
		session2.getTransaction().commit();
		
		for(Object[] obj : objList){
			LogEntry ref = new LogEntry();
			if(LogEntry.TYPE_BLOG_REFLECTION.equals(type)){
				ref.setBlogId(destinateObjId);
				ref.setType(LogEntry.TYPE_BLOG_REFLECTION);
			}
			else if(LogEntry.TYPE_FORUM_REFLECTION.equals(type)){
				ref.setForumThreadId(Long.parseLong(destinateObjId));
				ref.setType(LogEntry.TYPE_FORUM_REFLECTION);
			}
			ref.setProject(destinateProject);
			
			
			ref.setCdate(longObjToDate((Long) obj[5]));
			ref.setContent((String) obj[4]);
			User creator = userDAO.getUserByUsername((String) obj[2]);
			if(creator==null)
				creator = importUser(((String) obj[8]), false); 
			ref.setCreator(creator);
			ref.setMdate(longObjToDate((Long) obj[6]));
			ref.setShared(! (Boolean) obj[7]); //opposite of 'accessRestricted'
			ref.setTitle((String) obj[3]);
			//ref.setId(id)   
			//ref.setFiles(files);   //reflection in eureka1 doesn't have this
			//ref.setImages(images)
			//ref.setLinks(links)
			
			learninglogDAO.saveLogEntry(ref);
			
			refList.add(ref);
		}
		
		
		return refList;
	}
	
	
	private Date longObjToDate(Object value){
		Long longValue = (long) 0;
		if(value instanceof Long)
			longValue = (Long) value;
		else 
			return null;
		if(longValue<=0)
			return null;
		return new Date(longValue);
	}
	private ThreadType threadCategoryPictureToThreadType(String categoryPic){
		if(categoryPic.equals("forum_category1.gif"))
			return ThreadType.DISCUSS_TOPIC;
		if(categoryPic.equals("forum_category2.gif"))
			return ThreadType.ASK_A_QUESTION;
		if(categoryPic.equals("forum_category3.gif"))
			return ThreadType.SUGGEST_IMPROVMENTS;
		if(categoryPic.equals("forum_category4.gif"))
			return ThreadType.DEVELOPING_SOLUTIONS;
		if(categoryPic.equals("forum_category5.gif"))
			return ThreadType.IDENTITFY_PROBLEMS;
		if(categoryPic.equals("forum_category6.gif"))
			return ThreadType.ADD_OPINION_OR_FEEDBACK;
		return null;
	}
	private long countObjToLong(Object count){
		if(count==null)
			return 0;
		if(count instanceof BigInteger)
			return ((BigInteger) count).longValue();
		if(count instanceof Integer)
			return new Long((Integer) count);
	
		return (Long) count;
	}
	
	@CommitAfter
	@Override
	public Project assignModuleToProject(String moduleName, Project proj){
		//if project don't have this module, add it first
		Module m = moduleDAO.getModuleByName(moduleName);
		ProjModule pm = proj.getProjModule(m);
		if(pm==null){
			proj.addModule(new ProjModule(proj, m));
			projDAO.saveProject(proj);
		}
		return proj;
	}

	@Override
	public void saveMigratedProjInfo(MigratedProjInfo migratedProjInfo){
		session.save(migratedProjInfo);
	}
	@Override
	public MigratedProjInfo getMigratedProjInfo(String eureka2ProjId){
		return (MigratedProjInfo) session.get(MigratedProjInfo.class, eureka2ProjId);
	}
	@Override
	@CommitAfter
	public void deleteMigratedProjInfo(MigratedProjInfo migratedProjInfo){
		session.delete(migratedProjInfo);
	}
	@Override
	public MigratedProjInfo getMigratedProjInfoByVer1ProjId(String ver1ProjId){
		Query q = session.createQuery("SELECT mpi FROM MigratedProjInfo mpi, Project p  "
				+ " WHERE mpi.eureka1ProjId = :projId " 
				+ " AND mpi.eureka2ProjId=p.id "
				+ " AND p.status.name = :statusDelete  ")
				.setString("projId", ver1ProjId)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
				;
		List<MigratedProjInfo> pList = q.list();
		if(pList.isEmpty())
			return null;
		return pList.get(0);
	}
	
	@Override
	public boolean hasItemsToMigrate(String mProjId) {
		return (countAnnouncementToMigrate(mProjId)>0
				|| countActivitiesToMigrate(mProjId)>0
				|| countFileToMigrate(mProjId)>0
				|| countDiscussionToMigrate(mProjId)>0
				|| countWebblogToMigrate(mProjId)>0
				|| countLinkToMigrate(mProjId)>0
				);
				
	}
	
	
	@Override
	public long countAnnouncementToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(n.news_id) FROM mbnews n "
				+ " WHERE link_id = :projId ")
				.setString("projId", mProjId);
		Object count =  q.uniqueResult();
		session2.getTransaction().commit();
		
		return countObjToLong(count);
	}
	@Override
	public long countActivitiesToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(t.task_id) " 
				+" FROM mbtask t "
				+ " JOIN mbmilestone m ON t.milestone_id=m.milestone_id "
				+ " WHERE m.campaign_id = :projId ")
				.setString("projId", mProjId);
		Object count =  q.uniqueResult();
		session2.getTransaction().commit();
		
		return countObjToLong(count);
	}
	
	
	@Override
	public long countFileToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT f.tnode_id "
				+ " FROM mbtaxonomynode f "
				+ " JOIN mbcampaign c ON f.treeId=c.assetTreeId "
				+ " WHERE c.campaign_id = :projId "
				)
				.setString("projId", mProjId);
		q.addScalar("tnode_id")
			;
		List<Object > objList = q.list();
		session2.getTransaction().commit();
		long totalCount = 0;
		for(Object obj : objList){
			String mFolderId = (String) obj; 
			session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
			session2.beginTransaction();
			q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(f.asset_id)" +
					" FROM  mbtaxonomynodeobjectlink AS l, mbasset AS f   "
					+ " WHERE f.asset_id = l.object_id "
					+ " AND l.tnode_id = :rTNodeId " 
					)
					.setString("rTNodeId", mFolderId);
			
			Object count =  q.uniqueResult();
			session2.getTransaction().commit();
			totalCount += countObjToLong(count);
		}
		return totalCount;
	}
	@Override
	public long countDiscussionToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(f.forum_id) FROM mbforum f "
				+ " WHERE f.link_id = :projId ")
				.setString("projId", mProjId);
			
		Object count =  q.uniqueResult();
		session2.getTransaction().commit();
		return countObjToLong(count);
	}
	@Override
	public long countWebblogToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(b.weblog_id) FROM mbweblog b "
				+ " WHERE b.link_id = :projId "
				)
				.setString("projId", mProjId);
		
		Object count =  q.uniqueResult();
		session2.getTransaction().commit();
		return countObjToLong(count);
	}
	@Override
	public long countLinkToMigrate(String mProjId) {
		Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
		session2.beginTransaction();
		SQLQuery q = (SQLQuery) session2.createSQLQuery("SELECT COUNT(l.website_id) FROM mbwebsitelink l "
				+ " JOIN mbwebsitefolder f ON l.link_id = f.folder_id "
				+ " WHERE f.link_id = :projId "
				)
				.setString("projId", mProjId);
		
		Object count =  q.uniqueResult();
		session2.getTransaction().commit();
		return countObjToLong(count);
	}
	
}
