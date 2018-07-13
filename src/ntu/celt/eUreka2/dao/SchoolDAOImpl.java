package ntu.celt.eUreka2.dao;

import java.util.List;

import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SchoolNameMap;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SchoolDAOImpl implements SchoolDAO {
	@Inject
    private Session session;
	
	@Override
	public void delete(School school) {
		session.delete(school);	
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<School> getAllSchools() {
		return session.createCriteria(School.class)
				.addOrder(Order.asc("name"))
				.list();
	}
	
	@Override
	public School getSchoolById(int schoolId) {
		return (School) session.get(School.class, schoolId);
	}
	
	@Override
	public School getSchoolByName(String name) {
		if(name==null)
			return null;
		Criteria c = session.createCriteria(School.class)
					.add(Restrictions.eq("name", name));
		
		return (School) c.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public School getFirstSchoolByDescription(String des) {
		String mappedDes = this.getSchoolMappedName(des);
		if(mappedDes!=null)
			des = mappedDes;
		
		Criteria c = session.createCriteria(School.class)
					.add(Restrictions.eq("des", des));
		List<School> caList = c.list();
		
		if(caList.isEmpty())
			return null;
		return caList.get(0);
	}
	@SuppressWarnings("unchecked")
	@Override
	public School getFirstSchoolByDescriptionWithoutDuplicateCheck(String des) {
		Criteria c = session.createCriteria(School.class)
					.add(Restrictions.eq("des", des));
		List<School> caList = c.list();
		
		if(caList.isEmpty())
			return null;
		return caList.get(0);
	}
	@Override
	public void save(School school) {
		session.persist(school);
	}
	@Override
	public void immediateSave(School school) {
		session.persist(school);
	}
	@Override
	public boolean isSchoolNameExist(String name) {
		long count = (Long) session.createQuery("SELECT COUNT(*) FROM School WHERE name = :rName")
				.setString("rName", name)
				.uniqueResult();
		if(count == 0)
			return false;
		else 
			return true;
	}
	@SuppressWarnings("unchecked")
	public List<School> searchSchools(String searchText){
		Criteria crit = session.createCriteria(School.class);
		
		if(searchText != null){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("name", "%"+word+"%"),
						Restrictions.like("des", "%"+word+"%" )));
			}
		}
		
		return crit.list();
	}

	@Override
	public String getNextDefaultName(String prefixText) {
		Query q = session.createQuery("SELECT s FROM School AS s " 
				+ " WHERE s.name LIKE :rName"
				+ " ORDER BY s.name DESC "
				)
				.setString("rName", prefixText+"%")
				.setMaxResults(1)
				;
		School s = (School) q.uniqueResult();  //get only the latest version
		int num = 1;
		
		if(s!=null ){ 
			String numStr = s.getName().substring(prefixText.length()); //get the number part (e.g. "Dept_2" will get "2")
			try{
				num = Integer.parseInt(numStr);
				num++;
			}
			catch(NumberFormatException ex){
				System.out.println(ex.getMessage());
			}
		}
		
		return prefixText + num;
		
	}

	@Override
	public String getSchoolMappedName(String nameFrom) {
		Query q = session.createQuery("SELECT n FROM SchoolNameMap n " 
				+ " WHERE n.nameFrom=:nf")
				.setString("nf", nameFrom);
					
		SchoolNameMap snm = (SchoolNameMap) q.uniqueResult();
		
		if(snm!=null)
			return snm.getNameTo();
		return null;
	}

	@Override
	public void saveOrUpdateNameMap(SchoolNameMap schlNameMap) {
		Query q = session.createQuery("SELECT n FROM SchoolNameMap n " 
				+ " WHERE n.nameFrom=:nf")
				.setString("nf", schlNameMap.getNameFrom());
					
		SchoolNameMap snm = (SchoolNameMap) q.uniqueResult();
		
		if(snm!=null){
			snm.setNameTo(schlNameMap.getNameTo());
			snm.setNameFrom(schlNameMap.getNameFrom());
			session.merge(snm);
		}
		else{
			session.save(schlNameMap);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SchoolNameMap> getAllSchoolMappedName() {
		Query q = session.createQuery("SELECT n FROM SchoolNameMap n ");
		
		return q.list();
	}
	
}