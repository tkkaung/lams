package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.entities.ProjStatus;

public class ProjStatusDAOImpl implements ProjStatusDAO {
	@Inject
	private Session session;


	public void deleteStatus(ProjStatus status) {
		session.delete(status);
	}

	@SuppressWarnings("unchecked")
	public List<ProjStatus> getAllStatus() {
		return session.createQuery("from ProjStatus order by name").list();
	}

	public ProjStatus getStatusById(int id) {
		return (ProjStatus) session.get(ProjStatus.class, id);
	}

	public ProjStatus getStatusByName(String name) {
		return (ProjStatus) session.createCriteria(ProjStatus.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}

	public boolean isStatusNameExist(String name) {
		Query q = session.createQuery("SELECT COUNT(*) FROM ProjStatus WHERE name = :rName")
			.setString("rName", name);
		long count = (Long) q.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}

	public void updateStatus(ProjStatus status) {
		session.persist(status);
	}
}
