package ntu.celt.eUreka2.pages.modules.profiling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.profiling.LQuestionType;
import ntu.celt.eUreka2.modules.profiling.ProfileQuestion;
import ntu.celt.eUreka2.modules.profiling.ProfileUser;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.QuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageProfiling{
	@Property
	private Project project;
	private long profId;
	@Property
	private Profiling prof;
	@Property
	private ProfileUser profUser;
	@Property
	private List<ProfileUser> profUserList;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private Group group;
	private boolean createMode;
	@Property
	private int rowIndex;
	@Property
	private int tempScore;
	@Property
	private ProfileQuestion que;
	@Property
	private boolean finished;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private ProfilingDAO profDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		profId = ec.get(Long.class, 0);
		
		prof = profDAO.getProfilingById(profId);
		if(prof==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Profiling ID", profId));
		
		project = prof.getProject();
		
		
		
		profUserList = profDAO.searchProfileUser(prof, getCurUser(), null, null, null);
		if(profUserList.size()==0){
			createMode = true;
			que = prof.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(profUserList);
			if(redirectPage!=null)
				return redirectPage;
			profUser = profUserList.get(0);
			que = prof.getQuestions().get(profUser.getLastQuestionNum());
			
			finished = profUser.isFinished();
		}
		
		group = prof.getGroup();
		if(group!=null){
			assessees = getOtherStudentsInSameGroup(group, getCurUser());
		//	assessees.add(uDAO.getUserByUsername("student1")); //TEST ONLY
		//	assessees.add(uDAO.getUserByUsername("somsak")); //TEST ONLY
		}
		else{
			assessees = new ArrayList<User>();
			assessees.add(getCurUser());
		//	assessees.add(uDAO.getUserByUsername("student2")); //TEST ONLY
		}
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {profId};
	}
	
	void setupRender(){
		if (!canSubmit(prof)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
				
	}
	
	
	//assume profUserList is not null
	Object checkExpiredDeleteRedirect(List<ProfileUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* ProfileUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		ProfileUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetProfUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetProfUsers(List<ProfileUser> puList){
		for(ProfileUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			profDAO.saveProfileUser(pu);
		}
	}
	
	
	public boolean isPeerProfiling(){
		if(prof.getQuestionType() == LQuestionType.PEER)
			return true;
		return false;
	}
	
	public int getNumQuestionWithoutNumber(Profiling prof){
		return prof.getQuestions().size();
		//TODO: consider to count without "Number" or not, e.g , it may show "120 of 118"
//		int total = 0;
//		for(ProfileQuestion q : prof.getQuestions()){
//			if(q.getNumber()!=null){
//				total++;
//			}
//		}
//		return total;
	}
	
//	@Component()
//	private Form form;
//	void onValidateFromFromForm(){
//		boolean selectAllField = true;
//		for (User u : assessees) {
//			String selScore = request.getParameter("rdo_" + Integer.toString(u.getId()) );
//			if(selScore==null || selScore.isEmpty()){
//				selectAllField = false;
//				break;
//			}
//		}
//		
//		if(!selectAllField){
//			form.recordError("Some answer not selected yet, please select again");
//		}
//	}
	private ProfileUser getProfUserByAssessee(List<ProfileUser> puList, User u){
		for(ProfileUser pu : puList){
			if(u.equals(pu.getAssessee())){
				return pu;
			}
		}
		return null;
	}
	
	@CommitAfter
	Object onSuccessFromForm() {
		Link pagelink = null ;
		for (User u : assessees) {
			String selScore = request.getParameter("rdo_" + Integer.toString(u.getId()) );
			
			ProfileUser pu = getProfUserByAssessee(profUserList, u);
			if(pu==null){
				pu = new ProfileUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setProfile(prof);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
			}
			
			int score = Integer.parseInt(selScore);
			if(que.isReverseScore())
				score = 8 - score;
			QuestionScore qs = new QuestionScore(que, score, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == prof.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, prof.getId());
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, prof.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				prof.addProfUsers(pu);
			}
		}
		profDAO.updateProfiling(prof);
		
		
		return pagelink;
	}
	
	
	public String getSubmitText(){
		if(profUser==null)
			return "Next";
		if(profUser.getLastQuestionNum() == (prof.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}

}
