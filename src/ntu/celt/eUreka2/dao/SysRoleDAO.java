package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;


import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;

public interface SysRoleDAO {
	List<SysRole> getAllSysRoles();
  	SysRole getSysRoleById(int sysRoleId);
  	void save(SysRole sysRole) throws DuplicateException;
  	void delete(SysRole sysRole) throws DataBeingUsedException;
  	SysRole getSysRoleByName(String name);
  	boolean isRoleNameExist(String name);
  	
  	SysroleUser getSysroleUserById(long id);
  	void deleteSysroleUser(SysroleUser sysroleuser);
  	@CommitAfter
  	void immediateDeleteSysroleUser(SysroleUser sysroleuser);
}
