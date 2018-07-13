package ntu.celt.eUreka2.modules.backuprestore;

import java.util.List;


import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.backuprestore.MigratedProjInfo;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MProject;
import ntu.celt.eUreka2.modules.backuprestore.migrateEnities.MUser;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


public interface MigrateDAO {
	
	
	public long getCountProjs(String status);
	public List<String> getProjIDsByStatus(String status, Integer firstResult, Integer maxResult);
	public List<MProject> getProjectsByUser(String username);	
	public long getCountSysRoles();
	public long getCountProjRoles();
	public long getCountProjStatuss();
	public long getCountProjTypes();
	public long getCountSchools();
	public long getCountUsers();
	@CommitAfter
	public ResultSummary importAllSysRoles(boolean replaceIfExist);
	@CommitAfter
	public ResultSummary importAllProjRoles(boolean replaceIfExist);
	@CommitAfter
	public ResultSummary importAllProjStatus(boolean replaceIfExist);
	@CommitAfter
	public ResultSummary importAllProjTypes(boolean replaceIfExist);
	@CommitAfter
	public ResultSummary importAllSchools(boolean replaceIfExist);
	@CommitAfter
	public ResultSummary importAllUsers(boolean replaceIfExist);
	@CommitAfter
	public Project importProjInfo(String mProjId);
	@CommitAfter
	public Project importProjMembers(String mProjId, Project toProject);
	@CommitAfter
	public ResultSummary importAnnouncements(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importActivityTasks(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importFiles(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importDiscussionBoards(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importWebBlogs(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importLinks(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importBudgets(String mProjId, Project proj);
	@CommitAfter
	public ResultSummary importAssesments(String mProjId, Project proj);
	public MUser getMUser(String userId);
	public User importUser( String userId, boolean replaceIfExist);
	@CommitAfter
	public School importSchool( String school_id, boolean replaceIfExist);
	@CommitAfter
 	public Project assignModuleToProject(String moduleName, Project proj);
	public void saveMigratedProjInfo(MigratedProjInfo migratedProjInfo);
	public MigratedProjInfo getMigratedProjInfo(String eureka2ProjId);
	@CommitAfter
	public void deleteMigratedProjInfo(MigratedProjInfo migratedProjInfo);
	public MigratedProjInfo getMigratedProjInfoByVer1ProjId(String ver1ProjId);
	
	
	public boolean hasItemsToMigrate(String mProjId);
	
	public long countAnnouncementToMigrate(String mProjId);
	public long countActivitiesToMigrate(String mProjId);
	public long countFileToMigrate(String mProjId);
	public long countDiscussionToMigrate(String mProjId);
	public long countWebblogToMigrate(String mProjId);
	public long countLinkToMigrate(String mProjId);
	
	
}
