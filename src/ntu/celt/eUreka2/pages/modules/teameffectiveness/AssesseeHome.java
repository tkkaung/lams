package ntu.celt.eUreka2.pages.modules.teameffectiveness;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurveyUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageTE{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<TESurvey> tes;
	@SuppressWarnings("unused")
	@Property
	private TESurvey te;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private TEDAO teDAO;
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
		
		tes =  teDAO.getTESurveysByProject(project);
		
		if(tes.size()==1 ){
			te = tes.get(0);
			if(isShowLink(te)){ //PENDING
				return (linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, te.getId()));
			}
		}
		
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		
		
		
		
		removeExpiredTESurveyUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(TESurvey pro : tes){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<TESurvey> getModel() {
		BeanModel<TESurvey> model = beanModelSource.createEditModel(TESurvey.class, messages);
		model.include("name", "sdate");
		model.get("name").label("TE Survey");
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
		if (tes == null)
			return 0;
		return tes.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(TESurvey te){
		if(te.getSdate()!=null ||te.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(TESurvey te){
		if(te.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(te.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(TESurvey te){
		if(te.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(te.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(TESurvey te){
		if(te.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		TESurveyUser teUser = null;
		List<TESurveyUser> teUserList = teDAO.searchTESurveyUser(te, getCurUser(),  null, null);
		if(teUserList.size()>0)
			teUser = teUserList.get(0);
		
		if(teUser==null){
			if(isBeforeRatePeriod(te)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(te)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(teUser.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(te)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(te)){
				statusStudent = "Submission closed";
				return false;
			}

			if(teUser.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + teUser.getLastQuestionNum() + " of " + teUser.getSurvey().getQuestions().size();
			return true;
		}
	}

}
