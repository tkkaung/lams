package ntu.celt.eUreka2.dao;

import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.entities.Module;

public class ModuleDAOImpl implements ModuleDAO {
	@Inject
	private Session session;

	@Override
	public boolean isModuleNameExists(String name) {
		Query q = session.createQuery("select count(*) from Module WHERE name = :rName")
					.setString("rName", name);
		
		long count = (Long) q.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}


	public void deleteModule(Module module) {
		session.delete(module);
	}

	@SuppressWarnings("unchecked")
	public List<Module> getAllModules() {
		return session.createQuery("FROM Module ORDER BY name").list();
	}

	
	
	public Module getModuleById(int id) {
		Module module = (Module) session.get(Module.class, id);
		return module;
	}

	public Module getModuleByName(String name) {
		if(name==null)
			return null;
		return (Module) session.createCriteria(Module.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}

	public void updateModule(Module module) {
		session.persist(module);
	}


	

}
