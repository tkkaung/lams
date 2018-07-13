package ntu.celt.eUreka2.modules.blog;

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
import ntu.celt.eUreka2.pages.modules.blog.View;

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

public class BlogDAOImpl implements BlogDAO{
	@Inject
	private Session session;
	private PageRenderLinkSource linkSource;
	private Messages messages;
	
	
	public BlogDAOImpl(Session session, PageRenderLinkSource linkSource,
			Messages messages) {
		super();
		this.session = session;
		this.linkSource = linkSource;
		this.messages = messages;
	}

	@Override
	public String getModuleName() {
		return PredefinedNames.MODULE_BLOG;
	}
	
	@Override
	public void addBlog(Blog blog) {
		session.save(blog);
	}

	@Override
	public void deleteBlog(Blog blog) {
		session.delete(blog);
	}

	@Override
	public Blog getBlogById(String id) {
		if(id==null) return null;
		Blog blog = (Blog) session.get(Blog.class, id);
		return blog;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Blog> getBlogsByProject(Project project) {
		Query query = session.createQuery("select b from Blog as b Join b.project as p where p.id = :rProj");
		query.setString("rProj", project.getId());
		return query.list();
	}

	
	@Override
	public void updateBlog(Blog blog) {
		session.persist(blog);
	}

	@Override
	public void updateComment(BlogComment comment) {
		session.persist(comment);
	}
	

	@Override
	public BlogComment getBlogCommentById(int id) {
		return (BlogComment) session.get(BlogComment.class, id);
	}

	@Override
	public BlogFile getBlogFileById(String id) {
		return (BlogFile) session.get(BlogFile.class, id);
	}

	@Override
	public void updateBlogFile(BlogFile blogFile) {
		session.merge(blogFile);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Blog> searchBlogs(String searchText, BlogSearchableField searchIn,
			Project project, User author, BlogStatus status, Date sdate, Date edate,
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
					for(int i=0; i<BlogSearchableField.values().length; i++){
						BlogSearchableField f = BlogSearchableField.values()[i];
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
		if(firstResult!=null){
			crit.setFirstResult(firstResult);
		}
		if(maxResults!=null){
			crit.setMaxResults(maxResults);
		}

		crit = crit.add(Restrictions.or(Restrictions.eq("shared", true),Restrictions.eq("author", curUser)));
		crit = crit.add(Restrictions.or(Restrictions.eq("status", BlogStatus.PUBLISHED),Restrictions.eq("author", curUser)));
		crit = crit.addOrder(Order.desc("cdate"));
		
		return crit.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Blog> getActiveBlogs(Project proj, User u){
		Query q = session.createQuery("SELECT b FROM Blog b" 
				+ " WHERE b.project.id = :rPid "
				+ " AND (b.author = :rUser OR b.shared = true) "
				+ " AND (b.author = :rUser OR b.status = :rPublish) "
				+ " ORDER BY mdate DESC ")
				.setString("rPid", proj.getId())
				.setParameter("rUser", u)
				.setParameter("rPublish", BlogStatus.PUBLISHED)
				;
		return q.list();
	}

	@Override
	public long getTotalBlogComment(Project proj, User u) {
		Query q = session.createQuery("SELECT COUNT(c) FROM BlogComment c" 
				+ " WHERE c.blog.project.id = :rPid "
				+ " AND (c.author = :rUser OR c.disabled = false) "
				)
				.setString("rPid", proj.getId())
				.setParameter("rUser", u)
				;
		return (Long) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LastUpdateItem> getLastUpdates(Project project, User curUser, int numPastDay) {
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_YEAR, -1 * numPastDay);
		
		Query q = session.createQuery("SELECT b FROM Blog AS b " +
				" WHERE b.project.id=:rPid " +
				"   AND (b.shared = true OR b.author = :rUser) " +  //AND b.status = active
				"   AND b.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setParameter("rUser", curUser)
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<Blog> bList = q.list();
		List<LastUpdateItem> lList = new ArrayList<LastUpdateItem>();
		for(Blog b : bList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(b.getMdate());
			l.setTitle(b.getSubject());
			l.setType(messages.get("blog"));
			l.setUrl(linkSource.createPageRenderLinkWithContext(View.class
					, b.getId())
					.toAbsoluteURI());
			
			lList.add(l);
		}
		
		q = session.createQuery("SELECT c FROM BlogComment AS c " +
				" WHERE c.blog.project.id=:rPid " +
				"   AND c.disabled = false " +  //AND c.status = active
				"   AND c.mdate>=:rPastDay " +
				"   ORDER BY mdate DESC" +
				" 	 ")
				.setString("rPid", project.getId())
				.setDate("rPastDay", pastDay.getTime())
				;
		
		List<BlogComment> cList = q.list();
		for(BlogComment c : cList){
			LastUpdateItem l = new LastUpdateItem();
			l.setTime(c.getMdate());
			l.setTitle(Util.truncateString(Util.stripTags(c.getContent()), 30));
			l.setType(messages.get("comment"));
			Link link = linkSource.createPageRenderLinkWithContext(View.class, c.getBlog().getId());
			link.setAnchor("viewcmt");
			l.setUrl(link.toAbsoluteURI());
			
			lList.add(l);
		}
		
		Collections.sort(lList);
		
		return lList;
	}

	@Override
	public long countBlogs(Project proj) {
		Query q = session.createQuery("SELECT count(b) FROM Blog AS b " +
			" WHERE b.project.id=:rPid ")
			.setString("rPid", proj.getId());

		return (Long) q.uniqueResult();
	}
	@Override
	public long countBlogs(Project proj, User creator, BlogStatus status, User curUser) {
		Query q = session.createQuery("SELECT count(b) FROM Blog AS b " 
				+ " WHERE b.project.id=:rPid "
				+ " AND (b.shared = true OR b.author = :rUser) " 
				+ (creator==null? "" : " AND b.author=:rCreator ")
				+ (status==null? "" : " AND b.status=:rStatus ")
				)
				.setString("rPid", proj.getId())
				.setParameter("rUser", curUser);
		if(creator!=null)
			q.setParameter("rCreator", creator);
		if(status!=null)
			q.setParameter("rStatus", status);
	
		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Blog> getBlogs(Project proj, User creator, BlogStatus status, User curUser) {
		Query q = session.createQuery("SELECT b FROM Blog AS b " 
				+ " WHERE b.project.id=:rPid "
				+ " AND (b.shared = true OR b.author = :rUser) " 
				+ (creator==null? "" : " AND b.author=:rCreator ")
				+ (status==null? "" : " AND b.status=:rStatus ")
				)
				.setString("rPid", proj.getId())
				.setParameter("rUser", curUser);
		if(creator!=null)
			q.setParameter("rCreator", creator);
		if(status!=null)
			q.setParameter("rStatus", status);
	
		return q.list();
	}

	@Override
	public long countBlogComments(Project proj) {
		Query q = session.createQuery("SELECT count(c) FROM BlogComment AS c " +
			" WHERE c.blog.project.id=:rPid ")
			.setString("rPid", proj.getId());

		return (Long) q.uniqueResult();
	}
	@Override
	public long countBlogComments(Project proj, User creator) {
		Query q = session.createQuery("SELECT count(c) FROM BlogComment AS c " 
				+ " WHERE c.blog.project.id=:rPid "
				+ " AND c.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator);

		return (Long) q.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<BlogComment> getBlogComments(Project proj, User creator) {
		Query q = session.createQuery("SELECT c FROM BlogComment AS c " 
				+ " WHERE c.blog.project.id=:rPid "
				+ " AND c.author=:rCreator "
				)
				.setString("rPid", proj.getId())
				.setParameter("rCreator", creator);

		return q.list();
	}
	

	

	@Override
	public boolean hasBlogTags(Project proj) {
		Query q = session.createQuery("SELECT count(t) FROM Blog b" 
				+ " LEFT JOIN b.tags AS t " 
				+ ((proj==null)? "": " WHERE b.project.id=:rPid " )
				//+ " AND count(t)>0 "
				);
		if(proj!=null)
			q.setString("rPid", proj.getId());
		
		if(q.uniqueResult()==null)
			return false;
		long count = (Long) q.uniqueResult();
		if(count>0)
			return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getBlogTags(Project proj, int MaxResult) {
		Query q = session.createQuery("SELECT t FROM Blog b " +
				" LEFT JOIN b.tags AS t " +
				((proj==null)? "": " WHERE b.project.id=:rPid " )+
				" GROUP BY t HAVING t is not null" +
				" ORDER BY count(t) DESC"
				)
				.setMaxResults(MaxResult);
			if(proj!=null)
				q.setString("rPid", proj.getId());
		
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Blog> getBlogsByTags(Project proj, String tag, User curUser) {
		if(proj==null)
			return null;
		
		Query q = session.createQuery("SELECT b FROM Blog AS b " +
				" WHERE b.project.id=:rPid " +
				" AND :rTag IN elements(b.tags) " +
				" AND (b.shared = true OR b.author = :rUser)" +
				" AND (b.status = :rStatus OR b.author = :rUser)" +
				" ORDER BY b.cdate DESC"
				)
				.setString("rPid", proj.getId())
				.setString("rTag", tag)
				.setParameter("rUser", curUser)
				.setParameter("rStatus", BlogStatus.PUBLISHED)
				;
		
		
		return q.list();
	}
	
	@Override
	public long countBlogsByTags(Project proj, String tag, User curUser) {
		if(proj==null)
			return 0;
		
		Query q = session.createQuery("SELECT count(b) FROM Blog AS b " +
				" WHERE b.project.id=:rPid " +
				" AND :rTag IN elements(b.tags) " +
				" AND (b.shared = true OR b.author = :rUser)" +
				" AND (b.status = :rStatus OR b.author = :rUser)" 
				//+" ORDER BY b.cdate DESC"
				)
				.setString("rPid", proj.getId())
				.setString("rTag", tag)
				.setParameter("rUser", curUser)
				.setParameter("rStatus", BlogStatus.PUBLISHED)
				;
		
		if(q.uniqueResult()==null)
			return 0;
		
		return (Long)q.uniqueResult();
	}

	

	
	
	
	
	
}
