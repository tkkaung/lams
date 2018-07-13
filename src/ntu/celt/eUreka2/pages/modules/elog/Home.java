package ntu.celt.eUreka2.pages.modules.elog;

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
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;

public class Home extends AbstractPageElog  {
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ElogDAO eDAO;
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	@Property
	private Project project;
	@SessionState
	private AppState appState;
	@Property
	private String pid;
	
	@Property
	private String date;
	@SuppressWarnings("unused")
	@Property
	private Elog elog;
	@Property
	private List<Elog> elogs;
	@SuppressWarnings("unused")
	@Property
	private List<Elog> elogsDisplay;
	@Persist("flash")
	@Property
	private Integer curPageNo;
	@SuppressWarnings("unused")
	@Property
	private ElogFile attFile;
	
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
		
		if(canViewElog(project)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		if (request.getParameter("date") != null) {
			date = request.getParameter("date");
			ddate = Util.stringToDate(date, "yyyyMMdd");
			sdate = ddate;
			edate = ddate;
		}
		
		
		elogs = eDAO.searchElogs(null, null, project, author, ElogStatus.APPROVED, true, sdate, edate, null, null, getCurUser());
		
		if(curPageNo==null)
			curPageNo = 1;
		int fromIndex = (curPageNo-1)*appState.getRowsPerPage();
		int toIndex = Math.min(curPageNo*appState.getRowsPerPage(), elogs.size());
		elogsDisplay = elogs.subList(fromIndex, toIndex);
	
		
		if(canApproveElog(project)){ //for org-supervisor
			int numPending = getCountPendingElog();
			if(numPending>0 ){
				if(canApproveElog(project) || canCreateElog(project)){//i.e: only Org-supervisor and student
					if(numPending>1)
						appState.recordInfoMsg(messages.format("x-elogs-pending-approval", numPending)  );
					else
						appState.recordInfoMsg(messages.format("x-elog-pending-approval", numPending) );
					
					
				}
			}
			String link = linkSource.createPageRenderLinkWithContext(ApproveOverview.class, pid).toAbsoluteURI();
			appState.recordInfoMsg( messages.format("click-x-to-approve-reject"
					, "<a href='"+link+"'>"+messages.get("elog-approve-list")+"</a>"
			));
		}
		else{ //for non supervisor
			int numPending = getCountMyPendingElog();
			if(numPending>0 ){
				if(canApproveElog(project) || canCreateElog(project)){//i.e: only Org-supervisor and student
					if(numPending>1)
						appState.recordInfoMsg(messages.format("my-x-elogs-pending-approval", numPending)  );
					else
						appState.recordInfoMsg(messages.format("my-x-elog-pending-approval", numPending) );
					
					
				}
			}			
		}

		
/*		int numApprovedUnpublished = 0;
		if(canApproveElog(project)){
			//for org-sup
			numApprovedUnpublished = getCountApprovedUnpublishedElog();
		}
		else{
			if(canCreateElog(project)){ 
				//for student
				numApprovedUnpublished = getCountApprovedUnpublishedElog(getCurUser());
			}
		}
		if(numApprovedUnpublished>0){
			if(numApprovedUnpublished>1)
				wsData.recordInfoMsg(messages.format("x-elogs-approved-unpublished", numApprovedUnpublished));
			else
				wsData.recordInfoMsg(messages.format("x-elog-approved-unpublished", numApprovedUnpublished));
		}
	*/
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
		List<Elog> bbs = eDAO.searchElogs(null, null, project, author, ElogStatus.APPROVED,true, null,null, null, null, getCurUser());
		Set<Date> dateSet = new TreeSet<Date>();
		for (Elog b : bbs) {
			dateSet.add(DateUtils.truncate(b.getCdate(), Calendar.DATE));
		}
		for (Date d : dateSet) {
			rtn += CaltoStr(d) + ",";
		}
		if(!rtn.equals("["))  //remove the last ","
			rtn = Util.removeLastSeparator(rtn, ",");
		rtn += "]";
		return rtn;
	}
	//return javascript object of date to numOfElogOnTheDate. e.g:  {20110315 : 2 , 20110317 : 4 , 20110331 : 1}
	public String getElogCountArr(){
		String rtn = "{";
		List<Elog> bbs = eDAO.searchElogs(null, null, project, author, ElogStatus.APPROVED,true, null, null, null, null, getCurUser());
		Set<Date> dateSet = new TreeSet<Date>();
		for (Elog b : bbs) {
			dateSet.add(DateUtils.truncate(b.getCdate(), Calendar.DATE));
		}
		for (Date d : dateSet) {
			List<Elog> bb = eDAO.searchElogs(null, null, project, author, ElogStatus.APPROVED,true, d, d, null, null, getCurUser());
			rtn += CaltoStr(d) + " : " + bb.size() + ",";
		}
		if(!rtn.equals("{"))  //remove the last ","
			rtn = Util.removeLastSeparator(rtn, ",");
		
		rtn += "}";
		return rtn;
	}
	
	private String CaltoStr(Date d) {
		return Util.formatDateTime(d, "yyyyMMdd");
	}
	
	public int getCountMyElog(){
		List<Elog> elogs = eDAO.searchElogs(null, null, project, getCurUser(), null,null, null, null, null, null, getCurUser());
		return elogs.size();
	}
	public int getCountPendingElog(){
		List<Elog> elogs = eDAO.searchElogs(null, null, project, null, ElogStatus.PENDING,null, null, null, null, null, getCurUser());
		return elogs.size();
	}
	public int getCountMyPendingElog(){
		List<Elog> elogs = eDAO.searchElogs(null, null, project, getCurUser(), ElogStatus.PENDING,null, null, null, null, null, getCurUser());
		return elogs.size();
	}
	public int getCountApprovedUnpublishedElog(User student){
		List<Elog> elogs = eDAO.searchElogs(null, null, project, student, ElogStatus.APPROVED,false, null, null, null, null, getCurUser());
		return elogs.size();
	}
	public int getCountApprovedUnpublishedElog(){
		return getCountApprovedUnpublishedElog(null);
	}
	
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public int getTotalSize() {
		if(elogs==null) return 0;
		return elogs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
