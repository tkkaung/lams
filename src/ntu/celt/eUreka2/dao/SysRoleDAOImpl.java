package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SysRoleDAOImpl implements SysRoleDAO {
	@Inject
	private Session session;

	
	@Override
	public void delete(SysRole sysRole) throws DataBeingUsedException {
		session.delete(sysRole);
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<SysRole> getAllSysRoles() {
		return session.createCriteria(SysRole.class).list();
	}
	
	@Override
	public SysRole getSysRoleById(int sysRoleId) {
		return (SysRole) session.get(SysRole.class, sysRoleId);
	}
	@Override
	public SysRole getSysRoleByName(String name) {
		Criteria c = session.createCriteria(SysRole.class).add(
				Restrictions.eq("name", name));

		return (SysRole) c.uniqueResult();
	}
	@Override
	public boolean isRoleNameExist(String name){
		long count = (Long) session.createQuery("SELECT COUNT(*) FROM SysRole WHERE name = :rName")
					.setString("rName", name)
					.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}
	
	@Override
	public void save(SysRole sysRole) throws DuplicateException {
		session.persist(sysRole);
	}


	@Override
	public SysroleUser getSysroleUserById(long id) {
		return (SysroleUser) session.get(SysroleUser.class, id);
	}
	@Override
	public void deleteSysroleUser(SysroleUser sysroleUser){
		session.delete(sysroleUser);
	}
	@Override
	public void immediateDeleteSysroleUser(SysroleUser sysroleUser){
		session.delete(sysroleUser);
	}
}
