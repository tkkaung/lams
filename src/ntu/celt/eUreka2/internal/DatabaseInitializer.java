package ntu.celt.eUreka2.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.announcement.PrivilegeAnnouncement;
import ntu.celt.eUreka2.modules.assessment.PrivilegeAssessment;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;
import ntu.celt.eUreka2.modules.budget.PrivilegeBudget;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.message.PrivilegeMessage;
import ntu.celt.eUreka2.modules.resources.PrivilegeResource;
import ntu.celt.eUreka2.modules.scheduling.PrivilegeSchedule;
import ntu.celt.eUreka2.modules.usage.PrivilegeUsage;
import ntu.celt.eUreka2.services.email.EmailTemplate;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DatabaseInitializer implements Runnable {
	/*
	@Inject private Session ses;
	'Inject' can not be used here, because it is called in AppModule.contributeRegistryStartup
	which runs before services are initialized
	*/
	
	/**
	 * it initialize the database, this function is called when the application startup
	 * (in function contributeRegistryStartup() inside AppModule.java) //probably we don't need this class, just import initialized database from .sql
	 */
	public void run() {
		boolean needToRun = Config.getBoolean("run.initialize.database");
		if(needToRun){
			initAllTable();
		}
	}
	public void initAllTable(){
		initTbl_School();
		initTbl_Privilege();
		initTbl_SysRole();
		initTbl_User();
		initTbl_EmailTemplate();
		initTbl_ProjStatus();
		initTbl_ProjType();
		initTbl_ProjRole();
		initTbl_Module();
		
		HibernateUtil.getSessionFactory().close();
	}
	

	/**
	 * Add/Delete privileges into database, according to fields defined in 
	 * ntu.celt.eUreka2.data.PrivilegeConstant : getALLPrivilege()
	 */
	@SuppressWarnings("unchecked")
	private void initTbl_Privilege(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		//System
		List<Privilege> pList = new ArrayList<Privilege>();
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.CREATE_PROJECT, "Create Adhoc Project", "Create Adhoc Projects"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_PROJECT,"Manage Projects", "Add/Edit/Delete Projects, house keeping tasks"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_USER,"Manage Users", "Add/Edit/Delete Users, house keeping tasks"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_EMAIL_TEMPLATE, "Manage Email Templates", "Edit email templates used send to users for each type of notification email"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_SYSTEM_DATA, "Manage System Data", "Add/Edit/Delete System Data such as Privilege, ProjStatus, Module..."));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_SYSTEM_ROLE, "Manage System Role", "Add/Edit/Delete System Role, and Privileges of each Role"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_PROJ_ROLE, "Manage Project Role", "Add/Edit/Delete Project Role, and Privileges of each Role"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_PROJ_TYPE, "Manage Project Type", "Add/Edit/Delete Project Type, and configure the type settings"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.SYSTEM_ANNOUNCEMENT, "System Annmt", "Add System Announcement which will show on every page"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.PROJ_SCHOOL_ANNOUNCEMENT, "Annmt by Schl/Dept ", "Add announcement/Importan-Info in 'Project Info' page of projects of the selected School/Department. The School/Department is determined by the User's School/Department"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.PROJ_TYPE_ANNOUNCEMENT, "Annmt by Project Type", "Add announcement/Importan-Info in 'Project Info' page of projects of the selected Project-Type. The Project-Type is determined by the User's Project-Type"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_OWN_RUBRIC, "Manage Own Rubric", "Add/Edit/Delete his/her own rubrics. Student role may not need this"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_SCHOOL_RUBRIC, "Manage School Rubrics", "Add/Edit/Delete rubrics, and can set as Master Rubric for the School"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.MANAGE_RUBRIC, "Manage Rubrics", "Add/Edit/Delete all rubrics, and can Set Master rubrics"));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.CHANGE_EMAIL, "Change Email", "Change his/her own email (to his/her personal email, it may cause problem if email is invalid)."));
		pList.add(new Privilege( PrivilegeType.SYSTEM, PrivilegeSystem.DIRECT_LOGIN, "Direct Login", "Login directly from the Login page, (no need to go through Blackboard)"));
		
		
		
		//Project
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.READ_PROJECT, "Read Project", "Read the project, User must have this to access the project"));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.UPDATE_PROJECT,"Update Project", "Update base info of the project"));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.ENROLL_MEMBER,"Enroll Members", "Enroll/Remove members, also assign project-role of the member. Non-members may send email to request for enrollment to the user with this Privilege"));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.ENROLL_ORG_SUPERVISOR,"Enroll Organization Supervisor", "Enroll/Remove organization supervisor. May create new user account with email as username."));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.ASSIGN_MODULE, "Assign Modules", "Add/Remove modules to be included in the project"));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.CHANGE_STATUS,"Change Project Status", "Change project status, e.g Active, Inactive, Archived, Reference, Deleted. Users with this privilege may receive email from the system to notify status changes."));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.UPDATE_PROJECT_SCOPE,"Change Project Scope / Pre-requisite", "Update project scope/pre-rquisite to capture up-to-date changes from on-site discussion with Organization Supervisor after student reports to the attachment organization. The changes require approval."));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.APPROVE_PROJECT_SCOPE,"Approve Project Scope / Pre-requisite", "Approve or Reject the update to project scope/pre-requisite"));
		pList.add(new Privilege( PrivilegeType.PROJECT, PrivilegeProject.IS_LEADER,"Is Leader", "Indicate the role is the Leader of that type of project. If the project has 'Hide Member List' setting, only this user role is shown."));
		
		
		//ANNOUNCEMENT
		pList.add(new Privilege( PrivilegeType.ANNOUNCEMENT, PrivilegeAnnouncement.CREATE_ANNOUNCEMENT, "Create Announcement", "Create Announcement in the project"));
		pList.add(new Privilege( PrivilegeType.ANNOUNCEMENT, PrivilegeAnnouncement.UPDATE_ANNOUNCEMENT, "Update Announcement", "Update Announcements. including announcements created by other members"));
		pList.add(new Privilege( PrivilegeType.ANNOUNCEMENT, PrivilegeAnnouncement.DELETE_ANNOUNCEMENT, "Delete Announcement", "Delete Announcements. including announcements created by other members"));
		pList.add(new Privilege( PrivilegeType.ANNOUNCEMENT, PrivilegeAnnouncement.VIEW_ANNOUNCEMENT, "View Announcement", "View published Announcements"));
		pList.add(new Privilege( PrivilegeType.ANNOUNCEMENT, PrivilegeAnnouncement.ADMIN_MANAGE, "Admin Manage", "View List of Announcements from all members within the project"));
				
		//ASSESSMENT
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.ACCESS_MODULE, "Access Module", "Access to Assessment module"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.CREATE_ASSESSMENT, "Create Assessment", "Create Assesment"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.EDIT_ASSESSMENT, "Edit Assessment", "Edit Assessment. Apply to all assessments within the project"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.DELETE_ASSESSMENT, "Delete Assessment", "Delete Assessment. Apply to all assessments within the project"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.VIEW_ASSESSMENT, "View Assessment", "View Assessment. Apply to all assessments within the project"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.VIEW_ASSESSMENT_GRADES, "View Assessment Grades", "View Assessment Grades. Apply to all assessments within the project"));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.GRADE_ASSESSMENT, "Grade Assessment", "Grade Assessment. Apply to all assessments within the project "));
		pList.add(new Privilege( PrivilegeType.ASSESSMENT, PrivilegeAssessment.IS_ASSESSEE, "Is Assessee", "Indicate that this role of user will be assessed. Assessors have option to hide assessment-rubric/grade from assessees"));
		
		
		//BLOG
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.CREATE_BLOG, "Create Blog", "Create Blog"));
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.EDIT_BLOG, "Edit Blog", "Edit Blogs. including blogs created by other members"));
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.DELETE_BLOG, "Delete Blog", "Delete Blogs. including blogs created by other members"));
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.ADD_COMMENT, "Add comment", "Add comment to any published Blog"));
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.DELETE_COMMENT, "Delete comment", "Delete comments. including comments added by other members"));
		pList.add(new Privilege( PrivilegeType.BLOG, PrivilegeBlog.VIEW_BLOG, "View Blogs", "View blogs, (other members' Public blogs, and his/her own private blogs"));

		//ELOG
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.CREATE_ELOG, "Create elog", "Create elog "));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.EDIT_ELOG, "Edit elog", "Edit elogs. Once requested for approval, owner cannot edit; Only user with this privilege can. Apply to All elogs within the project"));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.DELETE_ELOG, "Delete elog", "Delete elogs. Once requested for approval, owner cannot delete. only user with this privilege can. Apply to All elogs within the project"));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.ADD_COMMENT, "Add comment", "Add comment to any published eLog"));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.DELETE_COMMENT, "Delete comment", "Delete comments, including comments created by other members"));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.VIEW_ELOG, "View elogs", "View published elogs. "));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.APPROVE_ELOG, "Approve elogs", "Approve/Reject elogs, and publish/unpublish elogs. "));
		pList.add(new Privilege( PrivilegeType.ELOG, PrivilegeElog.VIEW_APPROVAL_LIST, "View approval list", "View list of elogs that are pending for approval, Rejected, and Published"));
		
		
		//BUDGET
		pList.add(new Privilege( PrivilegeType.BUDGET, PrivilegeBudget.VIEW_BUDGET, "View Budget", "View Budget module"));
		pList.add(new Privilege( PrivilegeType.BUDGET, PrivilegeBudget.ADD_TRANSACTION, "Add Transaction", "Add transaction"));
		pList.add(new Privilege( PrivilegeType.BUDGET, PrivilegeBudget.EDIT_TRANSACTION, "Edit Transaction", "Edit transaction, including transactions added by other members"));
		pList.add(new Privilege( PrivilegeType.BUDGET, PrivilegeBudget.DELETE_TRANSACTION, "Delete Transaction", "Delete transaction, including transactions added by other members"));
		
		//FORUM
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.CREATE_FORUM, "Create Forum", "Create forum"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.CREATE_THREAD, "Create Forum Thread", "Create forum thread to start a discussion. Apply to all forums within the project"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.REPLY_THREAD, "Reply to Thread", "Reply to threads, apply to all threads within the project"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.EDIT_FORUM, "Edit Forum", "Edit forum info, , including forums created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.EDIT_THREAD, "Edit Forum Thread", "Edit forum threads, including threads created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.EDIT_REPLY_THREAD, "Edit Thread Replies", "Edit Thread replies, including replies created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.DELETE_FORUM, "Delete Forum", "Delete forums, including forums created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.DELETE_THREAD, "Delete Thread", "Delete threads, including threads created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.DELETE_REPLY_THREAD, "Delete Thread Replies", "Delete thread replies, including replies created by other members"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.VIEW_FORUM, "View Forum", "View forums"));
		pList.add(new Privilege( PrivilegeType.FORUM, PrivilegeForum.VIEW_THREAD, "View Thread", "View thread and thread replies"));
		
		
		//MESSAGE
		pList.add(new Privilege( PrivilegeType.MESSAGE, PrivilegeMessage.CREATE_MESSAGE, "Create Message", "Create and Send message"));
		pList.add(new Privilege( PrivilegeType.MESSAGE, PrivilegeMessage.REPLY_MESSAGE, "Reply Message", "Reply and Send message"));
		//pList.add(new Privilege( PrivilegeType.MESSAGE, PrivilegeMessage.DELETE_MESSAGE, "Delete Message", "Delete message"));
		pList.add(new Privilege( PrivilegeType.MESSAGE, PrivilegeMessage.VIEW_MESSAGE, "View Message", "View messages, own messages only"));

		//RESOURCES
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.VIEW_RESOURCE, "View Resources", "View resources (Browser folder and Download file)"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.CREATE_FILE, "Create File", "Upload file"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.UPDATE_FILE, "Update File", "Edit file info, including shared files uploaded by other members"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.REMOVE_FILE, "Remove File", "Remove file, including shared files uploaded by other members "));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.CREATE_FOLDER, "Create Folder", "Create folder"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.UPDATE_FOLDER, "Update Folder", "Update folder info, including shared folders created by other members "));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.REMOVE_FOLDER, "Remove Folder", "Remove folder, including shared folders created by other members"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.CREATE_LINK, "Create Link", "Create link"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.UPDATE_LINK, "Update Link", "Update link info, including shared links created by other members"));
		pList.add(new Privilege( PrivilegeType.RESOURCES, PrivilegeResource.REMOVE_LINK, "Remove Link", "Remove link, including shared links created by other members"));
		
		//SCHEDULE
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.VIEW_SCHEDULE, "View Schedule", "View Schedule/Timeline/Gantt Chart. Including details of Milestones, Phases, and Tasks"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.CREATE_MILESTONE, "Create Milestone", "Create milestone"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.UPDATE_MILESTONE, "Update Milestone", "Update milestones, including milestones created by other members"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.REMOVE_MILESTONE, "Remove Milestone", "Remove milestones, including milestones created by other members"));
		//pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.VIEW_MILESTONE, "View Milestone", "View milestones"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.CREATE_PHASE, "Create Phase", "Create phase"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.UPDATE_PHASE, "Update Phase", "Update phases, including phases created by other members"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.REMOVE_PHASE, "Remove Phase", "Remove phases, including phases created by other members"));
		//pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.VIEW_PHASE, "View Phase", "View phase"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.CREATE_TASK, "Create Task", "Create task"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.UPDATE_TASK, "Update Task", "Update tasks, including tasks created by other members. (By default creator and assigned member can update it)"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.REMOVE_TASK, "Remove Task", "Remove tasks, including tasks created by other members"));
		//pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.VIEW_TASK, "View Task", "View task"));
		pList.add(new Privilege( PrivilegeType.SCHEDULE, PrivilegeSchedule.MANAGE_VERSION, "Manage Version", "Save/Restore/Compare version"));
		
		
		//USAGE
		pList.add(new Privilege( PrivilegeType.USAGE, PrivilegeUsage.VIEW_CONTRIBUTION, "View Contribution", "View members' usage report of all modules in the project"));
		
		
		List<Privilege> privilegesDb = session.createCriteria(Privilege.class).list();
		
		//check to delete
		for(Privilege privDb : privilegesDb){
			if(!pList.contains(privDb)){ 
				if(privDb.getType().equals(PrivilegeType.SYSTEM)){
					List<SysRole> sysRoles = session.createCriteria(SysRole.class).list();
					for(SysRole r : sysRoles){
						if(r.getPrivileges().contains(privDb)){
							r.getPrivileges().remove(privDb);
							session.save(r);
						}
					}
				}
				else {
					List<ProjRole> projRoles = session.createCriteria(ProjRole.class).list();
					for(ProjRole r : projRoles){
						if(r.getPrivileges().contains(privDb)){
							r.getPrivileges().remove(privDb);
							session.save(r);
						}
					}
				}
				
				session.delete(privDb);
			}
		}
		//check to add
		for(Privilege privJava : pList){
			if(!privilegesDb.contains(privJava)){
				session.save(privJava);
			}
			//update
			else{
				for(int i=0; i<privilegesDb.size(); i++){
					Privilege privDb = privilegesDb.get(i);
					if(privDb.equals(privJava)){
						privDb.setName(privJava.getName());
						privDb.setDescription(privJava.getDescription());
						session.save(privDb);
						privilegesDb.remove(i);
						break;
					}
				}
			}
			
		}
		
		session.getTransaction().commit();
	}
	
	/**
	 * If SUPER_ADMIN role not exists, create it.
	 * Default Name is: SUPER_ADMIN - Super Administrator of the application,
	 * 					he/she has all privileges.
	 * It must be called after initTbl_Privilege 
	 */
	@SuppressWarnings("unchecked")
	private void initTbl_SysRole(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		String roles[] = {PredefinedNames.SYSROLE_ADMIN, PredefinedNames.SYSROLE_FACULTY
				,PredefinedNames.SYSROLE_PROJTYPE_ADMIN, PredefinedNames.SYSROLE_SCHOOL_ADMIN
				,PredefinedNames.SYSROLE_STUDENT, PredefinedNames.SYSROLE_USER
				, PredefinedNames.SYSROLE_EXTERNAL_USER};
		
		SysRole r = (SysRole) session.createCriteria(SysRole.class)
				.add(Restrictions.eq("name", PredefinedNames.SYSROLE_SUPER_ADMIN))
				.uniqueResult();
		if(r==null ){
			r = new SysRole();
			r.setName(PredefinedNames.SYSROLE_SUPER_ADMIN);
			r.setDescription("Super Administrator of the application, he/she has all privileges.");
			r.setSystem(true);
			Set<Privilege> privileges = new HashSet<Privilege>();
			List<Privilege> pList = session.createCriteria(Privilege.class).list();
			for(Privilege p : pList){
				if(p.getType().equals(PrivilegeType.SYSTEM))
					privileges.add(p);
			}
			r.setPrivileges(privileges);
			session.save(r);
		}

		for(String role : roles){
			r = (SysRole) session.createCriteria(SysRole.class)
						.add(Restrictions.eq("name", role))
						.uniqueResult();
			if(r==null ){
				r = new SysRole();
				r.setName(role);
				r.setDescription(role);
				r.setSystem(true);
				session.save(r);
			}
		}
    /*    SysRole r = (SysRole) session.createCriteria(SysRole.class)
        			.add(Restrictions.eq("name", PredefinedNames.SYSROLE_SUPER_ADMIN))
        			.uniqueResult();
        if(r==null ){
        	r = new SysRole();
        	r.setName(PredefinedNames.SYSROLE_SUPER_ADMIN);
        	r.setDescription("Super Administrator of the application, he/she has all privileges.");
        	r.setSystem(true);
        	Set<Privilege> privileges = new HashSet<Privilege>();
        	List<Privilege> pList = session.createCriteria(Privilege.class).list();
        	for(Privilege p : pList){
        		if(p.getType().equals(PrivilegeType.SYSTEM))
        			privileges.add(p);
        	}
        	r.setPrivileges(privileges);
        	session.save(r);
        }
       SysRole r2 = (SysRole) session.createCriteria(SysRole.class)
				.add(Restrictions.eq("name", PredefinedNames.SYSROLE_USER))
				.uniqueResult();
		if(r2==null ){
			r2 = new SysRole();
			r2.setName(PredefinedNames.SYSROLE_USER);
			r2.setDescription("User of the system.");
			r2.setSystem(true);
			session.save(r2);
		}
       */ 
        session.getTransaction().commit();
	}
	
	
	/**
	 * If no school exists, create it. 
	 * Default Name is: Others - Others
	 */
	@SuppressWarnings("unchecked")
	private void initTbl_School(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

        List<School> l = session.createCriteria(School.class)
        	.add(Restrictions.eq("name", PredefinedNames.SCHOOL_OTHERS))
        	.list();
        if(l==null || l.size()==0){
        	School s = new School();
        	s.setName(PredefinedNames.SCHOOL_OTHERS);
        	s.setDes("Others");
        	s.setSystem(true);
        	session.save(s);
        }
        session.getTransaction().commit();
	}
	
	@SuppressWarnings("unchecked")
	private void initTbl_ProjType(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		String types[] = {PredefinedNames.PROJTYPE_ADHOC, PredefinedNames.PROJTYPE_CAO
				,PredefinedNames.PROJTYPE_FYP, PredefinedNames.PROJTYPE_SENATE
				, PredefinedNames.PROJTYPE_COURSE};
		
		for(String type : types){
			List<ProjType> l = session.createCriteria(ProjType.class)
	        	.add(Restrictions.eq("name", type))
	        	.list();
	        if(l==null || l.size()==0){
	        	ProjType t = new ProjType();
	        	t.setName(type);
	        	t.setDes(type);
	        	t.setSystem(true);
	        	session.save(t);
	        }
		}
        session.getTransaction().commit();
	}
	@SuppressWarnings("unchecked")
	private void initTbl_ProjStatus(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		String status[] = {PredefinedNames.PROJSTATUS_ACTIVE
				, PredefinedNames.PROJSTATUS_INACTIVE
				, PredefinedNames.PROJSTATUS_ARCHIVED
				, PredefinedNames.PROJSTATUS_DELETED
				, PredefinedNames.PROJSTATUS_REFERENCE};
		String statusDes[] = {"The project is on going"
				, "The project has not been accessed for more than 90 days"
				, "The project has moved out from normal flow, it has been INACTIVE for more than 30 days"
				, "The project is marked as DELETED, All data and attached files will be permanently deleted in 30 days from the delete day"
				, "The project is Read-only mode, non-members can also access"};
		
		for(int i=0; i<status.length; i++){
	        List<ProjStatus> l = session.createCriteria(ProjStatus.class)
	        	.add(Restrictions.eq("name", status[i]))
	        	.list();
	        if(l==null || l.size()==0){
	        	ProjStatus t = new ProjStatus();
	        	t.setName(status[i]);
	        	t.setDescription(statusDes[i]);
	        	t.setSystem(true);
	        	session.save(t);
	        }
		}
        session.getTransaction().commit();
	}
	@SuppressWarnings("unchecked")
	private void initTbl_ProjRole(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

        String roles[] = {PredefinedNames.PROJROLE_LEADER, PredefinedNames.PROJROLE_MEMBER
        		,PredefinedNames.PROJROLE_SUPERVISOR, PredefinedNames.PROJROLE_STUDENT
        		,PredefinedNames.PROJROLE_EXAMINER, PredefinedNames.PROJROLE_SCHOOL_TUTOR
        		,PredefinedNames.PROJROLE_ORG_SUPERVISOR};
	    for(String r : roles){
			List<ProjRole> l = session.createCriteria(ProjRole.class)
	        	.add(Restrictions.eq("name", r))
	        	.list();
	        if(l==null || l.size()==0){
	        	ProjRole t = new ProjRole();
	        	t.setName(r);
	        	t.setDes(r);
	        	t.setSystem(true);
	        	session.save(t);
	        }
        }
        session.getTransaction().commit();
	}
	
	
	
	/**
	 * If 'superadmin' not exists, create it.
	 * it must be called after initTbl_School and initTbl_SysRole 
	 */
	private void initTbl_User(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

        User u = (User) session.createCriteria(User.class)
        		.add(Restrictions.eq("username", "superadmin"))
        		.uniqueResult();
        if(u==null ){
        	u = new User();
        	u.setCreateDate(new Date());
        	u.setEmail(Config.getString(Config.SYSTEM_EMAIL_ADDRESS));
        	u.setEnabled(true);
        	u.setExternalKey("");
        	u.setFirstName("Administrator");
        	u.setLastName("Super");
        	u.setUsername("superadmin");
        	u.setPassword(Util.generateHashValue("eureka_20"));
        	School school = (School) session.createCriteria(School.class)
				.add(Restrictions.eq("name", PredefinedNames.SCHOOL_OTHERS)).uniqueResult();
        	u.setSchool(school);
        	u.setOrganization("eUreka system users");
        	SysRole sysRole = (SysRole) session.createCriteria(SysRole.class)
        				.add(Restrictions.eq("name", PredefinedNames.SYSROLE_SUPER_ADMIN)).uniqueResult();
        	u.setSysRole(sysRole);
        	//leave all other fields as null
        	
        	session.save(u);
        }
        session.getTransaction().commit();
	}
	
	private void initTbl_EmailTemplate(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		EmailTemplate e = (EmailTemplate) session.createCriteria(EmailTemplate.class)
        		.add(Restrictions.eq("type", "default"))
        		.uniqueResult();
        if(e==null ){
        	e = new EmailTemplate();
        	e.setContent("<p>%userDisplayName% has updated an item %ItemTitle% in " +
        			"%ProjectName%  on %ModifiedTime% </p>" +
        			"<div style='border: 2px solid #aaaaaa; padding:5px;'>%ItemContent%</div> " +
        			"<p>This is an auto generated email, please do not reply.</p>"
        		);
        	e.setDescription("Default email template");
        	e.setLanguage("en");
        	e.setModifyDate(new Date());
        	e.setSubject("[eUreka] notification of update for %ProjectName%");
        	e.setType("default");
        	
        	session.save(e);
        }
        session.getTransaction().commit();
	}
	
	/**
	 * If no module exists, create it. 
	 */
	@SuppressWarnings("unchecked")
	private void initTbl_Module(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		List<Module> mList = new ArrayList<Module>();
		mList.add(new Module(PredefinedNames.MODULE_ANNOUNCEMENT
				,null
				,"This module acts as Notice board of the selected project. The members can post up announcements, news, and/or reminders for other members to see. When more attention is needed, They can indicate the announcements as Important and notify other member by email"
				,"modules/announcement" 
				,"lib/img/mod/btnann.png"));
		mList.add(new Module(PredefinedNames.MODULE_FORUM
				,null
				,"Forum is the online discussion board to facilitate group discussion. Members and Supervisors can raise issues to discuss. Discussion threads are recorded for later reference. Users can rate, flag for follow up, and mark as important for quick access on a later date."
				,"modules/forum" 
				,"lib/img/mod/btnfor.png"));
		mList.add(new Module(PredefinedNames.MODULE_ELOG
				,"eLog"
				,"Similar to Blog, eLog is the online space for students to record their progress, observations, thoughts….etc But It is more formal than Blog. Approval process is required before the log entry be published for other members to see, to ensure that no confidential information is disclosed by students. eLog is intended to be like or replace physical log-book for students to record their works in their Industrial/Professional Attachments."
				,"modules/elog/approveoverview" 
				,"lib/img/mod/btnnot.png"));
		mList.add(new Module(PredefinedNames.MODULE_LEARNING_LOG
				,"Personal Note"
				,"As its name imply, Personal Note is for users’ personal use. Only the creator of the note is allowed to access. Users can access their notes from any project site. Images and Files are allowed to attach to note, so users can be used Note to record anything they want, e.g thoughts, reflections, memo, to-do list  "
				,"modules/learninglog" 
				,"lib/img/mod/btntes.png"));
		mList.add(new Module(PredefinedNames.MODULE_SCHEDULING
				,"Timeline"
				,"Tool to create project timeline. Users define milestones, phase, and tasks for the project.  The module will generate gantt chart to graphically represent overall project status. It helps to monitor the progress of the project, and provide common understanding to project members of expectations, intents and deliverables. The users can use versioning function to keep history of the gantt chart. They can even export to excel file, then import it back a later time."
				,"modules/scheduling" 
				,"lib/img/mod/btnsch.png"));
		mList.add(new Module(PredefinedNames.MODULE_BUDGET
				,null
				,"This module helps to monitor budget of the project. Users can record fund and expenses during the project period. The transaction then be recorded, ordered, used to compute available balance, and displayed in table format, so that the users can easily identify latest balance and history of each transaction."
				,"modules/budget" 
				,"lib/img/mod/btnbud.png"));
		mList.add(new Module(PredefinedNames.MODULE_MESSAGE
				,null
				,"This allows members to send private message or email to other member(s) without leaving eUreka2.0. The other member(s) can then read the message in this module, or in his/her email inbox. You can think of this module as simplified version of email."
				,"modules/message" 
				,"lib/img/mod/btnmes.png"));
		mList.add(new Module(PredefinedNames.MODULE_BLOG
				,"Blog"
				,"This is the online space for Students to document their works, record their progress, observations, thoughts, insights, responses, learning experiences… etc. The blog entry created can be ‘public’ (visible by all project members) or student can keep it for his/her by making it private. comment on the blog is available for other students or supervisors to provide feedbacks."
				,"modules/blog/home" 
				,"lib/img/mod/btnblo.png"));
		mList.add(new Module(PredefinedNames.MODULE_RESOURCE
				,"Resources"
				,"This is the link and file repository of the project. Users can use it to store and share project files such as reference, papers, reports, useful links … etc with other project members. The users can organize their uploaded files into multiple folders. Multiple files of any format can be uploaded at the same time, with size limit up to 1GB per file. There is File versioning feature to keep track of uploaded files"
				,"modules/resources/home" 
				,"lib/img/mod/btnres.png"));
		mList.add(new Module(PredefinedNames.MODULE_ASSESSMENT
				,"Assessment"
				,"It helps project supervisors to assess students performance. The supervisors may decide to assess based on assessment rubric or input score manually.	The Rubric can be shared, customized, and reused. The supervisors have overall view of the score table, with color highlights. The supervisors can allow members to see the assessment(s), its rubric, and/or its graded score."
				,"modules/assessment/home" 
				,"lib/img/mod/btnass.png"));
		mList.add(new Module(PredefinedNames.MODULE_USAGE
				,"Usage Report"
				,"Give summary of how many items project members have contributed to the project, the user click to see the detail"
				,"modules/usage" 
				,"lib/img/mod/btnusg.png"));
		mList.add(new Module(PredefinedNames.MODULE_PEER_EVALUATION
				,"Peer Evaluation"
				,"Allow students to evaluate their peers with the group"
				,"modules/peerevaluation/home" 
				,"lib/img/mod/btnpee.png"));
		mList.add(new Module(PredefinedNames.MODULE_LEADERSHIP_PROFILING
				,"Leadership Profiling"
				,"Leadership Profiling"
				,"modules/profiling/manage" 
				,"lib/img/mod/btnlea.png"));
		

		for(Module m : mList){
			List<Module> l = session.createCriteria(Module.class)
	        	.add(Restrictions.eq("name", m.getName()))
	        	.list();
	        if(l==null || l.size()==0){
	        	session.save(m);
	        }
        }
        session.getTransaction().commit();
	}
}
