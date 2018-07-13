package ntu.celt.eUreka2.pages.modules.blog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

public class Home extends AbstractPageBlog  {
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BlogDAO bDAO;
	@Property
	private Project project;
	@SessionState
	private AppState appState;
	@Property
	private String pid;
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	
	@Property
	private String date;
	@SuppressWarnings("unused")
	@Component
	private Form filterForm;
	@SuppressWarnings("unused")
	@Property
	private Blog blog;
	@Property
	private List<Blog> blogs;
	@SuppressWarnings("unused")
	@Property
	private List<Blog> blogsDisplay;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@SuppressWarnings("unused")
	@Property
	private BlogFile attFile;
	@SuppressWarnings("unused")
	@Property
	private String tempTag;
	@SuppressWarnings("unused")
	@Property
	private int tempIndex;
	
	
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private User author;
	private Date ddate;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date sdate;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date edate;
	
	
	
	void onActivate(String id) {
		pid = id;
	}
	String onPassivate() {
		return pid;
	}
	
	public void setupRender() {
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		if(!canViewBlog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		if (request.getParameter("date") != null) {
			date = request.getParameter("date");
			ddate = Util.stringToDate(date, "yyyyMMdd");
			sdate = ddate;
			edate = ddate;
		}
		
		blogs = bDAO.searchBlogs(null, null, project, author, null, sdate, edate, null, null, getCurUser());
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), blogs.size());
		blogsDisplay = blogs.subList(fromIndex, toIndex);
	}
	

	void onActionFromLast7Days(){
		Calendar date = Calendar.getInstance();
		edate = date.getTime();
		date.add(Calendar.DAY_OF_YEAR, -7);
		sdate = date.getTime();
	}
	void onActionFromLast30Days(){
		Calendar date = Calendar.getInstance();
		edate = date.getTime();
		date.add(Calendar.DAY_OF_YEAR, -30);
		sdate = date.getTime();
	}
	void onActionFromAllDate(){
		edate = null;
		sdate = null;
	}
	void onSuccessFromDateFilterForm(){
		//edate = edate;
		//sdate = sdate;
		
		//do nothing, only need to reload
	}
	void onSuccessFromFilterForm(){
		//do nothing, only need to reload
	}
	
	
	
	public SelectModel getAuthorModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<User> members = project.getUsers();
		for (User u : members) {
			OptionModel optModel = new OptionModelImpl(u.getDisplayName(), u);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	//return javascript array of dates to be highlight. e.g: [20110315,20110317,20110331]
	public String getDatesHighlight() {
		String rtn = "[";
		List<Blog> bbs = bDAO.searchBlogs(null, null, project, author, null, null, null, null, null, getCurUser());
		Set<Date> dateSet = new TreeSet<Date>();
		for (Blog b : bbs) {
			dateSet.add(DateUtils.truncate(b.getCdate(), Calendar.DATE));
		}
		for (Date d : dateSet) {
			rtn += CaltoStr(d) + ",";
		}
		if(!rtn.equals("["))  
			rtn = Util.removeLastSeparator(rtn, ",");
		rtn += "]";
		return rtn;
	}
	//return javascript object of date to numOfBlogOnTheDate. e.g:  {20110315 : 2 , 20110317 : 4 , 20110331 : 1}
	public String getBlogCountArr(){
		String rtn = "{";
		List<Blog> bbs = bDAO.searchBlogs(null, null, project, author, null, null, null, null, null, getCurUser());
		Set<Date> dateSet = new TreeSet<Date>();
		for (Blog b : bbs) {
			dateSet.add(DateUtils.truncate(b.getCdate(), Calendar.DATE));
		}
		for (Date d : dateSet) {
			List<Blog> bb = bDAO.searchBlogs(null, null, project, author, null, d, d, null, null, getCurUser());
			rtn += CaltoStr(d) + " : " + bb.size() + ",";
		}
		if(!rtn.equals("{"))  //remove the last ","
			rtn = Util.removeLastSeparator(rtn, ",");
		
		rtn += "}";
		return rtn;
	}
	
	private String CaltoStr(Date d) {
	/*	String rtn = "";
		int i = d.getYear() + 1900;
		String str = Integer.toString(i);
		rtn = rtn + str;
		i = d.getMonth() + 1;
		str = Integer.toString(i);
		if (i <= 9) {
			str = "0" + str;
		}
		rtn = rtn + str;
		i = d.getDate();
		str = Integer.toString(i);
		if (i <= 9) {
			str = "0" + str;
		}
		rtn =  rtn + str; 
		return rtn;
	*/
		return Util.formatDateTime(d, "yyyyMMdd");
	}
	
	
	public int getTotalSize() {
		if(blogs==null) return 0;
		return blogs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
}
