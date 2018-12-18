package ntu.celt.eUreka2.pages.modules.big5;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.big5.BIG5SurveyUser;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class AssesseeHome extends AbstractPageBIG5{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<BIG5Survey> big5s;
	@SuppressWarnings("unused")
	@Property
	private BIG5Survey big5;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private BIG5DAO big5DAO;
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
		
		big5s =  big5DAO.getBIG5SurveysByProject(project);
		
		if(big5s.size()==1 ){
			big5 = big5s.get(0);
			if(isShowLink(big5)){ //PENDING
				return (linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, big5.getId()));
			}
		}
		
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		
		
		
		
		removeExpiredBIG5SurveyUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(BIG5Survey pro : big5s){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<BIG5Survey> getModel() {
		BeanModel<BIG5Survey> model = beanModelSource.createEditModel(BIG5Survey.class, messages);
		model.include("name", "sdate");
		model.get("name").label("BIG5 Survey");
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
		if (big5s == null)
			return 0;
		return big5s.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(BIG5Survey big5){
		if(big5.getSdate()!=null ||big5.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(BIG5Survey big5){
		if(big5.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(big5.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(BIG5Survey big5){
		if(big5.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(big5.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(BIG5Survey big5){
		if(big5.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		BIG5SurveyUser big5User = null;
		List<BIG5SurveyUser> big5UserList = big5DAO.searchBIG5SurveyUser(big5, getCurUser(),  null, null);
		if(big5UserList.size()>0)
			big5User = big5UserList.get(0);
		
		if(big5User==null){
			if(isBeforeRatePeriod(big5)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(big5)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(big5User.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(big5)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(big5)){
				statusStudent = "Submission closed";
				return false;
			}

			if(big5User.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + big5User.getLastQuestionNum() + " of " + big5User.getSurvey().getQuestions().size();
			return true;
		}
	}

}
