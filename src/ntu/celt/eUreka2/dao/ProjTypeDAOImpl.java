package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.entities.ProjType;

public class ProjTypeDAOImpl implements ProjTypeDAO {
	@Inject
	private Session session;

	@Override
	public void addType(ProjType type) {
			session.save(type);
	}

	@Override
	public void deleteType(ProjType type) {
		session.delete(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjType> getAllTypes() {
		return  session.createQuery("from ProjType order by name").list();
	}

	@Override
	public ProjType getTypeById(int id) {
		return (ProjType) session.get(ProjType.class, id);
	}

	@Override
	public ProjType getTypeByName(String name) {
		return (ProjType) session.createCriteria(ProjType.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}
	@Override
	public boolean isTypeNameExist(String name) {
		Query q = session.createQuery("SELECT COUNT(*) FROM ProjType WHERE name = :rName")
			.setString("rName", name);
		long count = (Long) q.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}
	
	@Override
	public void updateType(ProjType type) {
		session.persist(type);
	}

}
