package ntu.celt.eUreka2.modules.elog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.pages.modules.elog.View;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ElogDAOImpl implements ElogDAO{
	@Inject
	private Session session;
	private PageRenderLinkSource linkSource;
	private Messages messages;
	
	
	public ElogDAOImpl(Session session, PageRenderLinkSource linkSource,
			Messages messages) {
		super();
		this.session = session;
		this.linkSource = linkSource;
		this.messages = messages;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_ELOG;
	}
	
	@Override
	public void addElog(Elog elog) {
		session.save(elog);
	}
	@Override
	public void updateElog(Elog elog) {
		session.persist(elog);
	}
	@Override
	public void deleteElog(Elog elog) {
		session.delete(elog);
	}

	@Override
	public Elog getElogById(String id) {
		if(id==null) return null;
		Elog elog = (Elog) session.get(Elog.class, id);
		return elog;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Elog> getElogsByProject(Project project) {
		Query query = session.createQuery("select b from Elog as b Join b.project as p where p.id = :rProj");
		query.setString("rProj", project.getId());
		return query.list();
	}


	@Override
	public void updateComment(ElogComment comment) {
		session.persist(comment);
	}

	@Override
	public ElogComment getElogCommentById(int id) {
		return (ElogComment) session.get(ElogComment.class, id);
	}

	@Override
	public ElogFile getElogFileById(String id) {
		return (ElogFile) session.get(ElogFile.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Elog> searchElogs(String searchText, ElogSearchableField searchIn,
			Project project, User author, ElogStatus status, Boolean published, 
			Date sdate, Date edate, 
			Integer firstResult, Integer maxResults, User curUser) {
		
		Criteria crit = session.createCriteria(Elog.class);
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
					for(int i=0; i<ElogSearchableField.values().length; i++){
						ElogSearchableField f = ElogSearchableField.values()[i];
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
		if(sdate!=null){
			crit = crit.add(Restrictions.ge("cdate",sdate));
		}
		if(edate!=null){
			edate = DateUtils.truncate(edate, Calendar.DATE);
			Calendar nextDate = Calendar.getInstance();
			nextDate.setTime(edate);
			nextDate.add(Calendar.DAY_OF_YEAR, 1);
			
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
		if(published!=null){
			crit = crit.add(Restrictions.eq("published", published));
		}
		if(firstResult!=null){
			crit.setFirstResult(firstResult);
		}
		if(maxResults!=null){
			crit.setMaxResults(maxResults);
		}

		crit = crit.addOrder(Order.desc("cdate"));
		
		return crit.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Elog> getActiveElogs(Project proj, User u){
		Query q = session.createQuery("SELECT b FROM Elog b" 
				+ " WHERE b.project.id = :rPid "
				+ " AND ( b.status = :rPublish) "  //b.author = :rUser OR
				+ " ORDER BY mdate DESC ")
				.setString("rPid", proj.getId())
			//	.setParameter("rUser", u)
				.setParameter("rPublish", ElogStatus.APPROVED)
				;
		return q.list();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT e FROM Elog AS e " +
				" WHERE e.project.id=:rPid " +
				"   AND (e.status = :rApproved  )" +  //OR (e.author = :rUser)
				"   AND e.published = true " +
				"   AND e.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setParameter("rApproved", ElogStatus.APPROVED)
				.setString("rPid", project.getId())
		//		.setParameter("rUser", curUser)
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Elog> eList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Elog e : eList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(e.getMdate());
			l.setTitle(e.getSubject());
			l.setType(messages.get("elog"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(View.class
					, e.getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		q = session.createQuery("SELECT c FROM ElogComment AS c " +
				" WHERE c.elog.project.id=:rPid " +
				"   AND c.elog.published = true " +
				"   AND c.disabled = false " +  //AND c.status = active
				"   AND c.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<ElogComment> cList = q.list();
		for(ElogComment c : cList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(c.getMdate());
			l.setTitle(Util.truncateString(Util.stripTags(c.getContent()), 30));
			l.setType(messages.get("comment"));
			Link link = linkSource.createPageRenderLinkWithContext(View.class, c.getElog().getId());
			link.setAnchor("viewcmt");
			l.setUrl(link.toAbsoluteURI());
			
			lList.add(l);
		}
		
		Collections.sort(lList);
		
		return lList;
	}

	@Override
	public long countElogs(Project proj) {
		Query q = session.createQuery("SELECT count(e) FROM Elog AS e " +
			" WHERE e.project.id=:rPid ")
			.setString("rPid", proj.getId());

		return (Long) q.uniqueResult();
	}

	@Override
	public long countElogComments(Project proj) {
		Query q = session.createQuery("SELECT count(c) FROM ElogComment AS c " +
			" WHERE c.elog.project.id=:rPid ")
			.setString("rPid", proj.getId());

		return (Long) q.uniqueResult();
	}

	

	@Override
	public long countElogs(Project proj, User creator, User user) {
		Query q = session.createQuery("SELECT count(e) FROM Elog AS e " 
				+ " WHERE e.project.id=:rPid "
				+ "  AND (e.published = true "
				+ "     OR e.status=:rApproved OR e.author=:rUser )"
				+ "  AND e.author = :rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rApproved", ElogStatus.APPROVED)
				.setParameter("rUser", user)
				;

		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Elog> getElogs(Project proj, User creator, User user) {
		Query q = session.createQuery("SELECT e FROM Elog AS e " 
				+ " WHERE e.project.id=:rPid "
				+ "  AND (e.published = true "
				+ "     OR e.status=:rApproved OR e.author=:rUser )"
				+ "  AND e.author = :rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				.setParameter("rApproved", ElogStatus.APPROVED)
				.setParameter("rUser", user)
				;

		return q.list();
	}
	@Override
	public long countElogComments(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(c) FROM ElogComment AS c " 
				+ " WHERE c.elog.project.id=:rPid "
				+ "  AND c.author=:rCreator"
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;

		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<ElogComment> getElogComments(Project proj, User creator) {
		Query q = session.createQuery("SELECT c FROM ElogComment AS c " 
				+ " WHERE c.elog.project.id=:rPid "
				+ "  AND c.author=:rCreator"
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator)
				;

		return q.list();
	}

	
	
	
}
