package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.RedirectException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.profiling.LQuestionType;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

public class TutorHome extends AbstractPageProfiling{
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
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private int rowIndex;
	@Property
	private ProfileUser profUser;
	@Property
	private Profiling profPEER;
	@Property
	private Profiling profINSTRUCTOR;

	
	//@Persist("flash")
	/*@Persist
	@Property
	private Integer curPageNo;*/
	
	@SessionState
	private AppState appState;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private Messages messages;
	@Inject
	private PageRenderLinkSource linkSource;
	
	Object onActivate(String id) {
		pid = id;
		
		project = projDAO.getProjectById(pid);
		if(project==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "ProjectId", pid));
		
		profs =  profDAO.getProfilingsByProject(project);
		if(profs.size()==0){
			return (linkSource.createPageRenderLinkWithContext(Home.class, pid));			
		}
		return null;
	}
	String onPassivate() {
		return pid;
	}
	
	
	void setupRender() {
		
		profPEER = profs.get(2);
		profINSTRUCTOR = profs.get(3);
		
		if(!isTutor(profPEER.getGroup(), getCurUser())){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		assessees = getAllAssesseesByTutor(project, profPEER.getGroup(), getCurUser());
	
		removeExpiredProfUser();
	}
	
	
	
	
	
	public int getRowNum(){
	//	return (curPageNo-1)*getRowsPerPage()+ rowIndex + 1;
		return  rowIndex + 1;
		
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	public int getTotalSize(){
		return assessees.size();
	}
	
	
	public ProfileUser loadProfUser(Profiling prof, User assessor, User assessee){
		List<ProfileUser> profUserList = profDAO.searchProfileUser(prof, assessor, assessee, null, null);
		if(profUserList.size()>0)
			profUser = profUserList.get(0);
		else
			profUser = null;
		return null;
	}
	public boolean hasGraded(){
		if(profUser==null)
			return false;
		return true;
	}

}
