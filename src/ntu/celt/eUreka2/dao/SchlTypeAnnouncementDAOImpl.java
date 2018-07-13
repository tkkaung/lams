package ntu.celt.eUreka2.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SchlTypeAnnouncement;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.announcement.SchlTypeAnnmtView;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SchlTypeAnnouncementDAOImpl implements SchlTypeAnnouncementDAO {
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public SchlTypeAnnouncementDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}
	
	@Override
	public void delete(SchlTypeAnnouncement sysAnnmt) {
		session.delete(sysAnnmt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SchlTypeAnnouncement> getAllSchlTypeAnnouncements() {
		return session.createCriteria(SchlTypeAnnouncement.class)
			.addOrder(Order.desc("mdate"))
			.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<SchlTypeAnnouncement> getSchoolAnnouncements(School schl) {
		return session.createCriteria(SchlTypeAnnouncement.class)
			.add(Restrictions.eq("school", schl))
			.addOrder(Order.desc("mdate"))
			.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<SchlTypeAnnouncement> getTypeAnnouncements(ProjType type) {
		return session.createCriteria(SchlTypeAnnouncement.class)
			.add(Restrictions.eq("projType", type))
			.addOrder(Order.desc("mdate"))
			.list();
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SchlTypeAnnouncement> getActiveSchlTypeAnnouncements(Project proj, User curUser) {
		Query q = session.createQuery("SELECT a FROM SchlTypeAnnouncement AS a " +
				" WHERE " +
				"   (a.edate IS NULL OR a.edate>= :rToday) " +
				"   AND (a.sdate IS NULL OR a.sdate<=:rToday) " +
				"   AND a.enabled = true " +
				"   AND NOT(:rUser IN elements(a.hasReadUsers)) " +
				"   AND (a.school IS NULL OR a.school.id=:rSchoolId)" +
				"   AND (a.projType IS NULL OR a.projType.id=:rTypeId)" +
				" 	ORDER BY a.urgent DESC, a.mdate DESC , a.id DESC ")
				.setTimestamp("rToday", new Date())
				.setInteger("rSchoolId", proj.getSchool().getId())
				.setInteger("rTypeId", proj.getType().getId())
				.setParameter("rUser", curUser)
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<SchlTypeAnnouncement> getDismissedSchlTypeAnnouncements(Project proj, User curUser){
		Query q = session.createQuery("SELECT a FROM SchlTypeAnnouncement AS a " +
				" WHERE " +
				"   (a.edate IS NULL OR a.edate>= :rToday) " +
				"   AND (a.sdate IS NULL OR a.sdate<=:rToday) " +
				"   AND a.enabled = true " +
				"   AND :rUser IN elements(a.hasReadUsers) " +
				"   AND (a.school IS NULL OR a.school.id=:rSchoolId)" +
				"   AND (a.projType IS NULL OR a.projType.id=:rTypeId)" +
				" 	ORDER BY a.urgent DESC, a.mdate DESC , a.id DESC ")
				.setTimestamp("rToday", new Date())
				.setInteger("rSchoolId", proj.getSchool().getId())
				.setInteger("rTypeId", proj.getType().getId())
				.setParameter("rUser", curUser)
				;
		
		return q.list();
	}
  	
	
	@Override
	public long countSchlTypeAnnouncements(Project proj) {
		Query q = session.createQuery("SELECT count(a) FROM SchlTypeAnnouncement AS a " +
				" WHERE " +
				"   (a.edate IS NULL OR a.edate>= :rToday) " +
				"   AND (a.sdate IS NULL OR a.sdate<=:rToday) " +
				"   AND a.enabled = true " +
				"   AND (a.school IS NULL OR a.school.id=:rSchoolId)" +
				"   AND (a.projType IS NULL OR a.projType.id=:rTypeId)" +
				" 	ORDER BY a.urgent DESC, a.mdate DESC , a.id DESC ")
				.setTimestamp("rToday", new Date())
				.setInteger("rSchoolId", proj.getSchool().getId())
				.setInteger("rTypeId", proj.getType().getId())
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public SchlTypeAnnouncement getSchlTypeAnnouncementById(int stAnnmtId) {
		return (SchlTypeAnnouncement) session.get(SchlTypeAnnouncement.class, stAnnmtId);
	}

	@Override
	public void save(SchlTypeAnnouncement stAnnmt) {
		session.persist(stAnnmt);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT a FROM SchlTypeAnnouncement AS a " +
				" WHERE " +
				"   (a.edate IS NULL OR a.edate>= :rToday) " +
				"   AND (a.sdate IS NULL OR a.sdate<=:rToday) " +
				"   AND a.enabled = true " +
				"   AND NOT(:rUser IN elements(a.hasReadUsers)) " +
				"   AND (a.school IS NULL OR a.school.id=:rSchoolId)" +
				"   AND (a.projType IS NULL OR a.projType.id=:rTypeId)" +
				"   AND a.mdate>=:rPastDay " +
				" 	ORDER BY a.mdate DESC ")
				.setTimestamp("rToday", new Date())
				.setInteger("rSchoolId", project.getSchool().getId())
				.setInteger("rTypeId", project.getType().getId())
				.setDate("rPastDay", pastDay.getTime())
				.setParameter("rUser", curUser)
				;
		
		List<SchlTypeAnnouncement> aList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(SchlTypeAnnouncement a : aList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(a.getMdate());
			l.setTitle(a.getSubject());
			l.setUrl(linkSource.createPageRenderLinkWithContext(SchlTypeAnnmtView.class, project.getId(), a.getId()).toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	
}