package ntu.celt.eUreka2.modules.learninglog;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface LearningLogDAO extends GenericModuleDAO {
	LogEntry getLogEntryById(Long id);
  	void saveLogEntry(LogEntry entry) ;
  	void deleteLogEntry(LogEntry entry) ;
 	
  	LLogFile getLLogFileById(String id);
  	@CommitAfter
  	void saveLLogFile(LLogFile file);
  	
  	List<String> getLogEntryTypes(User user);
  	
  	/**
  	 * if any parameter is null, means ignore that field
  	 */
  	List<LogEntry> searchLogEntries(User user, Project proj, String type, String filterText);
	long countLlogs(Project proj);
	
	long countForumReflections(long threadId, User curUser);
 	List<LogEntry> getForumReflections(long threadId, User curUser);
 	long countForumReflections(Project proj, User user, User curUser);
	List<LogEntry> getForumReflections(Project proj, User user, User curUser);
 	long countBlogReflections(String blogId, User curUser);
 	List<LogEntry> getBlogReflections(String blogId, User curUser);
 	long countBlogReflections(Project proj, User creator, User curUser);
	List<LogEntry> getBlogReflections(Project proj, User creator, User curUser);
	long countElogReflections(String elogId, User curUser);
 	List<LogEntry> getElogReflections(String elogId, User curUser);
 	long countElogReflections(Project proj, User creator, User curUser);
	List<LogEntry> getElogReflections(Project proj, User creator, User curUser);
 	
  	
}
