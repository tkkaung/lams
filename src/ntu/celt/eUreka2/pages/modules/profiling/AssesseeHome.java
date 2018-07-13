package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

public class AssesseeHome extends AbstractPageProfiling{
	@Property
	private Project project;
	@Property
	private String pid;
	@Property
	private List<Profiling> profs;
	@SuppressWarnings("unused")
	@Property
	private Profiling prof;
	@Property
	private int rowIndex;
	@Property
	private List<User> groupMembers;
	@Property
	private User peer;
	@Property
	private Profiling profSELFpre;
	@Property
	private Profiling profSELFpost;
	@Property
	private Profiling profPEER;
	@Property
	private Profiling profINSTRUCTOR;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProjectDAO pDAO;
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private Messages messages;
	@Inject
	private BeanModelSource beanModelSource;

	
	void onActivate(String id) {
		pid = id;
		project = pDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectID", pid));
		
	}
	String onPassivate() {
		return pid;
	}
	
	
	
	
	
	void setupRender() {
		
		profs =  profDAO.getProfilingsByProject(project);
		if(profs.size()==4){
			profs.remove(3); //remove the ProfINSTRUCT
		}
		
		
		removeExpiredProfUser();
	}
	
	public boolean hasAtLeastOneReleased(){
		for(Profiling pro : profs){
			if(pro.getReleased())
				return true;
		}
		return false;
	}
	
	public BeanModel<Profiling> getModel() {
		BeanModel<Profiling> model = beanModelSource.createEditModel(Profiling.class, messages);
		model.include("name", "sdate");
		model.get("name").label("Profiling Assessment");
		model.get("sdate").label("Rating Period");
		
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
		if (profs == null)
			return 0;
		return profs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public boolean isSetsdateORedate(Profiling prof){
		if(prof.getSdate()!=null ||prof.getEdate()!=null)
			return true;
		return false;
	}
	
	public boolean isBeforeRatePeriod(Profiling prof){
		if(prof.getSdate()==null) 
			return true;
		
		Date now = new Date();
		if(now.before(prof.getSdate()))
			return true;
		return false;
	}
	public boolean isAfterRatePeriod(Profiling prof){
		if(prof.getEdate()==null) 
			return true;
		Date now = new Date();
		if(now.after(Util.getDate2359(prof.getEdate())))
			return true;
		return false;
	}
	
	
	@Property
	private String statusStudent = "";
	public boolean isShowLink(Profiling prof){
		if(prof.getQuestions().size()==0){
			statusStudent = "Not defined yet";
			return false;
		}
		
		ProfileUser profUser = null;
		List<ProfileUser> profUserList = profDAO.searchProfileUser(prof, getCurUser(), null, null, null);
		if(profUserList.size()>0)
			profUser = profUserList.get(0);
		
		if(profUser==null){
			if(isBeforeRatePeriod(prof)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(prof)){
				statusStudent = "Submission closed";
				return false;
			}
			
			statusStudent = "PENDING";
			return true;
		}
		else{
			if(profUser.isFinished()){
				statusStudent = "Submitted";
				return false;
			}

			if(isBeforeRatePeriod(prof)){
				statusStudent = "Not open yet";
				return false;
			}
			if(isAfterRatePeriod(prof)){
				statusStudent = "Submission closed";
				return false;
			}

			if(profUser.getLastQuestionNum()==0){
				statusStudent = "PENDING"; //Re-do
				return true;
			}
			
			statusStudent = "PENDING : " + profUser.getLastQuestionNum() + " of " + profUser.getProfile().getQuestions().size();
			return true;
		}
	}

}
