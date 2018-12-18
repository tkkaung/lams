package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageIPSP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<IPSPSurvey> ipsps;
	@SuppressWarnings("unused")
	@Property
	private IPSPSurvey ipsp;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private PageRenderLinkSource linkSource;
	

	
	Object onActivate(String id) {
		pid = id;
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		
		ipsps =  ipspDAO.getIPSPSurveysByProject(project);
		
		if(ipsps.size()==1 ){
			ipsp = ipsps.get(0);
			if(isShowLink(ipsp)){ //PENDING
				return (linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, ipsp.getId()));
			}
		}
		
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		
		
		
		
		removeExpiredIPSPSurveyUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(IPSPSurvey pro : ipsps){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<IPSPSurvey> getModel() {
		BeanModel<IPSPSurvey> model = beanModelSource.createEditModel(IPSPSurvey.class, messages);
		model.include("name", "sdate");
		model.get("name").label("IPSP Survey");
		model.get("sdate").label("Opened Period");
		
		model.add("order",null);
		model.get("order").label("");
		model.add("status",null);
		model.get("status").label("Status");
		
		if(hasAtLeastOneReleased()){
			model.add("result",null);
		}
		
		model.reorder("order","name", "status", "sdate");
		
		return model;
	}
	
	public int getRowNum(){
		return rowIndex+1;
	}
	public int getTotalSize() {
		if (ipsps == null)
			return 0;
		return ipsps.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(IPSPSurvey ipsp){
		if(ipsp.getSdate()!=null ||ipsp.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(IPSPSurvey ipsp){
		if(ipsp.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(ipsp.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(IPSPSurvey ipsp){
		if(ipsp.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(ipsp.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(IPSPSurvey ipsp){
		if(ipsp.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		IPSPSurveyUser ipspUser = null;
		List<IPSPSurveyUser> ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, getCurUser(),  null, null, null);
		if(ipspUserList.size()>0)
			ipspUser = ipspUserList.get(0);
		
		if(ipspUser==null){
			if(isBeforeRatePeriod(ipsp)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(ipsp)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(ipspUser.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(ipsp)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(ipsp)){
				statusStudent = "Submission closed";
				return false;
			}

			if(ipspUser.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + ipspUser.getLastQuestionNum() + " of " + ipspUser.getSurvey().getQuestions().size();
			return true;
		}
	}

}
