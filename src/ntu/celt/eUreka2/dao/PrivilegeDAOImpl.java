package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.data.PrivilegeType;
import ntu.celt.eUreka2.entities.Privilege;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PrivilegeDAOImpl implements PrivilegeDAO {
	@Inject
	private Session session;

	@Override
	@SuppressWarnings("unchecked")
	public List<Privilege> getAllPrivileges() {
		return session.createCriteria(Privilege.class)
				.list();
	}
	
	@Override
	public Privilege getPrivilegeById(String privilegeId) {
		if(privilegeId==null) return null;
		return (Privilege) session.get(Privilege.class, privilegeId);
	}
	@Override
	public boolean isPrivilegeIdExist(String privilegeId) {
		if(privilegeId==null) return false;
		Privilege priv = (Privilege) session.get(Privilege.class, privilegeId);
		if(priv==null)
			return false;
		return true;
	}

	@Override
	public void save(Privilege privilege) {
		session.persist(privilege);
	}
	@Override
	public void delete(Privilege privilege) {
		session.delete(privilege);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<Privilege> getPrivilegesByType(PrivilegeType type) {
		return session.createCriteria(Privilege.class)
				.add(Restrictions.eq("type", type))
				.list();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Privilege> searchPrivilege(String searchText, PrivilegeType type) {
		Criteria crit = session.createCriteria(Privilege.class);
		
		if(searchText != null){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("id", "%"+word+"%")
						,Restrictions.or(Restrictions.like("name", "%"+word+"%" )
						,Restrictions.like("description", "%"+word+"%" )
						)));
			}
		}
		if(type!=null){
			crit = crit.add(Restrictions.eq("type", type));
		}
		
		return crit.list();
	}
}
