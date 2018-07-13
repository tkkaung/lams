package ntu.celt.eUreka2.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.CustomHibernateGridDataSource;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.ProjectSearchableField;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class ProjectDAOImpl implements ProjectDAO {
	
	private Session session;
	private ProjStatusDAO projStatusDAO;
	
	
	
	public ProjectDAOImpl(Session session, ProjStatusDAO projStatusDAO) {
		super();
		this.session = session;
		this.projStatusDAO = projStatusDAO;
	}
	
	
	@Override
	public String generateId(ProjType type, School school, Date sdate){
		//we generate a new ID in format Type-School-Year-Number, e.g: ADH-CELT-11-0001
		String id = type.getName() + "-" 
					+ (school==null? "" : school.getName().toUpperCase()) + "-"
					+ Util.formatDateTime(sdate, "yy") + "-";
		return generateId(id);
	}
	@SuppressWarnings("unchecked")
	@Override
	public String generateId(String prefix){
		prefix = prefix.replace(" ", "_");
		
		int num = 1;
		Query q = session.createQuery("SELECT p FROM Project AS p " +
				" WHERE id LIKE :rId " +
				" ORDER BY id DESC " 
				)
				.setString("rId", prefix+"%")
				.setMaxResults(1)
				;
		List<Project> plist = q.list();
		if(plist!=null && !plist.isEmpty() ){
			Project p = plist.get(0);
			if(p!=null){
				num = Integer.parseInt(p.getId().substring(prefix.length()));  //extract the number portion of the ID
				num++;
			}
		}
		prefix = prefix + String.format("%04d", num);
		return prefix;
	}
	
	@Override
	public void saveProject(Project project) {
		if(project.getId()==null){
			project.setId(generateId(project.getType(), project.getSchool(), project.getSdate()));
		}
		session.persist(project);
	}
	@Override
	public void updateProject(Project project) {
		session.update(project);
	}
	@Override
	public void immediateSaveProject(Project project) {
		if(project.getId()==null){
			project.setId(generateId(project.getType(), project.getSchool(), project.getSdate()));
		}
		session.save(project);
		session.flush();
		//session.clear();
	}
	
	@Override
	public void deleteProject(Project proj) {

		ProjStatus deleted = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_DELETED);
		
		/* not actual delete.
		All DAO retrieve function must remember to check for this field!
     	*/
		proj.setStatus(deleted);
		proj.setLastStatusChange(new Date()); 
		
		session.save(proj);
	}
	@Override
	public void deleteProjectPermanently(Project proj){
		/*	//note: if use only session.delete(proj) with cascade ALL, 
		//the projmodules and projusers still be removed even ConstraintViolationException has occured
		//so we use this work around instead
		*/
		//proj.setSchool(null);
		//proj.setStatus(null);
		
		proj.getProjmodules().clear();
		proj.getMembers().clear();
		
		
		
		session.delete(proj);  
	
	}
	@Override
	public void immediateDeleteProjectPermanently(Project proj){
		this.deleteProjectPermanently(proj);
		session.flush();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getAllProjects() {
		List<Project> projects = session.createQuery("FROM Project " 
				//+ " WHERE deleteDate IS NULL "
				+ " ORDER BY mdate DESC ")
				.list();
		return projects;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getAllProjects(int firtResult, int maxResult) {
		List<Project> projects = session.createQuery("FROM Project " 
				+ " ORDER BY mdate DESC ")
				.setFirstResult(firtResult)
				.setMaxResults(maxResult)
				.list();
		return projects;
	}

	@Override
	public Project getProjectById(String id) {
		if(id==null) 
			return null;
		Project proj = (Project) session.get(Project.class, id);
		if(proj==null)
			return null;
		/*if(proj.getStatus().getName().equals(PredefinedNames.PROJSTATUS_DELETED))
			return null;
		*/
		return proj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectByName(String name) {
		return session.createQuery("SELECT p FROM Project AS p " 
				+ " WHERE NOT(p.status.name = :statusDelete) "
				+ " AND p.name = :rName "
				)
				.setString("rName", name)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsByMember(User user) {
		return session.createQuery("SELECT p FROM Project AS p, User AS u " 
				+ " WHERE NOT(p.status.name = :statusDelete) "
				+ " AND u = :user "
				+ " AND u IN (SELECT pu.user FROM ProjUser pu WHERE pu.project=p)"
				)
				.setParameter("user", user)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsForHomeByMember(User user) {
		return session.createQuery("SELECT p FROM Project AS p, User AS u " 
				+ " WHERE (p.status.name = :statusActive OR p.status.name = :statusInactive) "
				+ " AND u = :user "
				+ " AND u IN (SELECT pu.user FROM ProjUser pu WHERE pu.project=p)"
				+ " ORDER BY p.sdate DESC, p.name ASC "
				)
				.setParameter("user", user)
				.setString("statusActive", PredefinedNames.PROJSTATUS_ACTIVE)
				.setString("statusInactive", PredefinedNames.PROJSTATUS_INACTIVE)
				
				
			.list();
	}
	@Override
	public GridDataSource getProjectsForHomeByMemberAsDataSource(User user,
			Integer firstResult, Integer maxResult,
			List<SortConstraint> sortConstraints) {
//		session.createQuery("SELECT p FROM Project AS p, User AS u " 
//				+ " WHERE (p.status.name = :statusActive OR p.status.name = :statusInactive) "
//				+ " AND u = :user "
//				+ " AND u IN (SELECT pu.user FROM ProjUser pu WHERE pu.project=p)"
//				+ " ORDER BY p.sdate DESC, p.name ASC "
//				)
//				.setParameter("user", user)
//				.setString("statusActive", PredefinedNames.PROJSTATUS_ACTIVE)
//				.setString("statusInactive", PredefinedNames.PROJSTATUS_INACTIVE)
//				
//				
//			.list();
//		
		List<Criterion> criterions = new ArrayList<Criterion>();
		Map<String, String> aliasMap = new HashMap<String, String>();
		aliasMap.put("members", "pu");
		aliasMap.put("status", "st");
	
		
		criterions.add(		Restrictions.or(
			Restrictions.eq("st.name", PredefinedNames.PROJSTATUS_ACTIVE),
			Restrictions.eq("st.name", PredefinedNames.PROJSTATUS_INACTIVE)
		));
		criterions.add(	Restrictions.eq("pu.user", user));
		
		
		CustomHibernateGridDataSource datasrc = new CustomHibernateGridDataSource(session, Project.class);
		datasrc.setAlias(aliasMap);
		datasrc.setCriterions(criterions);
		
		datasrc.prepare(firstResult, (firstResult+maxResult), sortConstraints);
		
		
		return datasrc;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsByCourseId(String courseId) {
		return session.createQuery("SELECT p FROM Project AS p " 
				+ " WHERE NOT(p.status.name = :statusDelete) "
				+ " AND p.courseId = :cId "
				)
				.setParameter("cId", courseId)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsByCourseIdUserId(String courseId, String username) {
		return session.createQuery("SELECT p FROM Project AS p, User AS u " 
				+ " WHERE NOT(p.status.name = :statusDelete) "
				+ " AND p.courseId = :cId "
				+ " AND u.username = :username "
				+ " AND u IN (SELECT pu.user FROM ProjUser pu WHERE pu.project=p)"
				)
				.setParameter("cId", courseId)
				.setParameter("username", username)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.list();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsByCourseIdGroupId(String courseId, String groupId) {
		return session.createQuery("SELECT p FROM Project AS p " 
				+ " WHERE NOT(p.status.name = :statusDelete) "
				+ " AND p.courseId = :cId "
				+ " AND p.groupId = :gId "
				)
				.setParameter("cId", courseId)
				.setParameter("gId", groupId)
				.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED)
			.list();
	}

	
	@Override
	public GridDataSource searchProjectsAsDataSource(String filterText, ProjectSearchableField filterIn,
			String username, Date filterSDate, Date filterEDate, ProjStatus filterStatus,
			ProjType filterType, School filterSchool,
			Integer firstResult, Integer maxResult,
			List<SortConstraint> sortConstraints, boolean includeDeleted) {
		
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		
		if(filterStatus!=null){
			criterions.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			criterions.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			criterions.add(Restrictions.eq("school", filterSchool));
		}
		if(filterText!=null){
			String[] words = filterText.split(" ");
			if(filterIn!=null){
				for(String word : words){
					criterions.add(Restrictions.like(filterIn.name(), "%"+word+"%"));
				}
			}
			else {
				for(String word : words){
					Criterion cr = null;
					for(int i=0; i<ProjectSearchableField.values().length; i++){
						ProjectSearchableField f = ProjectSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.like(f.name(), "%"+word+"%");
						else
							cr = Restrictions.or(cr, Restrictions.like(f.name(), "%"+word+"%"));
					}
					if(cr!=null)
						criterions.add(cr);
					
					
				}
			}
		}
		if(filterSDate!=null){
			criterions.add(Restrictions.ge("edate", filterSDate));
		}
		if(filterEDate!=null){
			criterions.add(Restrictions.le("sdate", filterEDate));
		}
		Map<String, String> aliasMap = new HashMap<String, String>();
		
		if(username!=null){
			criterions.add(
					Restrictions.or(
					Restrictions.or(
						Restrictions.like("u.username", "%"+username+"%"),
						Restrictions.like("u.firstName", "%"+username+"%")
					),
					Restrictions.like("u.lastName", "%"+username+"%")
					)
				);
			
			aliasMap.put("members", "pu");
			aliasMap.put("pu.user", "u");
		}
		
		if(!includeDeleted){
			criterions.add(Restrictions.ne("st.name", PredefinedNames.PROJSTATUS_DELETED));
			aliasMap.put("status", "st");
		}
		
		CustomHibernateGridDataSource datasrc = new CustomHibernateGridDataSource(session, Project.class);
		datasrc.setAlias(aliasMap);
		datasrc.setCriterions(criterions);
		
		datasrc.prepare(firstResult, (firstResult+maxResult), sortConstraints);
		
		
		return datasrc;
	}
	
	@Override
	public GridDataSource searchProjectsAsDataSource(ProjStatus filterStatus,
			ProjType filterType, School filterSchool, String strY, Integer firstResult, Integer maxResult,
			List<SortConstraint> sortConstraints) {
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		criterions.add(Restrictions.like("id", "%-" + strY + "-%"));
		if(filterStatus!=null){
			criterions.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			criterions.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			criterions.add(Restrictions.eq("school", filterSchool));
		}
		
		CustomHibernateGridDataSource datasrc = new CustomHibernateGridDataSource(session, Project.class);
		
		datasrc.setCriterions(criterions);
		
		datasrc.prepare(firstResult, (firstResult+maxResult), sortConstraints);
		
		
		return datasrc;
	}
	
	@Override
	public GridDataSource searchProjectsAsDataSource(ProjStatus filterStatus, ProjType filterType, School filterSchool, Integer firstResult, Integer maxResult, List<SortConstraint> sortConstraints) {
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		if(filterStatus!=null){
			criterions.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			criterions.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			criterions.add(Restrictions.eq("school", filterSchool));
		}
		
		Map<String, String> aliasMap = new HashMap<String, String>();
		
		CustomHibernateGridDataSource datasrc = new CustomHibernateGridDataSource(session, Project.class);
		datasrc.setAlias(aliasMap);
		datasrc.setCriterions(criterions);
		
		datasrc.prepare(firstResult, (firstResult+maxResult), sortConstraints);
		
		return datasrc;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> searchProjects(ProjStatus filterStatus, ProjType filterType, School filterSchool, Integer firstResult, Integer maxResult) {
		
		Criteria crit = session.createCriteria(Project.class);
		
		if(filterStatus!=null){
			crit = crit.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			crit = crit.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			crit = crit.add(Restrictions.eq("school", filterSchool));
		}
		
		crit.setFirstResult(firstResult);
		crit.setMaxResults(maxResult);
		
		return crit.list();
	}
	
	@Override
	public List<Project> searchProjects(ProjStatus filterStatus, ProjType filterType, School filterSchool) {
		
		Criteria crit = session.createCriteria(Project.class);
		
		if(filterStatus!=null){
			crit = crit.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			crit = crit.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			crit = crit.add(Restrictions.eq("school", filterSchool));
		}
		
		return crit.list();
	}
	@Override
	public List<Project> searchProjects(School searchSchool, String searchTerm,
			String searchCourseCode) {
	Criteria crit = session.createCriteria(Project.class);
		
		if(searchSchool!=null){
			crit = crit.add(Restrictions.eq("school", searchSchool));
		}
		if(searchTerm!=null){
			crit = crit.add(Restrictions.eq("term", searchTerm));
		}
		if(searchCourseCode!=null){
			crit = crit.add(Restrictions.eq("courseCode", searchCourseCode));
		}
		
		return crit.list();
	}
	
/*	
	@Override
	public long countSearchProjects(String filterText, ProjectSearchableField filterIn,
			Date filterSDate, Date filterEDate, ProjStatus filterStatus,
			ProjType filterType, School filterSchool){
		Criteria crit = session.createCriteria(Project.class)
				.add(Restrictions.isNull("deleteDate"));
		
		if(filterStatus!=null){
			crit = crit.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			crit = crit.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			crit = crit.add(Restrictions.eq("school", filterSchool));
		}
		if(filterText!=null){
			String[] words = filterText.split(" ");
			if(filterIn!=null){
				for(String word : words){
					crit = crit.add(Restrictions.like(filterIn.name(), "%"+word+"%"));
				}
			}
			else {
				for(String word : words){
					Criterion cr = null;
					for(int i=0; i<ProjectSearchableField.values().length; i++){
						ProjectSearchableField f = ProjectSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.like(f.name(), "%"+word+"%");
						else
							cr = Restrictions.or(cr, Restrictions.like(f.name(), "%"+word+"%"));
					}
					if(cr!=null)
						crit = crit.add(cr);
				}
			}
		}
		if(filterSDate!=null){
			crit = crit.add(Restrictions.ge("edate", filterSDate));
		}
		if(filterEDate!=null){
			crit = crit.add(Restrictions.le("sdate", filterEDate));
		}
		
		crit.setProjection(Projections.rowCount());	
		int count =(Integer) crit.uniqueResult();
		crit.setProjection(null); //set Criteria back to how it was
		crit.setResultTransformer(Criteria.ROOT_ENTITY);//set Criteria back to how it was
		
		return count; 
	}
*/
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> findProjectsToInactive(int numDays) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_YEAR, -1* numDays);
		
		Query q = session.createQuery("SELECT p FROM Project AS p " +
				" WHERE (p.lastAccess IS NOT NULL AND p.lastAccess < :rDate) "
				+ " AND (p.lastStatusChange IS NULL OR p.lastStatusChange < :rDate)"
				+ " AND p.status.name = :rStatusName "
				+ " AND p.noAutoChangeStatus = false"
				)
				.setTimestamp("rDate", date.getTime())
				.setString("rStatusName", PredefinedNames.PROJSTATUS_ACTIVE);
		
		return q.list();
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> findProjectsToArchive(int numDays) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_YEAR, -1* numDays);
		
		Query q = session.createQuery("SELECT p FROM Project AS p " +
				" WHERE (p.lastStatusChange IS NULL OR p.lastStatusChange < :rDate)"
				+ " AND p.status.name = :rStatusName "
				+ " AND p.noAutoChangeStatus = false"
				)
				.setTimestamp("rDate", date.getTime())
				.setString("rStatusName", PredefinedNames.PROJSTATUS_INACTIVE);
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> findProjectsToActualDelete(int numDays) {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_YEAR, -1* numDays);
		
		Query q = session.createQuery("SELECT p FROM Project AS p " +
				" WHERE (p.lastStatusChange IS NOT NULL AND p.lastStatusChange < :rDate)"
				+ " AND p.status.name = :rStatusName "
				)
				.setTimestamp("rDate", date.getTime())
				.setString("rStatusName", PredefinedNames.PROJSTATUS_DELETED);
		return q.list();
	}

	@Override
	public long countProjectBySchool(School school) {
		Query q =  session.createQuery("SELECT COUNT(p.id) FROM Project AS p " +
				" WHERE p.school=:rSchool " )
				.setParameter("rSchool", school);
		
		return  (Long) q.uniqueResult();
	}

	@Override
	public long countProjectByProjRole(ProjRole projRole) {
		Query q =  session.createQuery("SELECT COUNT(DISTINCT p.id) FROM Project AS p " +
				" LEFT JOIN p.members AS pu " +
				" WHERE pu.role=:rRole " )
				.setParameter("rRole", projRole);

		return  (Long) q.uniqueResult();
	}

	@Override
	public long countProjectByStatus(ProjStatus projStatus) {
		Query q =  session.createQuery("SELECT COUNT(p.id) FROM Project AS p " +
				" WHERE p.status=:rStatus " )
				.setParameter("rStatus", projStatus);

		return  (Long) q.uniqueResult();
	}

	@Override
	public long countProjectByType(ProjType type) {
		Query q =  session.createQuery("SELECT COUNT(p.id) FROM Project AS p " +
				" WHERE p.type=:rType " )
				.setParameter("rType", type);
		
		return  (Long) q.uniqueResult();
	}

	@Override
	public long countProjectByModule(Module module) {
		Query q =  session.createQuery("SELECT COUNT(DISTINCT p.id) FROM Project AS p " +
				" LEFT JOIN p.projmodules AS pm " +
				" WHERE pm.module=:rModule " )
				.setParameter("rModule", module);

		return  (Long) q.uniqueResult();
	}
	@Override
	public ProjectAttachedFile getAttachedFileById(String attachId) {
		return (ProjectAttachedFile) session.get(ProjectAttachedFile.class, attachId);
	}
	@Override
	public ProjUser getProjUserById(int id) {
		return (ProjUser) session.get(ProjUser.class, id);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getProjectsBySchool(School school, Integer firstResult,
			Integer maxResult) {
		Query q =  session.createQuery("SELECT p FROM Project AS p " 
					+ " WHERE p.school=:rSchl ")
					.setParameter("rSchl", school);
		if(firstResult!=null)
			q.setFirstResult(firstResult);
		if(maxResult!=null)
			q.setMaxResults(maxResult);
		
		return q.list();
	}


	@Override
	public List<Project> searchProjects(ProjStatus filterStatus,
			ProjType filterType, School filterSchool, String strY) {
		Criteria crit = session.createCriteria(Project.class).add(Restrictions.like("id", "%-" + strY + "-%"));
		if(filterStatus!=null){
			crit = crit.add(Restrictions.eq("status", filterStatus));
		}
		if(filterType!=null){
			crit = crit.add(Restrictions.eq("type", filterType));
		}
		if(filterSchool!=null){
			crit = crit.add(Restrictions.eq("school", filterSchool));
		}
		
		return crit.list();
	}
	
	


	@Override
	public List<Project> searchProjects(String projectTitle,
			User projectLeader, ProjRole projRole,  Date startDate) {
		Criteria crit = session.createCriteria(Project.class);
		if(projectTitle!=null){
			crit = crit.add(Restrictions.eq("name", projectTitle));
		}
		if(startDate!=null){
			crit = crit.add(Restrictions.eq("sdate", startDate));
		}
		List<Project> pList = crit.list();
		if(projectLeader!=null){
			for(int i = pList.size()-1; i>=0; i--){
				Project p = pList.get(i);
				if(!p.getMembersByRole(projRole).contains(projectLeader)){
					pList.remove(i);
				}
			}
		}
		
		return pList;
	}


	@Override
	public List<String> getAllTerms() {
//		ProjStatus deleted = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_DELETED);
//		ProjStatus archived = projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ARCHIVED);
		
		Query q =  session.createQuery("SELECT DISTINCT p.term FROM Project AS p WHERE length(p.term)=4 ORDER BY p.term ASC " 	);

		return   q.list();

	}


	@Override
	public List<String> getCourseCodeList(School searchSchool, String searchTerm) {
		Query q =  session.createQuery("SELECT DISTINCT p.courseCode FROM Project AS p " 	
				+ " WHERE (p.courseCode IS NOT NULL AND p.courseCode <> '') " 
				+ ((searchSchool!=null)? " AND p.school=:rSchl" : "")
				+ ((searchTerm!=null)? " AND p.term=:rTerm" : "")
				+ " ORDER BY p.courseCode ASC "
			);
				 
		if (searchSchool!=null){
			q.setParameter("rSchl", searchSchool);
		}
		if (searchTerm!=null){
		q.setParameter("rTerm", searchTerm);
		}

		return   q.list();
	}

	//TODO: do proper way, (not put RoleID here directly)
	@Override
	public long countLeaderByProjectID(String projID) {
		Query q =  session.createQuery("SELECT COUNT(pu.id) FROM ProjUser AS pu " +
			" WHERE pu.project.id=:rPID AND pu.role.id = :rID" )
			.setString("rPID", projID)
			.setParameter("rID", 1);

		return  (Long) q.uniqueResult();
	}

	@Override
	public long countStudentByProjectID(String projID) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ids.add(2);
		ids.add(983040);

		Query q =  session.createQuery("SELECT COUNT(pu.id) FROM ProjUser AS pu " +
			" WHERE pu.project.id=:rPID AND pu.role.id IN (:rID)" )
			.setString("rPID", projID)
			.setParameterList("rID", ids);

		return  (Long) q.uniqueResult();
	}


	
}
