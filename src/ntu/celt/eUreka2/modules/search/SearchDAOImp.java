package ntu.celt.eUreka2.modules.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.resources.ResourceType;
import ntu.celt.eUreka2.pages.modules.announcement.AnnouncementView;
import ntu.celt.eUreka2.pages.modules.forum.ForumView;
import ntu.celt.eUreka2.pages.modules.forum.ThreadView;
import ntu.celt.eUreka2.pages.project.Home;

public class SearchDAOImp implements SearchDAO{
	private Session session;
	private PageRenderLinkSource linkSource;
	@SuppressWarnings("unused")
	private Logger logger;
	private Messages messages;
	private final int MAX_CONTENT_LENGTH = 1000; //max number of character
	
	public SearchDAOImp(Session session, PageRenderLinkSource linkSource, Logger logger, Messages messages) {
		super();
		this.session = session;
		this.linkSource = linkSource;
		this.logger = logger;
		this.messages = messages;
	}
	
	@Override
	public List<SearchResult> searchProjects(String searchText, User curUser){
		if(searchText==null)
			return null; //not allow empty search
		return this.advancedSearchProjects(searchText, null, null, null, null, null, null, curUser);
	}
	@Override
	public List<SearchResult> searchAnnouncements(String searchText, User curUser){
		if(searchText==null)
			return null; //not allow empty search
		return this.advancedSearchAnnouncements(searchText, null, null, null, null, null, null, curUser);
	}
	@Override
	public List<SearchResult> searchForums(String searchText, User curUser){
		if(searchText==null)
			return null; //not allow empty search
		return this.advancedSearchForums(searchText, null, null, null, null, null, null, curUser);
	}
	@Override
	public List<SearchResult> searchResources(String searchText, User curUser){
		if(searchText==null)
			return null; //not allow empty search
		return this.advancedSearchResources(searchText, null, null, null, null, null, null, true, null, curUser);
	}
	@Override
	public List<SearchResult> searchWebBlogs(String searchText, User curUser){
		if(searchText==null)
			return null; //not allow empty search
		return this.advancedSearchWebBlogs(searchText, null, null, null, null, null, null, curUser);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SearchResult> advancedSearchProjects(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		System.out.println("search Kanesh here");
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( p.name LIKE :word"+i
							+ " OR p.id LIKE :word"+i+" "
							+ " OR p.description LIKE :word"+i
							+ " OR pu.user.username LIKE :word"+i 
							//+ " OR pu.user.firstName LIKE :word"+i
							+ " OR pu.user.lastName LIKE :word"+i
							+ " OR p.companyInfo LIKE :word"+i
							+ " OR :wordExact"+i+ " IN elements(p.keywords) "
							+ " )";
				
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( p.name LIKE :word"+words.size()
						+ " OR p.id LIKE :word"+words.size()
						+ " OR p.description LIKE :word"+words.size()
						+ " OR pu.user.username LIKE :word"+words.size()
						//+ " OR pu.user.firstName LIKE :word"+words.size()
						+ " OR pu.user.lastName LIKE :word"+words.size()
						+ " OR p.companyInfo LIKE :word"+words.size()
						+ " OR :wordExact"+words.size()+ " IN elements(p.keywords) "
						+ ")";
			
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( p.name LIKE :word"+words.size()
							+ " OR p.id LIKE :word"+words.size()
							+ " OR p.description LIKE :word"+words.size()
							+ " OR pu.user.username LIKE :word"+words.size()
							//+ " OR pu.user.firstName LIKE :word"+words.size()
							+ " OR pu.user.lastName LIKE :word"+words.size()
							+ " OR p.companyInfo LIKE :word"+words.size()
							+ " OR :wordExact"+words.size()+ " IN elements(p.keywords) "
							+ ")";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( p.name LIKE :word"+words.size()
							+ " OR p.id LIKE :word"+words.size()
							+ " OR p.description LIKE :word"+words.size()
							+ " OR pu.user.username LIKE :word"+words.size()
							//+ " OR pu.user.firstName LIKE :word"+words.size()
							+ " OR pu.user.lastName LIKE :word"+words.size()
							+ " OR p.companyInfo LIKE :word"+words.size()
							+ " OR :wordExact"+words.size()+ " IN elements(p.keywords) "
							+ ")";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			filterSql += " AND p.school.id IN (:schlList)";
		}
		if(fromDate!=null){
			filterSql += " AND p.mdate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND p.mdate <= :param"+params.size();
			params.add(toDate);
		}
		
		String qStr = " SELECT DISTINCT(p.id) FROM Project AS p "
						+ " JOIN p.members AS pu  "
						+ " WHERE (p.shared=true OR pu.user.id=:curUserId) "
						+ " AND NOT p.status.name = :statusDelete "
						+ filterSql;
		String qStr1 = "FROM Project p1 WHERE p1.id IN ("+qStr+")";
		Query q = session.createQuery(qStr1);
		q.setInteger("curUserId", curUser.getId())
			.setString("statusDelete", PredefinedNames.PROJSTATUS_DELETED);
		for(int i=0; i<words.size(); i++){
			String word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
			q.setString("wordExact"+i, word);
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		List<Project> projs = q.list();
		for(Project p : projs){
			SearchResult r = new SearchResult();
			r.setByUser(p.getCreator()==null? null : p.getCreator().getDisplayName());
			r.setType(SearchResultType.PROJECT);
			r.setLink(linkSource.createPageRenderLinkWithContext(Home.class, p.getId()).toAbsoluteURI());
			r.setModifyTime(p.getMdate());
			r.setProj(p);
			String title = Util.stripTags(p.getDisplayName() + " - " +p.getName());
			title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			
			String content = Util.stripTags(p.getDescription());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			if(!p.isActive())
				r.setRemarks(p.getStatus().getDisplayName());
			
			String leaderInfo = "";
			for(ProjUser pu : p.getMembers()){
				if(pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER)){
					leaderInfo += pu.getUser().getDisplayName()+", ";
				}
			}
			leaderInfo = Util.removeLastSeparator(leaderInfo, ", ");
			leaderInfo = Util.replaceHighlightTextHTML(leaderInfo, words);
			
			String keywords = "";
			for(String keyword : p.getKeywords()){
				keywords += keyword+", ";
			}
			keywords = Util.removeLastSeparator(keywords, ", ");
			keywords = Util.replaceHighlightTextHTML(keywords, words);
			
			
			r.getExtraInfo().put(messages.get("leader-text"), leaderInfo);
			if(!keywords.isEmpty())
				r.getExtraInfo().put(messages.get("keyword-text"), keywords);
			
			results.add(r);
		}
		return results;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<SearchResult> advancedSearchAnnouncements(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( a.subject LIKE :word"+i
							+ "	OR a.content LIKE :word"+i
							+ " OR a.creator.username LIKE :word"+i
							+ " OR a.creator.firstName LIKE :word"+i
							+ " OR a.creator.lastName LIKE :word"+i
							+ " )";
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( a.subject LIKE :word"+words.size()
						+ "	OR a.content LIKE :word"+words.size()
						+ " OR a.creator.username LIKE :word"+words.size()
						+ " OR a.creator.firstName LIKE :word"+words.size()
						+ " OR a.creator.lastName LIKE :word"+words.size()
						+ " )";
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( a.subject LIKE :word"+words.size()
							+ " OR a.content LIKE :word"+words.size()
							+ " OR a.creator.username LIKE :word"+words.size()
							+ " OR a.creator.firstName LIKE :word"+words.size()
							+ " OR a.creator.lastName LIKE :word"+words.size()
							+ " )";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += "  ( a.subject LIKE :word"+words.size()
							+ " OR a.content LIKE :word"+words.size()
							+ " OR a.creator.username LIKE :word"+words.size()
							+ " OR a.creator.firstName LIKE :word"+words.size()
							+ " OR a.creator.lastName LIKE :word"+words.size()
							+ " )";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			filterSql += " AND p.school.id IN (:schlList)";
		}
		if(fromDate!=null){
			filterSql += " AND a.modifyDate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND a.modifyDate <= :param"+params.size();
			params.add(toDate);
		}
		
		
		Query q = session.createQuery(
				"FROM Announcement a1 WHERE a1.id IN ("+
					"SELECT DISTINCT(a.id) FROM Announcement AS a , Project AS p " 
					+ " JOIN p.members AS pu  "
					+ " WHERE a.project.id=p.id " 
					+ " AND a.enabled=true "
					//+ "	AND (p.shared=true OR pu.user.id=:curUserId) " 
					+ "	AND (pu.user.id=:curUserId) "		//only users belong to the project 
					+ " AND (a.endDate is null OR a.endDate>= :rToday) " 
					+ " AND a.startDate<=:rToday " 
					+ filterSql
				+")"
				)
				.setInteger("curUserId", curUser.getId())
				.setDate("rToday", DateUtils.truncate(new Date(), Calendar.DATE));
				;
		//set parameters
		q.setInteger("curUserId", curUser.getId());
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		
		//list and form the searchResult object
		List<Announcement> annmts = q.list();
		for(Announcement a : annmts){
			SearchResult r = new SearchResult();
			r.setByUser(a.getCreator().getDisplayName());
			r.setType(SearchResultType.ANNOUNCEMENT);
			r.setModifyTime(a.getCreateDate());
			r.setProj(a.getProject());
			String title = Util.stripTags(a.getSubject());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(a.getContent());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);	
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			r.setLink(linkSource.createPageRenderLinkWithContext(AnnouncementView.class, a.getId()).toAbsoluteURI());
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			results.add(r);
		}

		return results;
	}
	@Override
	public List<SearchResult> advancedSearchForums(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		results.addAll( advancedSearchFForums(allWord, exactPhrase, anyWord, withoutWord,
				selSchools, fromDate, toDate, curUser));
		results.addAll( advancedSearchThreads(allWord, exactPhrase, anyWord, withoutWord,
				selSchools, fromDate, toDate, curUser));
		
		return results;
	}
	@SuppressWarnings("unchecked")
	private List<SearchResult> advancedSearchFForums(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( f.name LIKE :word"+i
							+ " OR f.description LIKE :word"+i
							+ " OR f.creator.username LIKE :word"+i
							+ " OR f.creator.firstName LIKE :word"+i
							+ " OR f.creator.lastName LIKE :word"+i
							+ " )";
				
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( f.name LIKE :word"+words.size()
						+ " OR f.description LIKE :word"+words.size()
						+ " OR f.creator.username LIKE :word"+words.size()
						+ " OR f.creator.firstName LIKE :word"+words.size()
						+ " OR f.creator.lastName LIKE :word"+words.size()
						+ " )";
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( f.name LIKE :word"+words.size()
							+ " OR f.description LIKE :word"+words.size()
							+ " OR f.creator.username LIKE :word"+words.size()
							+ " OR f.creator.firstName LIKE :word"+words.size()
							+ " OR f.creator.lastName LIKE :word"+words.size()
							+ " )";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += "  ( f.name LIKE :word"+words.size()
							+ " OR f.description LIKE :word"+words.size()
							+ " OR f.creator.username LIKE :word"+words.size()
							+ " OR f.creator.firstName LIKE :word"+words.size()
							+ " OR f.creator.lastName LIKE :word"+words.size()
							+ " )";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			filterSql += " AND f.project.school.id IN (:schlList))";
		}
		if(fromDate!=null){
			filterSql += " AND f.createDate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND f.createDate <= :param"+params.size();
			params.add(toDate);
		}
		
		Query q = session.createQuery(
				"FROM Forum a1 WHERE a1.id IN ("+
					"SELECT DISTINCT (f.id) FROM Forum as f  " 
					+ " JOIN f.project AS p "
					+ " JOIN p.members AS pu  "
					+ " Where pu.user.id=:curUserId " 
					+ filterSql	
				+ ")"
				)
				.setInteger("curUserId", curUser.getId())
				;
		//set parameters
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		
		//list and form the searchResult object
		List<Forum> forums = q.list();
		for(Forum f : forums){
			SearchResult r = new SearchResult();
			r.setByUser(f.getCreator().getDisplayName());
			r.setType(SearchResultType.FORUM);
			r.setModifyTime(f.getCreateDate());
			r.setProj(f.getProject());
			String title = Util.stripTags(f.getName());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(f.getDescription());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			r.setLink(linkSource.createPageRenderLinkWithContext(ForumView.class, f.getId()).toAbsoluteURI());
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			results.add(r);
		}
		
		return results;
	}
	@SuppressWarnings("unchecked")
	private List<SearchResult> advancedSearchThreads(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( t.name LIKE :word"+i
							+ " OR t.message LIKE :word"+i
							+ " OR t.author.username LIKE :word"+i
							+ " OR t.author.firstName LIKE :word"+i
							+ " OR t.author.lastName LIKE :word"+i
							+ " )";
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( t.name LIKE :word"+words.size()
						+ " OR t.message LIKE :word"+words.size()
						+ " OR t.author.username LIKE :word"+words.size()
						+ " OR t.author.firstName LIKE :word"+words.size()
						+ " OR t.author.lastName LIKE :word"+words.size()
						+ " )";
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( t.name LIKE :word"+words.size()
							+ " OR t.message LIKE :word"+words.size()
							+ " OR t.author.username LIKE :word"+words.size()
							+ " OR t.author.firstName LIKE :word"+words.size()
							+ " OR t.author.lastName LIKE :word"+words.size()
							+ " )";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += "  ( t.name LIKE :word"+words.size()
							+ " OR t.message LIKE :word"+words.size()
							+ " OR t.author.username LIKE :word"+words.size()
							+ " OR t.author.firstName LIKE :word"+words.size()
							+ " OR t.author.lastName LIKE :word"+words.size()
							+ " )";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		String schlSqlTh = "";
		String schlSqlThR = "";
		
		if(selSchools!=null && !selSchools.isEmpty()){
			schlSqlTh = " AND t.forum.project.school.id IN (:schlList))";
			schlSqlThR = " AND t.thread.forum.project.school.id IN (:schlList))";
		}
		if(fromDate!=null){
			filterSql += " AND t.modifyDate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND t.modifyDate <= :param"+params.size();
			params.add(toDate);
		}
		
		Query q = session.createQuery(
				"FROM Thread a1 WHERE a1.id IN ("+
					"SELECT DISTINCT(t.id) FROM Thread as t  "
					+ " JOIN t.forum.project AS p "
					+ " JOIN p.members AS pu  "
					+ " WHERE pu.user=:curUser " 
					+ filterSql	
					+ schlSqlTh
				+ " )"
				)
				.setParameter("curUser", curUser)
				
				;
		
		//set parameters
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		
		//list and form the searchResult object
		List<Thread> threads = q.list();
		for(Thread t : threads){
			SearchResult r = new SearchResult();
			r.setByUser(t.getAuthorDisplayName());
			r.setType(SearchResultType.FORUM_THREAD);
			r.setModifyTime(t.getCreateDate());
			r.setProj(t.getForum().getProject());
			String title = Util.stripTags(t.getName());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(t.getMessage());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			r.setLink(linkSource.createPageRenderLinkWithContext(ThreadView.class, t.getId()).toAbsoluteURI());
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			results.add(r);
		}
		
		
		
		
		q = session.createQuery(
				"FROM ThreadReply a1 WHERE a1.id IN ("+
					"SELECT DISTINCT t.id FROM ThreadReply AS t  "
					+ " JOIN t.thread.forum.project AS p "
					+ " JOIN p.members AS pu  "
					+ " WHERE pu.user=:curUser " 
					+ filterSql	
					+ schlSqlThR
				+ " ) ")
				.setParameter("curUser", curUser)
				;
		//set parameters
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		//list and form the searchResult object
		List<ThreadReply> threadReplies = q.list();
		for(ThreadReply t : threadReplies){
			SearchResult r = new SearchResult();
			r.setByUser(t.getAuthorDisplayName());
			r.setType(SearchResultType.FORUM_THREAD_REPLY);
			r.setModifyTime(t.getCreateDate());
			r.setProj(t.getThread().getForum().getProject());
			String title = Util.stripTags(t.getName());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(t.getMessage());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			r.setLink(linkSource.createPageRenderLinkWithContext(ThreadView.class, t.getThread().getId()).toAbsoluteURI());
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			results.add(r);
		}
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SearchResult> advancedSearchResources(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, 
			boolean only, String includeFileFormats,  User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( r.name LIKE :word"+i
							+ " OR r.des LIKE :word"+i
							+ " OR r.owner.username LIKE :word"+i
							+ " OR r.owner.firstName LIKE :word"+i
							+ " OR r.owner.lastName LIKE :word"+i
							+ " )";
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( r.name LIKE :word"+words.size()
						+ " OR r.des LIKE :word"+words.size()
						+ " OR r.owner.username LIKE :word"+words.size()
						+ " OR r.owner.firstName LIKE :word"+words.size()
						+ " OR r.owner.lastName LIKE :word"+words.size()
						+ " )";
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( r.name LIKE :word"+words.size()
							+ " OR r.des LIKE :word"+words.size()
							+ " OR r.owner.username LIKE :word"+words.size()
							+ " OR r.owner.firstName LIKE :word"+words.size()
							+ " OR r.owner.lastName LIKE :word"+words.size()
							+ " )";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += "  ( r.name LIKE :word"+words.size()
							+ " OR r.des LIKE :word"+words.size()
							+ " OR r.owner.username LIKE :word"+words.size()
							+ " OR r.owner.firstName LIKE :word"+words.size()
							+ " OR r.owner.lastName LIKE :word"+words.size()
							+" )";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			filterSql += " AND r.proj.school.id IN (:schlList) ";
		}
		if(fromDate!=null){
			filterSql += " AND r.mdate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND r.mdate <= :param"+params.size();
			params.add(toDate);
		}
		String fileExtension[] = null;
		if(includeFileFormats != null){
			fileExtension = includeFileFormats.split(",");
			if(only) //ONLY include specific file format
				filterSql += " AND (";
			else //DONT include specific file format
				filterSql += " AND NOT (";
			for(int i=0; i<fileExtension.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " r.name LIKE :fileExt"+i;  
			}
			filterSql += " )";
		}
		else{
			if(!only){ //DONT return ALL file format
				return results; // return nothing
			}
		}
		
		Query q = session.createQuery(
				"FROM Resource a1 WHERE a1.id IN ("+
					"SELECT DISTINCT r.id FROM Resource as r  "
					+ " JOIN r.proj AS p "
					+ " JOIN p.members AS pu  "
					+ " WHERE pu.user=:curUser " 
				//	+ " AND r.type <> :folderType "
					+ " AND (r.shared = true OR r.owner=:curUser ) "
					+ filterSql	
				+ " )" 
				+ " ORDER BY a1.type, a1.name")
				.setParameter("curUser", curUser)
			//	.setParameter("folderType", ResourceType.Folder)
				;
		
		//set parameters
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		if(fileExtension!=null ){
			for(int i=0; i<fileExtension.length; i++){
				q.setString("fileExt"+i, "%." + fileExtension[i]);
			}
		}
		
		//list and form the searchResult object
		List<Resource> resources = q.list();
		
		for(Resource rs : resources){
			SearchResult r = new SearchResult();
			r.setByUser(rs.getOwner().getDisplayName());
			if(rs.getEditor()!=null && !rs.getOwner().equals(rs.getEditor()))
				r.setByUser(r.getByUser() + ", "+ rs.getEditor().getDisplayName());
			r.setModifyTime(rs.getCdate());
			r.setProj(rs.getProj());
			String title = Util.stripTags(rs.getName());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(rs.getDes());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			
			//set resource Link
			r.setLink(linkSource.createPageRenderLinkWithContext(ntu.celt.eUreka2.pages.modules.resources.Home.class
					, rs.getProj().getId()
					, rs.getType().equals(ResourceType.Folder)?  rs.getId() : rs.getParent().getId())
					.toAbsoluteURI());
			
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			switch(rs.getType()){
			case Folder : r.setType(SearchResultType.RESOURCE_FOLDER);
				break;
			case File : r.setType(SearchResultType.RESOURCE_FILE);
				r.getExtraInfo().put(messages.get("version-text"), rs.toFile().getLatestFileVersion().getVersionDisplay());
				break;
			case Link : r.setType(SearchResultType.RESOURCE_LINK);
				break;
			}
			
			results.add(r);
		}
		
		return results;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SearchResult> advancedSearchWebBlogs(String allWord,
			String exactPhrase, String anyWord, String withoutWord,
			List<String> selSchools, Date fromDate, Date toDate, User curUser) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		List<String> words = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		
		String filterSql = "";
		if(allWord!=null){
			String[] allWords = allWord.split(" ");
			for(int i=0; i<allWords.length; i++){
				filterSql += " AND ( b.subject LIKE :word"+i
							+ " OR b.content LIKE :word"+i
							+ " OR b.author.username LIKE :word"+i
							+ " OR b.author.firstName LIKE :word"+i
							+ " OR b.author.lastName LIKE :word"+i
							+" )";
				words.add(allWords[i]);
			}
		}
		if(exactPhrase!=null){
			filterSql += " AND ( b.subject LIKE :word"+words.size()
						+ " OR b.content LIKE :word"+words.size()
						+ " OR b.author.username LIKE :word"+words.size()
						+ " OR b.author.firstName LIKE :word"+words.size()
						+ " OR b.author.lastName LIKE :word"+words.size()
						+ " )";
			words.add(exactPhrase);
		}
		if(anyWord!=null){
			String[] anyWords = anyWord.split(" ");
			filterSql += " AND ( ";
			for(int i=0; i<anyWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += " ( b.subject LIKE :word"+words.size()
							+ " OR b.content LIKE :word"+words.size()
							+ " OR b.author.username LIKE :word"+words.size()
							+ " OR b.author.firstName LIKE :word"+words.size()
							+ " OR b.author.lastName LIKE :word"+words.size()
							+ " )";
				words.add(anyWords[i]);
			}
			filterSql += " )";
		}
		if(withoutWord !=null){
			String[] withoutWords = withoutWord.split(" ");
			filterSql += " AND NOT( ";
			for(int i=0; i<withoutWords.length; i++){
				if(i!=0)
					filterSql += " OR ";
				filterSql += "  ( b.subject LIKE :word"+words.size()
							+ " OR b.content LIKE :word"+words.size()
							+ " OR b.author.username LIKE :word"+words.size()
							+ " OR b.author.firstName LIKE :word"+words.size()
							+ " OR b.author.lastName LIKE :word"+words.size()
							+ " )";
				words.add(withoutWords[i]);
			}
			filterSql += " )";
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			filterSql += " AND b.project.school.id IN (:schlList))";
		}
		if(fromDate!=null){
			filterSql += " AND b.mdate >= :param"+params.size();
			params.add(fromDate);
		}
		if(toDate!=null){
			filterSql += " AND b.mdate <= :param"+params.size();
			params.add(toDate);
		}
		
		Query q = session.createQuery(
				"FROM Blog a1 WHERE a1.id IN ("+
					"SELECT DISTINCT b.id FROM Blog AS b  "
					+ " JOIN b.project AS p "
					+ " JOIN p.members AS pu  "
					+ " Where pu.user=:curUser " 
					+ " AND (b.author = :curUser " 
					//+ "       OR :curUser is moderator which is set in BlogConfig"
					+		" OR (b.status =:publish AND b.shared = true ))"
					+ filterSql	
				+ " ) ")
				.setParameter("curUser", curUser)
				.setParameter("publish", BlogStatus.PUBLISHED)
				;
		
		//set parameters
		for(int i=0; i<words.size(); i++){
			Object word = words.get(i);
			q.setString("word"+i, "%"+word+"%");
		}
		for(int i=0; i<params.size(); i++){
			Object param = params.get(i);
			q.setParameter("param"+i, param);
		}
		if(selSchools!=null && !selSchools.isEmpty()){
			q.setParameterList("schlList", Util.StrList2IntList(selSchools));
		}
		
		//list and form the searchResult object
		List<Blog> blogs = q.list();
		for(Blog b : blogs){
			SearchResult r = new SearchResult();
			r.setByUser(b.getAuthor().getDisplayName());
			r.setModifyTime(b.getMdate());
			r.setProj(b.getProject());
			String title = Util.stripTags(b.getSubject());
				title = Util.replaceHighlightTextHTML(title, words);
			r.setTitle(title);
			String content = Util.stripTags(b.getContent());
				content = Util.truncateString(content, MAX_CONTENT_LENGTH);
				content = Util.replaceHighlightTextHTML(content, words);
			r.setContent(content);
			r.setType(SearchResultType.BLOG);
			r.setLink(linkSource.createPageRenderLinkWithContext(
					ntu.celt.eUreka2.pages.modules.blog.Home.class, b.getProject().getId(),"").toAbsoluteURI()
					+"?date="+Util.formatDateTime(b.getCdate(), "yyyyMMdd"));
			
			r.getExtraInfo().put(messages.get("project"), r.getProj().getDisplayName());
			r.getExtraInfo().put(messages.get("by-user-text"), 
					Util.replaceHighlightTextHTML(r.getByUser(), words)+", "+ r.getModifyTimeDisplay());
			
			results.add(r);
		}
		
		return results;
	}
	
}
