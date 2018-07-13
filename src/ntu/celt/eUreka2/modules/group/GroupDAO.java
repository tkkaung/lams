package ntu.celt.eUreka2.modules.group;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.entities.Project;

public interface GroupDAO extends GenericModuleDAO {
  	Group getGroupById(Long id);
  	@CommitAfter
  	void saveGroup(Group group) ;
  	@CommitAfter
  	void deleteGroup(Group group) ;
  	List<Group> getGroupsByProject(Project project); 
  	List<Group> getAllGroups();
  	
  	GroupUser getGroupUserById(Long id);
  	void saveGroupUser(GroupUser groupUser);
  	@CommitAfter
  	void immediateSaveGroupUser(GroupUser groupUser);
  	
  	boolean isGroupBeingUsePE(Group group);
  	boolean isGroupBeingUseAS(Group group);
	Group getGroupByBBID(String bbID);
  	
  	
} 
