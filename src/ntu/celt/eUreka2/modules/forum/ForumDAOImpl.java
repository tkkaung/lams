package ntu.celt.eUreka2.modules.forum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.pages.modules.forum.ForumView;
import ntu.celt.eUreka2.pages.modules.forum.ThreadView;

public class ForumDAOImpl implements ForumDAO {
	@Inject
    private Session session;
	private PageRenderLinkSource linkSource;
	private Messages messages;
	
	
	public ForumDAOImpl(Session session, PageRenderLinkSource linkSource,
			Messages messages) {
		super();
		this.session = session;
		this.linkSource = linkSource;
		this.messages = messages;
	}
	
	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_FORUM;
	}
	
	@Override
	public void deleteForum(Forum forum) {
		forum.getThreads().clear();
		session.delete(forum);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Forum> getActiveForums(Project project) {
		Criteria crit = session.createCriteria(Forum.class)
			.add(Restrictions.eq("project.id", project.getId()));
		return crit.list();
	}


	@Override
	public Forum getForumById(Long id) {
		if(id==null) return null;
		return (Forum) session.get(Forum.class, id);
	}

	@Override
	public void saveForum(Forum forum) {
		session.persist(forum);
		//session.saveOrUpdate(forum);
	}

	@Override
	public void deleteThread(Thread thread) {
		thread.getAttachedFiles().clear();
		session.delete(thread);
	}
	@Override
	public void saveThread(Thread thread) {
		session.persist(thread);
	}
	@Override
	public Thread getThreadById(Long id) {
		if(id==null) return null;
		return (Thread) session.get(Thread.class, id);
	}
	@Override
	public void deleteThreadReply(ThreadReply threadR) {
		threadR.getAttachedFiles().clear();
		session.delete(threadR);
	}
	@Override
	public void saveThreadReply(ThreadReply thread) {
		session.persist(thread);
	}
	@Override
	public ThreadReply getThreadReplyById(Long id) {
		if(id==null) return null;
		return (ThreadReply) session.get(ThreadReply.class, id);
	}
	

	@Override
	public ThreadUser getThreadUser(Long threadId, Integer userId) {
		return (ThreadUser) session.createCriteria(ThreadUser.class)
			.add(Restrictions.eq("thread.id", threadId))
			.add(Restrictions.eq("user.id", userId))
			.uniqueResult();
	}
	@Override
	public void saveThreadUser(ThreadUser threadUser){
		session.persist(threadUser);
	}
	@Override
	public ThreadReplyUser getThreadReplyUser(Long threadReplyId, Integer userId) {
		return (ThreadReplyUser) session.createCriteria(ThreadReplyUser.class)
			.add(Restrictions.eq("threadReply.id", threadReplyId))
			.add(Restrictions.eq("user.id", userId))
			.uniqueResult();
	}
	@Override
	public void saveThreadReplyUser(ThreadReplyUser threadReplyUser){
		session.persist(threadReplyUser);
	}

	@Override
	public ForumAttachedFile getAttachedFileById(String id) {
		if(id==null) return null;
		return (ForumAttachedFile) session.get(ForumAttachedFile.class, id);
	}


	@Override
	public void saveAttachFile(ForumAttachedFile attachedFile) {
		session.save(attachedFile);
		
	}

	@Override
	public long countPastUpdated(Project project, int numOfPastDays) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numOfPastDays);
		
		Query q = session.createQuery("SELECT COUNT(th) FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum.project AS p "
				+ " WHERE p.id=:rPid "
				+ " AND th.modifyDate>=:rPastDay " 
				)
				.setParameter("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		long countTh = (Long) q.uniqueResult();
		q = session.createQuery("SELECT COUNT(thR) FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread.forum.project AS p "
				+ " WHERE p.id=:rPid "
				+ " AND thR.modifyDate>=:rPastDay " 
				)
				.setParameter("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		long countThR = (Long) q.uniqueResult();
		
		return countTh + countThR;
	}

	

	@Override
	public long getTotalForums(Project proj) {
		Query q = session.createQuery("SELECT COUNT(ch) FROM Forum AS ch " 
				+ " WHERE ch.project.id=:rPid "
				)
				.setString("rPid", proj.getId())
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalThreads(Project proj) {
		Query q = session.createQuery("SELECT COUNT(th) FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum.project AS p "
				+ " WHERE p.id=:rPid "
				)
				.setString("rPid", proj.getId())
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalThreadReplies(Project proj) {
		Query q = session.createQuery("SELECT COUNT(thR) FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread.forum.project AS p "
				+ " WHERE p.id=:rPid "
				)
				.setString("rPid", proj.getId())
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalThreads(Forum forum) {
		Query q = session.createQuery("SELECT COUNT(th) FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum AS f "
				+ " WHERE f.id=:rFid "
				)
				.setLong("rFid", forum.getId())
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalThreadReplies(Forum forum) {
		Query q = session.createQuery("SELECT COUNT(thR) FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread.forum AS f "
				+ " WHERE f.id=:rFid "
				)
				.setLong("rFid", forum.getId())
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Thread> getLatestThreads(Project project, int maxResult, int numOfPastDays) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numOfPastDays);
		
		Query q = session.createQuery("SELECT th FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum.project AS p "
				+ " WHERE p.id=:rPid "
				+ " AND th.modifyDate>=:rPastDay " 
				+ " ORDER BY th.modifyDate DESC "
				)
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				.setMaxResults(maxResult)
				;
		
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Thread> getLatestThreads(Forum forum, int maxResult) {
		Query q = session.createQuery("SELECT th FROM Thread AS th " 
				+ " LEFT OUTER JOIN th.forum AS f "
				+ " WHERE f.id=:rFid "
				+ " ORDER BY th.modifyDate DESC "
				)
				.setLong("rFid", forum.getId())
				.setMaxResults(maxResult)
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getLatestThreadReplies(Project project, int maxResult, int numOfPastDays) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numOfPastDays);
		
		Query q = session.createQuery("SELECT thR FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread.forum.project AS p "
				+ " WHERE p.id=:rPid "
				+ " AND thR.modifyDate>=:rPastDay " 
				+ " ORDER BY thR.modifyDate DESC "
				)
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				.setMaxResults(maxResult)
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getLatestThreadReplies(Forum forum, int maxResult) {
		Query q = session.createQuery("SELECT thR FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread.forum AS f "
				+ " WHERE f.id=:rFid "
				+ " ORDER BY thR.modifyDate DESC "
				)
				.setLong("rFid", forum.getId())
				.setMaxResults(maxResult)
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getLatestThreadReplies(Thread thread, int maxResult) {
		Query q = session.createQuery("SELECT thR FROM ThreadReply AS thR " 
				+ " LEFT OUTER JOIN thR.thread AS th "
				+ " WHERE th.id=:rTid "
				+ " ORDER BY thR.modifyDate DESC "
				)
				.setLong("rTid", thread.getId())
				.setMaxResults(maxResult)
				;
		
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadUser> getThUserByRateType(Thread thread, RateType type) {
		Query q = session.createQuery("SELECT thU FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.id=:rTid "
				+ "    AND thU.rate=:rType"
				)
				.setLong("rTid", thread.getId())
				.setParameter("rType", type)
				;
		
		return q.list();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReplyUser> getThUserByRateType(ThreadReply threadR, RateType type) {
		Query q = session.createQuery("SELECT thU FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.id=:rTid "
				+ "    AND thU.rate=:rType"
				)
				.setLong("rTid", threadR.getId())
				.setParameter("rType", type)
				;
		
		return q.list();
	}
	@Override
	public long getTotalRateByType(Thread thread, RateType type) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.id=:rTid "
				+ "    AND thU.rate=:rType"
				)
				.setLong("rTid", thread.getId())
				.setParameter("rType", type)
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalRateByType(ThreadReply threadR, RateType type) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.id=:rTid "
				+ "    AND thU.rate=:rType"
				)
				.setLong("rTid", threadR.getId())
				.setParameter("rType", type)
				;
		
		return (Long) q.uniqueResult();
	}

	@Override
	public long getTotalView(Thread thread) {
		Query q = session.createQuery("SELECT SUM(thU.numView) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.id=:rTid "
				)
				.setLong("rTid", thread.getId())
				;
		if(q.uniqueResult()==null)
			return 0;
		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT f FROM Forum AS f " +
				" WHERE f.project.id=:rPid " +
				"   AND f.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Forum> fList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Forum f : fList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(f.getModifyDate());
			l.setTitle(f.getName());
			l.setType(messages.get("forum"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(ForumView.class
					, f.getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		q = session.createQuery("SELECT th FROM Thread AS th " +
				" WHERE th.forum.project.id=:rPid " +
				"   AND th.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Thread> thList = q.list();
		for(Thread th : thList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(th.getModifyDate());
			l.setTitle(th.getName());
			l.setType(messages.get("thread"));
			Link link = linkSource.createPageRenderLinkWithContext(ThreadView.class, th.getId());
			l.setUrl(link.toAbsoluteURI());
			
			lList.add(l);
		}
		
		q = session.createQuery("SELECT thR FROM ThreadReply AS thR " +
				" WHERE thR.thread.forum.project.id=:rPid " +
				"   AND thR.modifyDate>=:rPastDay " +
				"   ORDER BY modifyDate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<ThreadReply> thRList = q.list();
		for(ThreadReply thR : thRList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(thR.getModifyDate());
			l.setTitle(thR.getName());
			l.setType(messages.get("reply"));
			Link link = linkSource.createPageRenderLinkWithContext(ThreadView.class, thR.getThread().getId());
			link.setAnchor("thR"+thR.getId());
			l.setUrl(link.toAbsoluteURI());
			
			lList.add(l);
		}
		
		//reflection
		q = session.createQuery("SELECT ref FROM LogEntry AS ref " +
				" WHERE ref.project.id=:rPid " +
				"	AND ref.forumThreadId IN " +
				"		(SELECT th.id FROM Thread AS th WHERE th.forum.project.id=:rPid) " +
				"	AND (ref.shared=true OR ref.creator=:rUser) " +
				"   AND ref.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setParameter("rUser", curUser)
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<LogEntry> refList = q.list();
		for(LogEntry ref : refList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(ref.getMdate());
			l.setTitle(ref.getTitle());
			l.setType(messages.get("reflection"));
			Link link = linkSource.createPageRenderLinkWithContext(ThreadView.class, ref.getForumThreadId());
			l.setUrl(link.toAbsoluteURI());
			
			lList.add(l);
		}
		
		Collections.sort(lList);
		
		return lList;
	}

	
	@Override
	public long getTotalThreadReflection(Project proj, User curUser){
		Query q = session.createQuery("SELECT count(ref) FROM LogEntry AS ref " +
				" WHERE ref.project.id=:rPid " +
				"	AND ref.forumThreadId IN " +
				"		(SELECT th.id FROM Thread AS th WHERE th.forum.project.id=:rPid) " +
				"	AND (ref.shared=true OR ref.creator=:rUser) ")
			.setString("rPid", proj.getId())
			.setParameter("rUser", curUser)
			;
		return (Long) q.uniqueResult();
	}

	
	
	

	@Override
	public long getTotalThreads(Project proj, User user) {
		Query q = session.createQuery("SELECT COUNT(th) FROM Thread AS th " 
				+ " WHERE th.forum.project.id=:rPid "
				+ "   AND th.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Thread> getThreads(Project proj, User user) {
		Query q = session.createQuery("SELECT th FROM Thread AS th " 
				+ " WHERE th.forum.project.id=:rPid "
				+ "   AND th.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				;
		
		return q.list();
	}
	@Override
	public long getTotalThreadsRate(Project proj, User user, RateType type) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.forum.project.id=:rPid "
				+ "    AND th.author=:rCreator "
				+ "    AND thU.rate=:rType "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				.setParameter("rType", type)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Thread> getThreadsRate(Project proj, User user, RateType type) {
		Query q = session.createQuery(
				"FROM Thread a1 WHERE a1.id IN ("
				+"SELECT DISTINCT(th.id) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.forum.project.id=:rPid "
				+ "    AND th.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ ")"
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				.setParameter("rType", type)
				;
		
		return q.list();
	}

	@Override
	public long getTotalThreadsYouRate(Project proj, User user, RateType type, User curUser) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.forum.project.id=:rPid "
				+ "    AND th.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ "    AND thU.user=:rUser "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				.setParameter("rUser", curUser)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Thread> getThreadsYouRate(Project proj, User user,
			RateType type, User curUser) {
		Query q = session.createQuery(
				"FROM Thread a1 WHERE a1.id IN ("
				+"SELECT DISTINCT(th.id)  FROM ThreadUser AS thU " 
				+ " JOIN thU.thread AS th "
				+ " WHERE th.forum.project.id=:rPid "
				+ "    AND th.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ "    AND thU.user=:rUser "
				+ ")"
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				.setParameter("rUser", curUser)
				;
		
		return q.list();
	}
	
	@Override
	public long getTotalThreadReplies(Project proj, User user) {
		Query q = session.createQuery("SELECT COUNT(thR) FROM ThreadReply AS thR " 
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "   AND thR.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getThreadReplies(Project proj, User user) {
		Query q = session.createQuery("SELECT thR FROM ThreadReply AS thR " 
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "   AND thR.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", user)
				;
		
		return q.list();
	}

	@Override
	public long getTotalThreadRepliesRate(Project proj, User user, RateType type) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "    AND thR.thread.author=:rCreator "
				+ "    AND thU.rate=:rType "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getThreadRepliesRate(Project proj, User user, RateType type) {
		Query q = session.createQuery(
				"FROM ThreadReply a1 WHERE a1.id IN ("
				+"SELECT DISTINCT(thR.id) FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "    AND thR.thread.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ ")"
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				;
		
		return q.list();
	}

	@Override
	public long getTotalThreadRepliesYouRate(Project proj, User user, RateType type, User curUser) {
		Query q = session.createQuery("SELECT COUNT(thU.rate) FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "    AND thR.thread.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ "    AND thU.user=:rUser "
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				.setParameter("rUser", curUser)
				;
		
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ThreadReply> getThreadRepliesYouRate(Project proj, User user,
			RateType type, User curUser) {
		Query q = session.createQuery(
				"FROM ThreadReply a1 WHERE a1.id IN ("+
				"SELECT DISTINCT(thR.id) FROM ThreadReplyUser AS thU " 
				+ " JOIN thU.threadReply AS thR "
				+ " WHERE thR.thread.forum.project.id=:rPid "
				+ "    AND thR.thread.author=:rCreator "
				+ "    AND thU.rate=:rType "
				+ "    AND thU.user=:rUser "
				+ " )"
				)
				.setString("rPid", proj.getId())
				.setParameter("rType", type)
				.setParameter("rCreator", user)
				.setParameter("rUser", curUser)
				;
		
		return q.list();
	}
	
	

	

	

	
	
	
	
}
