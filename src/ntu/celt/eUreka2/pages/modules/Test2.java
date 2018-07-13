package ntu.celt.eUreka2.pages.modules;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.UserSearchableField;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
//import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogSearchableField;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.message.MessageType;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailTemplateDAO;
import ntu.celt.eUreka2.services.email.EmailTemplateVariables;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
//import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;


public class Test2
{
	@Property
	private Project curProj;
	//@SessionState
	//private WebSessionData wsData;
	private String projId ;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private Logger logger;
	@SuppressWarnings("unused")
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private RequestGlobals requestGlobals;
	@SuppressWarnings("unused")
	@Inject
	private Response response;
	@SuppressWarnings("unused")
	@Inject
    private BeanModelSource beanModelSource;
    @SuppressWarnings("unused")
	@Inject
    private Messages messages;
	@SuppressWarnings("unused")
	@Inject
	private Request request;
    @SuppressWarnings("unused")
	@Inject
    private PropertyConduitSource propertyConduitSource; 
	@Persist("flash")
	@Property
	private String msg;
	void onActivate(String id) {
		projId = id;
	}
	String onPassivate() {
		return projId;
	}

	public String getModuleName(){
		return "Test";
	}
	
	
	
	void setupRender() throws IOException{
			curProj = projDAO.getProjectById(projId);
			if(curProj==null){
				throw new RecordNotFoundException("ProjectID "+projId+" does not exists.");
			}
			
		//logger.warn("....."+appendSequenceNo("aaa_aaa_ss","_",".ss"));
		
		HttpServletRequest request = requestGlobals.getHTTPServletRequest();
		
		String file = request.getRequestURI();
		if (request.getQueryString() != null) {
		   file += '?' + request.getQueryString();
		}
		URL reconstructedURL = new URL(request.getScheme(),
		                               request.getServerName(),
		                               request.getServerPort(),
		                               file);

		msg = reconstructedURL.toString();
		
		
		
		
		msg = "hhhhhhhhhh";
		
		
		
		
		//Project proj = curProj;
		



//User u = wsData.getUser();
//String searchText = "somsak";



//objList = projDAO.getAllProjects() ;




/*Query q = session.createQuery("SELECT DISTINCT t FROM Task AS t " +
		" LEFT OUTER JOIN t.milestone.schedule AS s" +
		" LEFT OUTER JOIN s.project AS p" +
		" WHERE " +
		"   s.active = true " +
		"   AND p.id = :rPid " +
		"	AND t.percentageDone<>100 " +
		"	AND :rUser in elements(t.assignedPersons) " +
		"   ORDER BY t.startDate "
		)
		.setString("rPid", proj.getId())
		.setParameter("rUser", u)
		;
*/
/*	Query q = session.createQuery("SELECT t FROM Thread AS t "
			+ " LEFT OUTER JOIN t.forum.project AS p "
			+ " JOIN t.threadUsers AS tu "
			+ " WHERE p.id = :rPid "
			+ " AND tu.user = :rUser"
			+ " AND tu.read = false"
			+ " ORDER BY t.modifyDate"
			)
			.setString("rPid", proj.getId())
			.setParameter("rUser", u)
			
			;
*/
/*
SQLQuery q = session.createSQLQuery("SELECT * FROM tbl_forum_thread");
	q.addScalar( "id", Hibernate.LONG);
	q.addScalar( "name", Hibernate.STRING);
	q.addScalar( "message", Hibernate.STRING);

	
	
objList =  q.list();
msg += "....aa="+objList.size() + "....qString="+ q.getQueryString() ;	
for(int i=0; i< objList.size(); i++){
	msg += "<<";
	Object[] o = (Object[]) objList.get(i);
	for(int j=0; j<o.length; j++){
		msg += "..."+ o[j];
	}
	msg += ">>";
}
		
Session session2 = HibernateUtil.getSessionFactory2().getCurrentSession();
session2.beginTransaction();
String userId = "bbtestuser";
SQLQuery q2 = (SQLQuery) session2.createSQLQuery("SELECT * FROM mbcampaign c " 
		+ " JOIN mbmember m ON c.campaign_id = m.roll_id " 
		+ " WHERE m.principal_id = :userId "
		+ " AND m.manager = 1 ")
		.setString("userId", userId);
q2.addScalar( "campaign_id", Hibernate.STRING);
q2.addScalar( "name", Hibernate.STRING);
q2.addScalar( "startDate", Hibernate.LONG);

objList =  q2.list();


msg += "....2aa="+objList.size() + "....qString="+ q2.getQueryString() ;	
System.out.println("....2aa="+objList.size() + "....qString="+ q2.getQueryString());
for(int i=0; i< objList.size(); i++){
	msg += "==";
	Object[] o = (Object[]) objList.get(i);
	for(int j=0; j<o.length; j++){
		if(j==2)
			o[j] = new Date((Long) o[j]);
		msg += "..."+ o[j];
	}
	msg += "==";
}

SQLQuery q3 = (SQLQuery) session2.createSQLQuery("SELECT DISTINCT status FROM mbcampaign " );
q3.addScalar("status", Hibernate.STRING);
objList = q3.list();
for(int i=0; i< objList.size(); i++){
	msg += "[[";
	Object o = objList.get(i);
		msg += "..."+ o ;
	msg += "]]";
}


session2.getTransaction().commit();
	
		
		//objList = session.createCriteria(Announcement.class).list();



EmailTemplate emailTem = emailTemDao.findByTypeAndLanguage(EmailTemplateConstants.DEFAULT, "en");

Announcement annmt =  annmtDao.findById((long) 1);
EmailTemplateVariables  var = new EmailTemplateVariables(
		annmt.getCreateDateDisplay(), annmt.getReleaseDateDisplay(),
		annmt.getSubject(), annmt.getBody(), 
		annmt.getCreator().getDisplayName(),
		"projName", 
		emailManager.createLinkBackURL(req, AnnouncementView.class, (Long) null),
		annmt.isUrgent()? messages.get("urgent") : "", null);


String re = replaceEmailTemplate(emailTem.getContent(), var);
*/
	}
	@SuppressWarnings("unused")
	private String log(String m){
		return msg += m + "\n";
	}
	@SuppressWarnings("unused")
	@Inject 
	private Request req;
	@SuppressWarnings("unused")
	@Inject 
	private EmailTemplateDAO emailTemDao;
	@SuppressWarnings("unused")
	@Inject 
	private EmailManager emailManager;
	@SuppressWarnings("unused")
	@Inject
	private AnnouncementDAO annmtDao;
	
	public String wrapFieldName(String fieldName){
		return "%" + fieldName + "%";
	}
	public String replaceEmailTemplate(String template, EmailTemplateVariables itemVar) {
		for(Field f : itemVar.getClass().getFields() ){
			String name = f.getName();
			try {
				String s = wrapFieldName(name);
				String value = (String) f.get(itemVar);
				if(value==null)
					value = "NULL";
				
//			template = Pattern.compile("(?i)"+ s).matcher(template).quoteReplacement(value); ;
		
				template = template.replaceAll("(?i)"+ s, Matcher.quoteReplacement(value));   //replace with ignorecase
			
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
				e.printStackTrace() ;
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		return template;
	}

	public int dateDiff(Date earlierDate, Date laterDate){
		Date d1 = DateUtils.truncate(earlierDate, Calendar.DATE);
		Date d2 = DateUtils.truncate(laterDate, Calendar.DATE);
		msg += "....d1="+d1.getTime() + "d2="+d2.getTime();
		return (int) ((d2.getTime() - d1.getTime()) / (86400000));  //86400 = 24*60*60*100
	}
	@SuppressWarnings("unchecked")
	public List<ThreadReply> searchThread(String searchText, ThreadReply rootThread, Date startDate,
			Date endDate) {
		List<ThreadReply> threads = rootThread.getDescendants(new ArrayList<ThreadReply>());
		threads.add(rootThread);
		List<Long> threadIds = new ArrayList<Long>();
		for(ThreadReply th : threads){
			threadIds.add(th.getId());
		}
		
		Criteria crit = session.createCriteria(ThreadReply.class)
						.add(Restrictions.in("id", threadIds)  )
						.createAlias("author","Author");
		
		if(searchText!=null){
			String[] words = searchText.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.or(Restrictions.like("name", "%"+word+"%"),
						Restrictions.or(Restrictions.like("message", "%"+word+"%"),
						Restrictions.or(Restrictions.like("Author.firstName", "%"+word+"%"),
						Restrictions.or(Restrictions.like("Author.lastName", "%"+word+"%"),
						Restrictions.like("Author.username", "%"+word+"%" ))))));
			}
		}
		if(startDate!=null){
			crit = crit.add(Restrictions.ge("createDate", startDate));
		}
		if(endDate!=null){
			crit = crit.add(Restrictions.le("createDate", endDate));
		}
		
		return crit.list();
	}
	
	
	
	@Inject
	private Session session;
	@SuppressWarnings("unused")
	@Property
	private List<Object> objList;
	@SuppressWarnings("unused")
	@Property
	private Object obj;
	
		
	@SuppressWarnings("unused")
	@Property
	private String ratingValue;
		
	@SuppressWarnings("unchecked")
	@CommitAfter
	void onActionFromDelete(){
		Query q = session.createQuery("SELECT m FROM Message as m " 
				+ " WHERE m.type = :rType "
				+ " AND m.sendDate < current_date() -4)  "
				)
				.setParameter("rType", MessageType.TRASH)
				;
		List<Message> list = q.list();
		for(Message m : list){
			session.delete(m);
		}
		
		
	}
	@SuppressWarnings("unchecked")
	public List<Blog> searchBlogs(String searchText, BlogSearchableField searchIn,
			Project project, User author, BlogStatus status, Date date,
			Integer firstResult, Integer maxResults, User curUser) {
		
		Criteria crit = session.createCriteria(Blog.class);
		if(searchText!=null){
			String[] words = searchText.split(" ");
			if(searchIn!=null){
				for(String word : words){
					crit = crit.add(Restrictions.like(searchIn.name(), "%"+word+"%"));
				}
			}
			else {
				for(String word : words){
					Criterion cr = null;
					for(int i=0; i<UserSearchableField.values().length; i++){
						UserSearchableField f = UserSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.like(f.name(), "%"+word+"%");
						else
							cr = Restrictions.or(cr, Restrictions.like(f.name(), "%"+word+"%"));
					}
					if(cr!=null)
						crit = crit.add(cr);
				}
			}
		}
		if(date!=null){
			date = DateUtils.truncate(date, Calendar.DATE);
			Calendar nextDate = Calendar.getInstance();
			nextDate.setTime(date);
			nextDate.add(Calendar.DAY_OF_YEAR, 1);
			
			crit = crit.add(Restrictions.ge("cdate",date));
			crit = crit.add(Restrictions.le("cdate",nextDate.getTime()));
		}
		if(author!=null){
			crit = crit.add(Restrictions.eq("author", author));
		}
		if(project!=null){
			crit = crit.add(Restrictions.eq("project.id", project.getId()));
		}
		if(status!=null){
			crit = crit.add(Restrictions.eq("status", status));
		}
		if(firstResult!=null){
			crit.setFirstResult(firstResult);
		}
		if(maxResults!=null){
			crit.setMaxResults(maxResults);
		}

		crit = crit.add(Restrictions.or(Restrictions.eq("shared", true),Restrictions.eq("author", curUser)));
		crit = crit.addOrder(Order.desc("cdate"));
		
		return crit.list();
	}
	
	
	
	
	
	/**
	 * Search for user by several inputs. If any input is NULL, that input will Not be include is search criteria
	 */
	@SuppressWarnings("unchecked")
	public List<User> searchUsers(String searchText, UserSearchableField searchIn,
			Boolean enabled, Integer schoolId, Integer sysRoleId, User curUser) {
		Criteria crit = createCritForSearchUsers(searchText, searchIn, enabled, schoolId, sysRoleId, curUser);
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	public List<User> searchUsers(String searchText, UserSearchableField searchIn,
			Boolean enabled, Integer schoolId, Integer sysRoleId, User curUser, int firstResult, int maxResults) {
		Criteria crit = createCritForSearchUsers(searchText, searchIn, enabled, schoolId, sysRoleId, curUser);
		crit.setFirstResult(firstResult);
		crit.setMaxResults(maxResults);
		return crit.list();
	}
	public int countSearchUsers(String searchText, UserSearchableField searchIn,
			Boolean enabled, Integer schoolId, Integer sysRoleId, User curUser) {
		Criteria crit = createCritForSearchUsers(searchText, searchIn, enabled, schoolId, sysRoleId, curUser);
		crit.setProjection(Projections.rowCount());
		
		return (Integer) crit.uniqueResult();
	}
	private Criteria createCritForSearchUsers(String searchText, UserSearchableField searchIn,
			Boolean enabled, Integer schoolId, Integer sysRoleId, User curUser) {
		Criteria crit = session.createCriteria(User.class);
		if(searchText!=null){
			String[] words = searchText.split(" ");
			if(searchIn!=null){
				for(String word : words){
					crit = crit.add(Restrictions.like(searchIn.name(), "%"+word+"%"));
				}
			}
			else {
				for(String word : words){
					Criterion cr = null;
					for(int i=0; i<UserSearchableField.values().length; i++){
						UserSearchableField f = UserSearchableField.values()[i];
						if(i==0)
							cr = Restrictions.like(f.name(), "%"+word+"%");
						else
							cr = Restrictions.or(cr, Restrictions.like(f.name(), "%"+word+"%"));
					}
					if(cr!=null)
						crit = crit.add(cr);
				}
			}
		}
		if(enabled!=null){
			crit = crit.add(Restrictions.eq("enabled", enabled));
		}
		if(schoolId!=null){
			crit = crit.add(Restrictions.eq("school.id", schoolId));
		}
		if(sysRoleId!=null){
			crit = crit.add(Restrictions.eq("sysRole.id", sysRoleId));
		}
		return crit;
	}
	
	
	
	@CommitAfter
	public void deleteProject(Project proj) {
		proj.setSchool(null);
		proj.setStatus(null);
		
		List<ProjModule> pmList = proj.getProjmodules();
		List<ProjUser> puList = proj.getMembers();
		List<Integer> pmIdList = new ArrayList<Integer>();
		List<Integer> puIdList = new ArrayList<Integer>();
		
		
		for(ProjModule pm : pmList){
			pmIdList.add(pm.getId());
		}
		for(ProjUser pu : puList){
			puIdList.add(pu.getId());
		}
		
		pmList.clear();
		puList.clear();
		session.delete(proj);
		
		Query q = session.createQuery("DELETE FROM ProjModule WHERE id IN (:pmIds)")
				.setParameterList("pmIds", pmIdList);
		q.executeUpdate();
		q = session.createQuery("DELETE FROM ProjUser WHERE id IN (:puIds)")
				.setParameterList("puIds", puIdList);
		q.executeUpdate();
		
	}
	
	
	
	public String formatDateTime(Date date){
		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		
		long diff = now.getTimeInMillis() - cal.getTimeInMillis();
	
		if(diff<10*1000)
			return "a few seconds ago";
		if(diff<60*1000)
			return diff/(1000) +" seconds ago";
		if(diff<3*60*1000)
			return "a few minutes ago";
		if(diff<60*60*1000)
			return diff/(60*1000) +" minutes ago";
		if(diff<2*60*60*1000)
			return "about an hour ago";
		if(diff<24*60*60*1000)
			return diff/(60*60*1000) + " hours ago";
		if(diff<48*60*60*1000)
			if(now.get(Calendar.DAY_OF_YEAR)-cal.get(Calendar.DAY_OF_YEAR)==1 )
				return "yesterday";
		if(now.get(Calendar.YEAR)==cal.get(Calendar.YEAR))
				return new SimpleDateFormat("MMM dd, HH:mm:ss").format(date);
		
		return new SimpleDateFormat("yyyy MMM dd, HH:mm:ss").format(date);
		 
	}
	
	@SuppressWarnings("unchecked")
	public List<ThreadReply> getRecentPosts(Project project, int maxResult) {
		Query q = session.createQuery("SELECT th FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum.project AS p "
				+ " WHERE p.id=:rPid "
				+ " ORDER BY th.modifyDate DESC "
				)
				.setString("rPid", project.getId())
				.setMaxResults(maxResult)
				;
		
		return q.list();
	}
	
	public long getTotalView(Thread thread) {
		Query q = session.createQuery("SELECT SUM(thU.numView) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.id=:rTid "
				)
				.setLong("rTid", thread.getId())
				;
		
		return (Long) q.uniqueResult();
	}
	
	
	
	
	public String getDisplayText(){
		
		Query q = session.createQuery("select u from User as u where u.username = :User")
				.setString("User", "jsomsak");
		
		User u = (User) q.uniqueResult();
		
		String displaytxt = u.getDisplayName();
		
		
		return displaytxt;
	}
	
	
	
	
}
	