package ntu.celt.eUreka2.dao;

import java.util.List;


import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.entities.Privilege;

public interface PrivilegeDAO {
  	List<Privilege> getAllPrivileges();
  	Privilege getPrivilegeById(String privilegeId);
  	List<Privilege> getPrivilegesByType(PrivilegeType type);
  	boolean isPrivilegeIdExist(String Id);
  	void save(Privilege privilege);
	void delete(Privilege privilege);
	
	List<Privilege> searchPrivilege(String searchText, PrivilegeType type);
} 
