package ntu.celt.eUreka2.modules.elog;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;

public interface ElogDAO extends GenericModuleDAO{
	List<Elog> getElogsByProject(Project project);
	List<Elog> searchElogs(String searchText, ElogSearchableField searchIn,
			Project project, User author, ElogStatus status, Boolean published, Date sdate, Date edate,
			Integer firstResult, Integer maxResults, User curUser);  //if the value is null, means ignore check in that field
	List<Elog> getActiveElogs(Project proj, User u);
	
	Elog getElogById(String id);
	@CommitAfter
	void addElog(Elog elog);
	void updateElog(Elog elog);
	void deleteElog(Elog elog);
	
	
	ElogComment getElogCommentById(int id);
	void updateComment(ElogComment comment);
	
	ElogFile getElogFileById(String id);
	
	long countElogs(Project proj);
	long countElogComments(Project proj);
	long countElogs(Project proj, User creator, User user);
	long countElogComments(Project proj, User creator);
	List<Elog> getElogs(Project proj, User creator, User user);
	List<ElogComment> getElogComments(Project proj, User creator);
}
