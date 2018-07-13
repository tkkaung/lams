package ntu.celt.eUreka2.dao;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;

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

public interface ProjectDAO {
	String generateId(ProjType type, School school, Date sdate);
	String generateId(String prefix);
	List<Project> getAllProjects();
	List<Project> getAllProjects(int firtResult, int maxResult);
	List<Project> getProjectByName(String name);
	List<Project> getProjectsByMember(User user);
	List<Project> getProjectsForHomeByMember(User user);
	GridDataSource getProjectsForHomeByMemberAsDataSource(User user,
			Integer firstResult, Integer maxResult,
			List<SortConstraint> sortConstraints);
	List<Project> getProjectsByCourseId(String courseId);
	List<Project> getProjectsByCourseIdUserId(String courseId, String username);
	List<Project> getProjectsByCourseIdGroupId(String courseId, String groupId);
	ProjUser getProjUserById(int id);
	Project getProjectById(String id);
	void saveProject(Project project);
	void updateProject(Project project);
	@CommitAfter
	void immediateSaveProject(Project project);
	
	void deleteProject(Project project);
	@CommitAfter
	void deleteProjectPermanently(Project project);
	@CommitAfter
	void immediateDeleteProjectPermanently(Project project);
	
	GridDataSource searchProjectsAsDataSource(String filterText, ProjectSearchableField filterIn,
			String username, Date filterSDate, Date filterEDate, ProjStatus filterStatus,
			ProjType filterType, School filterSchool,
			Integer firstResult, Integer maxResult, List<SortConstraint> sortConstraints,
			boolean includeDeleted);
	
	GridDataSource searchProjectsAsDataSource(ProjStatus filterStatus, ProjType filterType, School filterSchool, Integer firstResult, Integer maxResult, List<SortConstraint> sortConstraints);
	GridDataSource searchProjectsAsDataSource(ProjStatus filterStatus, ProjType filterType, School filterSchool, String strY, Integer firstResult, Integer maxResult,
			List<SortConstraint> sortConstraints);
	List<Project> searchProjects(ProjStatus filterStatus, ProjType filterType, School filterSchool, Integer firstResult, Integer maxResult);
	List<Project> searchProjects(ProjStatus filterStatus, ProjType filterType, School filterSchool);
	List<Project> searchProjects(ProjStatus filterStatus, ProjType filterType, School filterSchool, String strY);
	List<Project> searchProjects(String projectTitle, User projectLeader, ProjRole projRole, Date startDate);
	List<Project> searchProjects(School searchSchool, String searchTerm, String searchCourseCode);

	/*
	int countSearchProjects(String filterText, ProjectSearchableField filterIn,
			Date filterSDate, Date filterEDate, ProjStatus filterStatus,
			ProjType filterType, School filterSchool);
	*/
	
	List<Project> findProjectsToInactive(int numDays);
	List<Project> findProjectsToArchive(int numDays);
	List<Project> findProjectsToActualDelete(int numDays);
	List<Project> getProjectsBySchool(School school, Integer firstResult, Integer maxResult);
	long countProjectBySchool(School school);
	long countProjectByProjRole(ProjRole projRole);
	long countProjectByStatus(ProjStatus projStatus);
	long countProjectByType(ProjType type);
	long countProjectByModule(Module module);
	ProjectAttachedFile getAttachedFileById(String attachId);
	List<String> getAllTerms();
	List<String> getCourseCodeList(School searchSchool, String searchTerm);
	
	

	long countLeaderByProjectID(String projID);
	long countStudentByProjectID(String projID);
}
