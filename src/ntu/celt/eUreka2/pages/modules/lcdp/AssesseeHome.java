package ntu.celt.eUreka2.pages.modules.lcdp;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurveyUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageLCDP{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<LCDPSurvey> lcdps;
	@SuppressWarnings("unused")
	@Property
	private LCDPSurvey lcdp;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private LCDPDAO lcdpDAO;
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
		
		lcdps =  lcdpDAO.getLCDPSurveysByProject(project);
		
		if(lcdps.size()==1 ){
			lcdp = lcdps.get(0);
			if(isShowLink(lcdp)){ //PENDING
				return (linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, lcdp.getId()));
			}
		}
		
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		
		
		
		
		removeExpiredLCDPSurveyUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(LCDPSurvey pro : lcdps){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<LCDPSurvey> getModel() {
		BeanModel<LCDPSurvey> model = beanModelSource.createEditModel(LCDPSurvey.class, messages);
		model.include("name", "sdate");
		model.get("name").label("Leadership Preference Survey");
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
		if (lcdps == null)
			return 0;
		return lcdps.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(LCDPSurvey lcdp){
		if(lcdp.getSdate()!=null ||lcdp.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(LCDPSurvey lcdp){
		if(lcdp.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(lcdp.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(LCDPSurvey lcdp){
		if(lcdp.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(lcdp.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(LCDPSurvey lcdp){
		if(lcdp.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		LCDPSurveyUser lcdpUser = null;
		List<LCDPSurveyUser> lcdpUserList = lcdpDAO.searchLCDPSurveyUser(lcdp, getCurUser(),  null, null);
		if(lcdpUserList.size()>0)
			lcdpUser = lcdpUserList.get(0);
		
		if(lcdpUser==null){
			if(isBeforeRatePeriod(lcdp)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(lcdp)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(lcdpUser.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(lcdp)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(lcdp)){
				statusStudent = "Submission closed";
				return false;
			}

			if(lcdpUser.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + lcdpUser.getLastQuestionNum() + " of " + lcdpUser.getSurvey().getQuestions().size();
			return true;
		}
	}

}
