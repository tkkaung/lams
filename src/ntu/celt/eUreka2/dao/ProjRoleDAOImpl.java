package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.entities.ProjRole;

public class ProjRoleDAOImpl implements ProjRoleDAO {
	@Inject
	private Session session;

	public void deleteRole(ProjRole role) {
		session.delete(role);
	}

	@SuppressWarnings("unchecked")
	public List<ProjRole> getAllRole() {
		List<ProjRole> roles = session.createQuery("from ProjRole order by name").list();
		return roles;
	}

	public ProjRole getRoleById(int id) {
		return (ProjRole) session.get(ProjRole.class, id);
	}

	public ProjRole getRoleByName(String name) {
		return (ProjRole) session.createCriteria(ProjRole.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}

	public boolean isRoleNameExist(String name) {
		Query q = session.createQuery("SELECT COUNT(*) FROM ProjRole WHERE name = :rName")
			.setString("rName", name);
		long count = (Long) q.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}

	public void updateRole(ProjRole role) {
		session.persist(role);
	}

}
