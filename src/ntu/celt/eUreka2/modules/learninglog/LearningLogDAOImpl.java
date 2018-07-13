package ntu.celt.eUreka2.modules.learninglog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.pages.modules.learninglog.LearningLogView;

public class LearningLogDAOImpl implements LearningLogDAO {

	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	
	
	public LearningLogDAOImpl(Session session, PageRenderLinkSource linkSource) {
		super();
		this.session = session;
		this.linkSource = linkSource;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_LEARNING_LOG;
	}
	
	@Override
	public LLogFile getLLogFileById(String id) {
		if(id==null) return null;
		return (LLogFile) session.get(LLogFile.class, id);
	}
	@Override
	public void saveLLogFile(LLogFile file) {
		session.persist(file);
	}
	
	

	@Override
	public void deleteLogEntry(LogEntry entry) {
		entry.getFiles().clear();
		session.delete(entry);
	}
	@Override
	public void saveLogEntry(LogEntry entry) {
		session.save(entry);
	}
	@Override
	public LogEntry getLogEntryById(Long id) {
		if(id==null) return null;
		return (LogEntry) session.get(LogEntry.class, id);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getLogEntryTypes(User user) {
		Query q = session.createQuery("SELECT DISTINCT e.type " 
				+ " FROM LogEntry AS e " 
				+ " WHERE e.creator = :rUser "
				+ " AND e.type IS NOT NULL"
				+ " ORDER BY e.type ASC ")
				.setParameter("rUser", user);
		
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> searchLogEntries(User user, Project proj, String type, String filterText) {
		Criteria crit = session.createCriteria(LogEntry.class);
		
		if(user!=null)
			crit = crit.add(Restrictions.eq("creator", user));
		if(proj!=null)
			crit = crit.add(Restrictions.eq("project.id", proj.getId()));
		if(type!=null)
			crit = crit.add(Restrictions.eq("type", type));
		
		if(filterText!=null){
			Criterion cr = null;
			String[] words = filterText.split(" ");
			for(String word : words){
				cr = Restrictions.like("title", "%"+word+"%");
				cr = Restrictions.or(cr, Restrictions.like("content", "%"+word+"%"));
				crit = crit.add(cr);
			}
		}
		crit.addOrder(Order.desc("mdate"));
		
		return crit.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT e FROM LogEntry AS e " +
				" WHERE e.project.id=:rPid " +
				"   AND e.creator=:rUser" +
				"   AND e.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setParameter("rUser", curUser)
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<LogEntry> eList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(LogEntry e : eList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(e.getMdate());
			l.setTitle(e.getTitle());
			l.setUrl(linkSource.createPageRenderLinkWithContext(LearningLogView.class
					, e.getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		return lList;
	}

	@Override
	public long countLlogs(Project proj) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l " +
			" WHERE l.project.id=:rPid " )
			.setString("rPid", proj.getId())
			;
		return (Long) q.uniqueResult();
	}

	@Override
	public long countForumReflections(long threadId, User curUser) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l " +
			" WHERE l.forumThreadId=:rTid " +
			" AND (l.shared=true OR l.creator=:rUser) " )
			.setLong("rTid", threadId)
			.setParameter("rUser", curUser)
			;
		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getForumReflections(long threadId, User curUser) {
		Query q = session.createQuery("SELECT l FROM LogEntry AS l " +
				" WHERE l.forumThreadId=:rTid " +
				" AND (l.shared=true OR l.creator=:rUser) " +
				" ORDER BY l.cdate asc " )
				.setLong("rTid", threadId)
				.setParameter("rUser", curUser)
				;
		return q.list();
	}
	@Override
	public long countForumReflections(Project proj, User user, User curUser) {
		Query q = session.createQuery("SELECT count(ref) FROM LogEntry AS ref " +
				" WHERE ref.project.id=:rPid " +
				"	AND ref.forumThreadId IN " +
				"		(SELECT th.id FROM Thread AS th WHERE th.forum.project.id=:rPid) " +
				"	AND (ref.shared=true OR ref.creator=:rUser) " +
				"   AND ref.creator=:rCreator "
				)
			.setString("rPid", proj.getId())
			.setParameter("rUser", curUser)
			.setParameter("rCreator", user)
			;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getForumReflections(Project proj, User user, User curUser) {
		Query q = session.createQuery("SELECT ref FROM LogEntry AS ref " +
				" WHERE ref.project.id=:rPid " +
				"	AND ref.forumThreadId IN " +
				"		(SELECT th.id FROM Thread AS th WHERE th.forum.project.id=:rPid) " +
				"	AND (ref.shared=true OR ref.creator=:rUser) " +
				"   AND ref.creator=:rCreator "
				)
			.setString("rPid", proj.getId())
			.setParameter("rUser", curUser)
			.setParameter("rCreator", user)
			;
		return q.list();
	}
	@Override
	public long countBlogReflections(String blogId, User curUser) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l " +
				" WHERE l.blogId=:rTid " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", blogId)
				.setParameter("rUser", curUser)
				;
		return (Long) q.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getBlogReflections(String blogId, User curUser) {
		Query q = session.createQuery("SELECT l FROM LogEntry AS l " +
				" WHERE l.blogId=:rTid " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", blogId)
				.setParameter("rUser", curUser)
				;
		return  q.list();
	}
	@Override
	public long countBlogReflections(Project proj, User creator, User curUser) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l, Blog AS b " +
				" WHERE l.project.id=:rTid " +
				" AND l.blogId=b.id " +
				" AND b.project.id=:rTid " +
				" AND l.creator=:rCreator " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rUser", curUser)
				;
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getBlogReflections(Project proj, User creator, User curUser) {
		Query q = session.createQuery("SELECT l FROM LogEntry AS l, Blog AS b " +
				" WHERE l.project.id=:rTid " +
				" AND l.blogId=b.id " +
				" AND b.project.id=:rTid " +
				" AND l.creator=:rCreator " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rUser", curUser)
				;
		return q.list();
	}
	
	@Override
	public long countElogReflections(String elogId, User curUser) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l " +
				" WHERE l.elogId=:rTid " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", elogId)
				.setParameter("rUser", curUser)
				;
		return (Long) q.uniqueResult();
	}

	@Override
	public long countElogReflections(Project proj, User creator, User curUser) {
		Query q = session.createQuery("SELECT count(l) FROM LogEntry AS l, Elog AS e " +
				" WHERE l.project.id=:rTid " +
				" AND l.elogId=e.id " +
				" AND e.project.id=:rTid " +
				" AND l.creator=:rCreator " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rUser", curUser)
				;
		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getElogReflections(String elogId, User curUser) {
		Query q = session.createQuery("SELECT l FROM LogEntry AS l " +
				" WHERE l.elogId=:rTid " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", elogId)
				.setParameter("rUser", curUser)
				;
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LogEntry> getElogReflections(Project proj, User creator, User curUser) {
		Query q = session.createQuery("SELECT l FROM LogEntry AS l, Elog AS e " +
				" WHERE l.project.id=:rTid " +
				" AND l.elogId=e.id " +
				" AND e.project.id=:rTid " +
				" AND l.creator=:rCreator " +
				" AND (l.shared=true OR l.creator=:rUser) " )
				.setString("rTid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rUser", curUser)
				;
		return q.list();
	}

}
