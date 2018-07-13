package ntu.celt.eUreka2.pages;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.search.SearchDAO;
import ntu.celt.eUreka2.modules.search.SearchResult;
import ntu.celt.eUreka2.modules.search.SearchResultType;
import ntu.celt.eUreka2.modules.search.SearchType;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.annotations.Inject;

public class Search {
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String srchTxt;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String srchField; 
	private boolean srchonload;
	@Property
	@Persist
	private List<SearchResult> srchResults;
	@SuppressWarnings("unused")
	@Property
	private List<SearchResult> srchResultsDisplay;
	@SuppressWarnings("unused")
	@Property
	private SearchResult srchResult;
	@SuppressWarnings("unused")
	@Property
	private String tempStr;
	
	@Inject
    private WebSessionDAO webSessionDAO;
	
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
	@SessionState
	private AppState appState;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd = new EvenOdd();
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Integer curPageNo;
	
	@Property
	private SearchResultType t1;
	@Property
	private SearchResultType t2;
	
	
    @Inject
    private SearchDAO searchDAO;
    
	void onActivate(EventContext ec) {
		if(ec.getCount()==2){
			srchTxt = ec.get(String.class, 0);
			srchField = ec.get(String.class, 1);
		}
		if(ec.getCount()==3){
			srchTxt = ec.get(String.class, 0);
			srchField = ec.get(String.class, 1);
			srchonload = ec.get(Boolean.class, 2);
		}
	}
	Object[] onPassivate() {
		if(srchonload)
			return new Object[]{srchTxt, srchField, srchonload};
		else
			return new Object[] {srchTxt, srchField};
	}
	
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	public SelectModel getSrchModel(){
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (SearchType t : SearchType.values()) {
			OptionModel optModel = new OptionModelImpl(Util.capitalize(t.name()), t.name());
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender(){
		if(srchonload){
			if(srchTxt!=null){
				doSearch();
			}
			curPageNo = 1;
			srchonload = false;
		}
		if(curPageNo==null)
			curPageNo = 1;
		if(srchResults==null)
			srchResults = new ArrayList<SearchResult>();
		
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), srchResults.size());
		srchResultsDisplay = srchResults.subList(fromIndex, toIndex);
	}
	private void doSearch(){
		srchResults = new ArrayList<SearchResult>();
		
		if(srchField!=null){
			SearchType srchType = SearchType.valueOf(srchField);
			switch(srchType){
				case PROJECTS: 
					srchResults = searchDAO.searchProjects(srchTxt, getCurUser());
					break;
				case ANNOUNCEMENTS: 
					srchResults = searchDAO.searchAnnouncements(srchTxt, getCurUser());
					break;
				case FORUMS:
					srchResults = searchDAO.searchForums(srchTxt, getCurUser());
					break;
				case RESOURCES: 
					srchResults = searchDAO.searchResources(srchTxt, getCurUser());
					break;
				case BLOGS:
					srchResults = searchDAO.searchWebBlogs(srchTxt, getCurUser()); 
					break;
			}
		}
		else{ //if srchType==null
			//case ALL:
			srchResults.addAll(searchDAO.searchProjects(srchTxt, getCurUser()));
			srchResults.addAll(searchDAO.searchAnnouncements(srchTxt, getCurUser()));
			srchResults.addAll(searchDAO.searchForums(srchTxt, getCurUser()));
			srchResults.addAll(searchDAO.searchResources(srchTxt, getCurUser()));
			srchResults.addAll(searchDAO.searchWebBlogs(srchTxt, getCurUser()));
		}
	}
	
	void onSubmitFromSrchForm(){
		if(srchTxt!=null){
			doSearch();
		}
		curPageNo = 1;
		srchonload = false;
		
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), srchResults.size());
		srchResultsDisplay = srchResults.subList(fromIndex, toIndex);
    }
	
	public int getTotalSize() {
		if(srchResults==null) return 0;
		return srchResults.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public void setRowsPerPage(int value){
		appState.setRowsPerPage(value);
	}
	
	
	@Inject
	@Path("context:lib/img/24/project.png")
	private Asset proj_png;
	@Inject
	@Path("context:lib/img/24/m_Announcement.png")
	private Asset annmt_png;
	@Inject
	@Path("context:lib/img/24/folder.png")
	private Asset folder_png;
	@Inject
	@Path("context:lib/img/24/file.png")
	private Asset file_png;
	@Inject
	@Path("context:lib/img/24/link.png")
	private Asset link_png;
	@Inject
	@Path("context:lib/img/24/m_Forum.png")
	private Asset forum_png;
	@Inject
	@Path("context:lib/img/24/thread.png")
	private Asset thread_png;
	@Inject
	@Path("context:lib/img/24/m_Blog.png")
	private Asset blog_png;
	
	public Asset getSearchResultTypeIcon(SearchResultType type){
		switch(type){
			case PROJECT: return proj_png;
			case ANNOUNCEMENT: return annmt_png;
			case RESOURCE_FOLDER: return folder_png;
			case RESOURCE_FILE: return file_png;
			case RESOURCE_LINK: return link_png;
			case FORUM: return forum_png;
			case FORUM_THREAD: return thread_png;
			case FORUM_THREAD_REPLY: return thread_png;
			case BLOG: return blog_png;
		}
		
		return null;
	}
	
	public boolean updateAndCheckTypeChanged(SearchResultType prev, SearchResultType cur){
		t1 = prev;
		t2 = cur;
		if(t1==null || t2.equals(t1))
			return false;
		return true;
	}
	
}
