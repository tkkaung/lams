package ntu.celt.eUreka2.pages.modules.care;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CARESurvey;
import ntu.celt.eUreka2.modules.care.CARESurveyUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageCARE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<CARESurvey> cares;
	@SuppressWarnings("unused")
	@Property
	private CARESurvey care;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private CAREDAO careDAO;
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
		
		cares =  careDAO.getCARESurveysByProject(project);
		
		if(cares.size()==1 ){
			care = cares.get(0);
			if(isShowLink(care)){ //PENDING
				return (linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, care.getId()));
			}
		}
		
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		
		
		
		
		removeExpiredCARESurveyUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(CARESurvey pro : cares){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<CARESurvey> getModel() {
		BeanModel<CARESurvey> model = beanModelSource.createEditModel(CARESurvey.class, messages);
		model.include("name", "sdate");
		model.get("name").label("CARE Survey");
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
		if (cares == null)
			return 0;
		return cares.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(CARESurvey care){
		if(care.getSdate()!=null ||care.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(CARESurvey care){
		if(care.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(care.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(CARESurvey care){
		if(care.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(care.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(CARESurvey care){
		if(care.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		CARESurveyUser careUser = null;
		List<CARESurveyUser> careUserList = careDAO.searchCARESurveyUser(care, getCurUser(),  null, null);
		if(careUserList.size()>0)
			careUser = careUserList.get(0);
		
		if(careUser==null){
			if(isBeforeRatePeriod(care)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(care)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(careUser.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(care)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(care)){
				statusStudent = "Submission closed";
				return false;
			}

			if(careUser.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + careUser.getLastQuestionNum() + " of " + careUser.getSurvey().getQuestions().size();
			return true;
		}
	}

}
