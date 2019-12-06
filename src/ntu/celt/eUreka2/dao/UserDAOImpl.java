package ntu.celt.eUreka2.dao;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.data.CustomHibernateGridDataSource;
import ntu.celt.eUreka2.data.FilterType;
import ntu.celt.eUreka2.data.UserSearchableField;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;


public class UserDAOImpl implements UserDAO {
	//@Inject
    private Session session;
	@SuppressWarnings("unused")
	private Logger logger ;
	
	
	
	public UserDAOImpl(Session session, Logger logger) {
		super();
		this.session = session;
		this.logger = logger;
	}

	@Override
	public void delete(User user) {
		session.delete(user);
	}

	@Override
	public User getUserById(int id) {
		return (User) session.get(User.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getAllUsers() {
		return session.createCriteria(User.class).list() ;
	}
	
	@Override
	public User getUserByUsername(String username) {
		if(username==null || username.isEmpty() || username.equals("."))
			return null;
		
		List uList = session.createCriteria(User.class)
			.add(Restrictions.eq("username", username))
			.list();
		if(uList.size()>0)
			return (User) uList.get(0);
		return null;
	}
	
	public List<User>  getUsersByUsernames(String usernames) { // usernames = 'username1','username' by Kanesh
		
		 if(usernames==null || usernames.isEmpty() || usernames.equals("."))
		 
			return null;
		/*
		String sqlNative = "select username from tbl_user where username ='" +username +"' ";
		Query qNative = session.createQuery(sqlNative);
		
		 try {
	            String url = "jdbc:msql://localhost:3306/eureka2";
	            Connection conn = (Connection) DriverManager.getConnection(url,"eureka","eureka_20");
	            Statement stmt = (Statement) conn.createStatement();
	            ResultSet rs;
	 
	            rs = stmt.executeQuery("select username from tbl_user where username ='" +username +"' ");
	            while ( rs.next() ) {
	                String lastName = rs.getString("id");
	                System.out.println(lastName);
	            }
	            conn.close();
	        } catch (Exception e) {
	            System.err.println("Got an exception! ");
	            System.err.println(e.getMessage());
	        }
*/

		String sqlNative = "SELECT u FROM User AS u  WHERE username IN ("+usernames+")";
		Query qNative = session.createQuery(sqlNative);
		return qNative.list();
	}
		
	/*@Override
	public User getUserByExKey(String externalKey) {
		if(externalKey==null || externalKey.isEmpty() || externalKey.equals("."))
			return null;
		
		return (User) session.createCriteria(User.class)
			.add(Restrictions.eq("externalKey", externalKey))
			.uniqueResult();
	}
	*/
	@Override
	public User getUserByExKey(String externalKey) {
		if(externalKey==null || externalKey.isEmpty() || externalKey.equals("."))
			return null;
		
		Query q =  session.createQuery("SELECT u FROM User AS u " 
				+ " WHERE externalKey=:rExternalKey "
				)
			.setString("rExternalKey", externalKey)
			.setMaxResults(1);
		return (User) q.uniqueResult();
	}

	@Override
	public void save(User user) {
		session.persist(user);
		session.flush();
	}
	@Override
	public boolean isUsernameExist(String username) {
		Query q =  session.createQuery("SELECT COUNT(*) FROM User WHERE username=:rUsername")
					.setString("rUsername", username);
		long count = (Long) q.uniqueResult();
		if(count==0)
			return false;
		else
			return true;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUserByUsernameOrEmail(String usernameEmail){
		Query q =  session.createQuery("SELECT u FROM User AS u " 
				+ " WHERE username=:rUsername "
				+ " OR email=:rEmail "
				)
			.setString("rUsername", usernameEmail)
			.setString("rEmail", usernameEmail);
		return q.list();
		
	}

	
	/**
	 * Search for user by several inputs. If any input is NULL, that input will Not be include is search criteria
	 */
	@Override
	public GridDataSource searchUsersAsDataSource(FilterType filterType, String searchText
			, UserSearchableField searchIn, Boolean enabled, School school, SysRole sysRole
  			, Integer firstResult, Integer maxResult, List<SortConstraint> sortConstraints) {
		List<Criterion> criterions = createCriterionList(filterType, searchText, searchIn, enabled, school, sysRole);
		
		CustomHibernateGridDataSource datasrc = new CustomHibernateGridDataSource(session, User.class);
		datasrc.setCriterions(criterions);
		datasrc.prepare(firstResult, (firstResult+maxResult), sortConstraints);
		return datasrc;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<User> searchUsers(FilterType filterType, String searchText, UserSearchableField searchIn,
			Boolean enabled, School school, Integer firstResult, Integer maxResult) {
		List<Criterion> criterions = createCriterionList(filterType, searchText, searchIn, enabled, school, null);
		
		Criteria crit = session.createCriteria(User.class);
		for(Criterion cr : criterions){
			crit.add(cr);
		}
		if(firstResult!=null)
			crit.setFirstResult(firstResult);
		if(maxResult!=null)
			crit.setMaxResults(maxResult);
		
		return crit.list();
	}
	@Override
	public int countSearchUsers(FilterType filterType, String searchText, UserSearchableField searchIn,
			Boolean enabled, School school) {
		List<Criterion> criterions = createCriterionList(filterType, searchText, searchIn, enabled, school, null);
		Criteria crit = session.createCriteria(User.class);
		for(Criterion cr : criterions){
			crit.add(cr);
		}
		
		crit.setProjection(Projections.rowCount());	
		long count =(Long) crit.uniqueResult();
		crit.setProjection(null); //set Criteria back to how it was
		crit.setResultTransformer(Criteria.ROOT_ENTITY);//set Criteria back to how it was
		
		return (int) count; 
	}
	private List<Criterion> createCriterionList(FilterType filterType, String searchText
			, UserSearchableField searchIn,Boolean enabled, School school, SysRole sysRole) {
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		if(searchText!=null){
			if(filterType==null)
				filterType = FilterType.CONTAIN;
			
			if(searchIn!=null){
				switch(filterType){
				case CONTAIN:
					String[] words = searchText.split(" ");
					for(String word : words){
						criterions.add(Restrictions.like(searchIn.name(), "%"+word+"%"));
					}
					break;
				case START_WITH:
					criterions.add(Restrictions.like(searchIn.name(), searchText+"%"));
					break;
				case EXACT_WORD:
					criterions.add(Restrictions.eq(searchIn.name(), searchText));
					break;
				}
			}
			else {
				Criterion cr = null;
				switch(filterType){
				case CONTAIN:
					String[] words = searchText.split(" ");
					for(String word : words){
						cr = null;
						for(int i=0; i<UserSearchableField.values().length; i++){
							UserSearchableField f = UserSearchableField.values()[i];
							if(i==0)
								cr = Restrictions.like(f.name(), "%"+word+"%");
							else
								cr = Restrictions.or(cr, Restrictions.like(f.name(), "%"+word+"%"));
						}
						if(cr!=null)
							criterions.add(cr);
					}
					break;
				case START_WITH:
					cr = null;
					for(int i=0; i<UserSearchableField.values().length; i++){
						UserSearchableField f = UserSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.like(f.name(), searchText+"%");
						else
							cr = Restrictions.or(cr, Restrictions.like(f.name(), searchText+"%"));
					}
					if(cr!=null)
						criterions.add(cr);
					break;
				case EXACT_WORD:
					cr = null;
					for(int i=0; i<UserSearchableField.values().length; i++){
						UserSearchableField f = UserSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.eq(f.name(), searchText);
						else
							cr = Restrictions.or(cr, Restrictions.eq(f.name(), searchText));
					}
					if(cr!=null)
						criterions.add(cr);
					break;
				}
			}
		}
		if(enabled!=null){
			criterions.add(Restrictions.eq("enabled", enabled));
		}
		if(school!=null){
			criterions.add(Restrictions.eq("school", school));
		}
		if(sysRole!=null){
			criterions.add(
					Restrictions.eq("sysRole", sysRole)
					
				/*	Restrictions.or(
					Restrictions.eq("sysRole", sysRole),
//					Restrictions.sqlRestriction("? IN (SELECT e.sysRole FROM SysroleUser e WHERE e.user=id )", sysRole, Hibernate.OBJECT)
					//Restrictions.eq("exRole.sysRole", sysRole)
					Subqueries.exists( 
							DetachedCriteria.forClass(SysroleUser.class,"sroleUser")
							.add(Restrictions.eq("sroleUser.sysRole", sysRole))
							.createAlias("sroleUser.user", "srUser")
							//.add(Property.forName("srUser.id" ).eqProperty("{alias}.id"))
							.add(Restrictions.sqlRestriction("srUser.id = {alias}.id"))
							//.add(Property.forName("sroleUser.user" ).eqProperty("usr1.id"))
							//.setProjection(Projections.property("sroleUser.sysRole"))
							)

//					Restrictions.sqlRestriction("? IN elements(extraRoles) ", sysRole.getId(), Hibernate.INTEGER )
					)
					*/
				);
		}
		
		return criterions;
	}

	@Override
	public void saveBatch(List<User> users) {
			for(User u : users){
				session.saveOrUpdate(u);
			}
			session.flush();
			session.clear();
		//	tx.commit(); //comment this line because @commitAfter is used
		
	}

	@Override
	public long countUserBySysRole(SysRole role) {
		Query q =  session.createQuery("SELECT COUNT(DISTINCT u.id) FROM User AS u " +
				" LEFT JOIN u.extraRoles AS t " +
				" WHERE u.sysRole=:rSysRole " +
				" OR t.sysRole=:rSysRole ")
				.setParameter("rSysRole", role);
		
		return  (Long) q.uniqueResult();
	}
	@Override
	public long countUserBySchool(School school) {
		Query q =  session.createQuery("SELECT COUNT(u.id) FROM User AS u " +
				" WHERE u.school=:rSchool " )
				.setParameter("rSchool", school);
		
		return  (Long) q.uniqueResult();
	}
	
	@Override
	public long countUserByProjRole(ProjRole projRole) {
		Query q =  session.createQuery("SELECT COUNT(pu.id) FROM ProjUser AS pu " +
				" WHERE pu.role=:rRole " )
				.setParameter("rRole", projRole);

		return  (Long) q.uniqueResult();
	}
}
