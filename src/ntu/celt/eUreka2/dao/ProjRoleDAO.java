package ntu.celt.eUreka2.dao;

import java.util.List;


import ntu.celt.eUreka2.entities.ProjRole;

public interface ProjRoleDAO {
	List<ProjRole> getAllRole();
	ProjRole getRoleById(int id);
	ProjRole getRoleByName(String name);
	boolean isRoleNameExist(String name);
	void updateRole(ProjRole role);
	void deleteRole(ProjRole role);
}
