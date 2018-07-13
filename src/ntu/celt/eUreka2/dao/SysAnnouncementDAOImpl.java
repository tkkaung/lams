package ntu.celt.eUreka2.dao;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.SysAnnouncement;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class SysAnnouncementDAOImpl implements SysAnnouncementDAO {
	@Inject
    private Session session;

	@Override
	public void delete(SysAnnouncement sysAnnmt) {
		session.delete(sysAnnmt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SysAnnouncement> getAllSysAnnouncements() {
		return session.createCriteria(SysAnnouncement.class)
			.addOrder(Order.desc("mdate"))
			.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SysAnnouncement> getActiveSysAnnouncements() {
		Query q = session.createQuery("SELECT a FROM SysAnnouncement AS a " +
				" WHERE " +
				"   (a.edate IS NULL OR a.edate>= :rToday) " +
				"   AND (a.sdate IS NULL OR a.sdate<=:rToday) " +
				"   AND a.enabled = true " +
				" 	ORDER BY a.urgent DESC, a.mdate DESC , a.id DESC ")
				.setTimestamp("rToday", new Date());
				
		return q.list();
	}

	@Override
	public SysAnnouncement getSysAnnouncementById(int sysAnnmtId) {
		return (SysAnnouncement) session.get(SysAnnouncement.class, sysAnnmtId);
	}

	@Override
	public void save(SysAnnouncement sysAnnmt) {
		session.persist(sysAnnmt);
	}
	
	
}