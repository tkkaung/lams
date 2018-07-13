package ntu.celt.eUreka2.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
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
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class SearchAdvanced {
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private List<SearchResult> srchResults;
	@SuppressWarnings("unused")
	@Property
	private List<SearchResult> srchResultsDisplay;
	@SuppressWarnings("unused")
	@Property
	private String tempStr;
	@SuppressWarnings("unused")
	@Property
	private SearchResult srchResult;
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
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String allWord;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String exactPhrase;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String anyWord;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String withoutWord;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private List<String> selSchools;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String includeFile;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private String includeFileFormat;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date fromDate;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private Date toDate;
	@Persist(PersistenceConstants.CLIENT)
	@Property
	private List<String> srchTypes;
	@Property
	private boolean isFirstLoad;
	@Property
	private SearchResultType t1;
	@Property
	private SearchResultType t2;
	
	
	@Inject
    private SearchDAO searchDAO;
    @Inject
    private SchoolDAO schoolDAO;
    @Inject
    private Messages messages;
    
	
	@SuppressWarnings("unused")
	@Property
	private StringValueEncoder stringEncoder = new StringValueEncoder();
	
	void onActivate(EventContext ec) {
		if(ec.getCount()==1){
			isFirstLoad = ec.get(Boolean.class, 0);
		}
		
	}
	Object[] onPassivate() {
		
		if(isFirstLoad)
			return new Object[]{isFirstLoad};
		return new Object[]{};
	}
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	void setupRender(){
		if(curPageNo==null)
			curPageNo = 1;
		if(srchResults==null)
			srchResults = new ArrayList<SearchResult>();
		
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), srchResults.size());
		srchResultsDisplay = srchResults.subList(fromIndex, toIndex);
	}
	
	public SelectModel getSchoolModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = schoolDAO.getAllSchools();
		for (School s : schoolList) {
			OptionModel optModel = new OptionModelImpl(s.getDisplayNameLong()
					, Integer.toString(s.getId()));
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getFileFormatModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		optModelList.add(new OptionModelImpl( "Adobe PDF (.pdf)"  , "pdf"));
		optModelList.add(new OptionModelImpl( "Microsoft Word (.doc, .docx)"  , "doc,docx"));
		optModelList.add(new OptionModelImpl( "Microsoft Excel (.xls, .xlsx)"  , "xls,xlsx"));
		optModelList.add(new OptionModelImpl( "Microsoft PowerPoint (.ppt, .pptx)"  , "ppt,pptx"));
		optModelList.add(new OptionModelImpl( "Image (.jpg, .gif, .png)"  , "jpg,jpeg,gif,png"));
		optModelList.add(new OptionModelImpl( "Rich Text Format (.rtf)"  , "rtf"));
		optModelList.add(new OptionModelImpl( "Comma-Separated Values (.csv)"  , "csv"));
		optModelList.add(new OptionModelImpl( "Compressed file (.zip)"  , "zip"));
		optModelList.add(new OptionModelImpl( "HTML file (.html, .htm)"  , "html,htm"));
		optModelList.add(new OptionModelImpl( "XML file (.xml)"  , "xml"));
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getSrchTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (SearchType s : SearchType.values()) {
			OptionModel optModel = new OptionModelImpl(s.toString(), s.toString());
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}

	@Component
	private Form srchForm;
	@Component(id="toDate")
	private DateField toDateField;
	void onValidateFormFromSrchForm(){
		if(toDate!=null && fromDate!=null && toDate.before(fromDate)){
			srchForm.recordError(toDateField, messages.get("endDate-must-be-after-startDate"));
		}
	}
	
	void onSubmitFromSrchForm(){
		doSearch(); 
		curPageNo = 1;
		isFirstLoad = false;
		
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), srchResults.size());
		srchResultsDisplay = srchResults.subList(fromIndex, toIndex);
    }
	private void doSearch(){
		srchResults = new ArrayList<SearchResult>();
		if(srchTypes!=null && !srchTypes.isEmpty()){
			for(String srchField : srchTypes){
				List<SearchResult> results = null;
				SearchType srchType = SearchType.valueOf(srchField);
				switch(srchType){
				case PROJECTS: 
					results = searchDAO.advancedSearchProjects(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser());
					break;
				case ANNOUNCEMENTS: 
					results = searchDAO.advancedSearchAnnouncements(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser());
					break;
				case FORUMS:
					results = searchDAO.advancedSearchForums(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser());
					break;
				case RESOURCES: 
					boolean only = (includeFile.equalsIgnoreCase("only")? true : false);
					results =  searchDAO.advancedSearchResources(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, only, includeFileFormat, getCurUser());
					break;
				case BLOGS:
					results = searchDAO.advancedSearchWebBlogs(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser()); 
					break;
				}
				if(results!=null){
					srchResults.addAll(results);
				}
			}
		}
		else{
			//case ALL
			srchResults.addAll(searchDAO.advancedSearchProjects(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser()));
			srchResults.addAll(searchDAO.advancedSearchAnnouncements(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser()));
			srchResults.addAll(searchDAO.advancedSearchForums(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser()));
			boolean only = (includeFile.equalsIgnoreCase("only")? true : false);
			srchResults.addAll(searchDAO.advancedSearchResources(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, only, includeFileFormat, getCurUser()));
			srchResults.addAll(searchDAO.advancedSearchWebBlogs(allWord, exactPhrase, anyWord, withoutWord, selSchools, fromDate, toDate, getCurUser()));
		}
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
	
	
	
	@InjectPage
	private Search searchPage;
	public Asset getSearchResultTypeIcon(SearchResultType type){
		return searchPage.getSearchResultTypeIcon(type);
	}
	
	public boolean updateAndCheckTypeChanged(SearchResultType prev, SearchResultType cur){
		t1 = prev;
		t2 = cur;
		if(t1==null || t2.equals(t1))
			return false;
		return true;
	}
	
	public Object[] getSimpleSearchContext(){
		return new Object[]{allWord, null, true};
	}
	
	
}
