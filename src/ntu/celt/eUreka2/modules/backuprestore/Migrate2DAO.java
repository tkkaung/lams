package ntu.celt.eUreka2.modules.backuprestore;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;


public interface Migrate2DAO {

	Project getProjectById(String projId);
	
	@CommitAfter
	User getOrImportUser(User userFrom);
	@CommitAfter
	School getOrImportSchool(School school);
	@CommitAfter
	Project importProjInfo(Project projFrom, Project projTo);
	@CommitAfter
	void importProjModules(Project projFrom, Project projTo);
	
	
	
}
