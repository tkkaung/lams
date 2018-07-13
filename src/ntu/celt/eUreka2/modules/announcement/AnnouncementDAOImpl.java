package ntu.celt.eUreka2.modules.announcement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.announcement.AnnouncementView;

public class AnnouncementDAOImpl implements AnnouncementDAO {
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public AnnouncementDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}

	
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_ANNOUNCEMENT;
	}
	
	@Override
	public void delete(Announcement announcement) {
		session.delete(announcement);
	}
	@Override
	public void save(Announcement announcement) {
		session.persist(announcement);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Announcement> getAllAnnouncements() {
		return session.createCriteria(Announcement.class).list();
	}

	@Override
	public Announcement getAnnouncementById(Long id) {
		if(id==null) return null;
		return (Announcement) session.get(Announcement.class, id);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Announcement> searchAnnouncements(Project project,	User creator, String subject) {
		Criteria crit = session.createCriteria(Announcement.class);
		if(project!=null){
			crit = crit.add(Restrictions.eq("project.id", project.getId()));
		}
		if(creator!=null){
			crit = crit.add(Restrictions.eq("creator", creator));
		}
		if(subject!=null){
			String[] words = subject.split(" ");
			for(String word : words){
				crit = crit.add(Restrictions.like("subject", "%"+word+"%"));
			}
		}
		crit.addOrder(Order.desc("modifyDate"));
		
		return crit.list();
	}


	@Override
	public List<Announcement> getActiveAnnouncements(Project project, User curUser) {
		return getActiveAnnouncements(project, null, curUser);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Announcement> getActiveAnnouncements(Project project, Integer maxResult, User curUser) {
		Query q = session.createQuery("SELECT a FROM Announcement AS a " +
				" WHERE " +
				"	a.project.id = :rPid " +
				"   AND a.enabled = true " +
				"   AND NOT(:rUser IN elements(a.hasReadUsers)) " +
				"   AND (a.endDate is null OR a.endDate>= :rToday) " +
				"   AND (a.startDate<=:rToday) " +
				" 	ORDER BY a.urgent DESC, a.startDate DESC, a.modifyDate DESC ")
				.setString("rPid", project.getId())
				.setParameter("rUser", curUser)
				.setTimestamp("rToday", new Date())
				;
				if(maxResult!=null)
					q.setMaxResults(maxResult);
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Announcement> getDismissedAnnouncements(Project project, Integer maxResult, User curUser) {
		Query q = session.createQuery("SELECT a FROM Announcement AS a " +
				" WHERE " +
				"	a.project.id = :rPid " +
				"   AND a.enabled = true " +
				"   AND :rUser IN elements(a.hasReadUsers) " +
				"   AND (a.endDate is null OR a.endDate>= :rToday) " +
				"   AND (a.startDate<=:rToday) " +
				" 	ORDER BY a.urgent DESC, a.startDate DESC, a.modifyDate DESC ")
				.setString("rPid", project.getId())
				.setParameter("rUser", curUser)
				.setTimestamp("rToday", new Date())
				;
				if(maxResult!=null)
					q.setMaxResults(maxResult);
		
		return q.list();
	}
	

	@Override
	public long countPastUpdated(Project project,  int numOfPastDays) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numOfPastDays);
		
		Query q = session.createQuery("SELECT COUNT(DISTINCT a.id) FROM Announcement AS a " +
				" WHERE a.project.id=:rPid " +
				"   AND a.enabled = true " +
				"   AND (a.endDate is null OR a.endDate>= :rToday) " +
				"   AND ( a.startDate<=:rToday) " +
				"   AND a.modifyDate>=:rPastDay " +
				" 	 ")
				.setString("rPid", project.getId())
				.setTimestamp("rToday", new Date())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		return (Long) q.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT a FROM Announcement AS a " +
				" WHERE a.project.id=:rPid " +
				"   AND a.enabled = true " +
				"   AND (a.endDate is null OR a.endDate>= :rToday) " +
				"   AND ( a.startDate<=:rToday) " +
				"   AND a.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setTimestamp("rToday", new Date())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Announcement> aList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Announcement a : aList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(a.getModifyDate());
			l.setTitle(a.getSubject());
			l.setUrl(linkSource.createPageRenderLinkWithContext(AnnouncementView.class, a.getId()).toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}


	@Override
	public long countAnnouncements(Project proj) {
		Query q = session.createQuery("SELECT count(a) FROM Announcement AS a " +
				" WHERE a.project.id=:rPid ")
				.setString("rPid", proj.getId());
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long countAnnouncements(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(a) FROM Announcement AS a " +
				" WHERE a.project.id=:rPid " +
				" AND a.creator=:rCreator ")
			.setString("rPid", proj.getId())
			.setParameter("rCreator", creator);

		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Announcement> getAnnouncements(Project proj, User creator) {
		Query q = session.createQuery("SELECT a FROM Announcement AS a " +
				" WHERE a.project.id=:rPid " +
				" AND a.creator=:rCreator ")
			.setString("rPid", proj.getId())
			.setParameter("rCreator", creator);

		return q.list();
	}

}
