package ntu.celt.eUreka2.dao;

import ntu.celt.eUreka2.entities.Preference;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;

public class PreferenceDAOImpl implements PreferenceDAO {
	@Inject
    private Session session;

	@Override
	public Preference getPreferenceById(int preferrenceId) {
		return (Preference) session.get(Preference.class, preferrenceId);
	}

	@Override
	public void immediateSave(Preference preference)  {
			//session.persist(preference);
			session.save(preference);
			session.flush();
			
	}
	
	@Override
	public Preference getPreferenceByUserId(int userId) {
		Query q = session.createQuery("from Preference as p " +
				" where p.user.id= :rId ")
				.setInteger("rId",	userId)
				;
		
		return (Preference) q.uniqueResult();
	}

	
	
}
