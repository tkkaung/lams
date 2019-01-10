package ntu.celt.eUreka2.pages.modules.teameffectiveness;

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
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDAO;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestion;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurvey;
import ntu.celt.eUreka2.modules.teameffectiveness.TESurveyUser;
import ntu.celt.eUreka2.modules.teameffectiveness.TEDimension;
import ntu.celt.eUreka2.modules.teameffectiveness.TEQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageTE{
	@Property
	private Project project;
	private long teId;
	@Property
	private TESurvey te;
	@Property
	private TESurveyUser teUser;
	@Property
	private List<TESurveyUser> teUserList;
	@Property
	private List<User> assessees;
	@Property
	private User user;
	@Property
	private boolean createMode;
	@Property
	private int rowIndex;
	@Property
	private int tempScore;
	@Property
	private TEQuestion que;
	@Property
	private boolean finished;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private TEDAO teDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		teId = ec.get(Long.class, 0);
		
		te = teDAO.getTESurveyById(teId);
		if(te==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", teId));
		
		project = te.getProject();
		
		
		
		teUserList = teDAO.searchTESurveyUser(te, getCurUser(), null,  null);
		
		if(teUserList.size()==0){
			createMode = true;
			que = te.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(teUserList);
			if(redirectPage!=null)
				return redirectPage;
			teUser = teUserList.get(0);
			que = te.getQuestions().get(teUser.getLastQuestionNum());
			
			finished = teUser.isFinished();
		}
		
		assessees = new ArrayList<User>();
		assessees.add(getCurUser());
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {teId};
	}
	
	void setupRender(){
		if (!canSubmit(te)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
				
	}
	void onPrepareForSubmitFromFormParticular(){

	}
	
	
	//assume teUserList is not null
	Object checkExpiredDeleteRedirect(List<TESurveyUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* TESurveyUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		TESurveyUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetTESurveyUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetTESurveyUsers(List<TESurveyUser> puList){
		for(TESurveyUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			teDAO.saveTESurveyUser(pu);
		}
	}
	
	public int getNumQuestionWithoutNumber(TESurvey te){
		return te.getQuestions().size();
		//TODO: consider to count without "Number" or not, e.g , it may show "120 of 118"
//		int total = 0;
//		for(ProfileQuestion q : prof.getQuestions()){
//			if(q.getNumber()!=null){
//				total++;
//			}
//		}
//		return total;
	}
	
	private int progressBarMaxWidth = 300;
	public String getProgressPercent(){
		double percent = (double)(que.getNumber()-1) / te.getQuestions().size();
		return Util.formatDecimal(percent * 100);
	}
	public int getProgressWidth(){	
		return (int) Math.round(Double.parseDouble(getProgressPercent()) * 0.01 * progressBarMaxWidth);
	}
	public int getProgressBarWidth(){
		return progressBarMaxWidth;
	}
	public boolean isHalfWay(){
		double percent = Double.parseDouble(getProgressPercent());
		if(percent >= 50 && percent <= 50.4)
				return true;
		return false;
	}
	public boolean isOneFourthWay(){
		double percent = Double.parseDouble(getProgressPercent());
		if(percent >= 25 && percent <= 25.4)
				return true;
		return false;
	}
	public boolean isFirstQuestionType(){
		if(que.getNumber()>37)
			return false;
		return true;		
	}
	
	public boolean isSecondQuestionType(){
		if(que.getNumber()>37)
			return true;
		return false;	
	}
	public boolean isThreeFourthWay(){
		double percent = Double.parseDouble(getProgressPercent());
		if(percent >= 75 && percent <= 75.4)
				return true;
		return false;
	}
	
	
	private TESurveyUser getTESurveyUserByAssessee(List<TESurveyUser> puList, User u){
		for(TESurveyUser pu : puList){
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
			
			TESurveyUser pu = getTESurveyUserByAssessee(teUserList, u);
			if(pu==null){
				pu = new TESurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(te);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
			}
			
			int score = Integer.parseInt(selScore);
			int[] scores = distributeToScore(score, que);
			
			TEQuestionScore qs = new TEQuestionScore(que, score, scores, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == te.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, te.getId());
				
				for(int j=1; j<=TEDimension.NUM_DIMENSIONS; j++){
					double avgScore = teDAO.getAverageScoreByDimension(pu, j);
					if(avgScore != -1.0){
						pu.setScoreDim(avgScore, j);
					}
				}
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, te.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				te.addTEUsers(pu);
			}
		}
		teDAO.updateTESurvey(te);
		
		
		return pagelink;
	}
	
	private int[] distributeToScore(int score, TEQuestion que){
		int[] scores = new int[TEDimension.NUM_DIMENSIONS];
		for(int i=0; i<TEDimension.NUM_DIMENSIONS; i++){
			String dim = que.getDimension(i+1);
			if(dim==null)
				continue;
			if("R".equals(dim))
				scores[i] = TEQuestionScore.MAX_SCORE+1 - score;
			else if("X".equals(dim))
				scores[i] = score;
		}
		
		return scores;
	}
	
	
	public String getSubmitText(){
		if(teUser==null)
			return "Next";
		if(teUser.getLastQuestionNum() == (te.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}

	/*@CommitAfter
	public void onStartSurvey(Long teId)  {
		te = teDAO.getTESurveyById(teId);
		if(te==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", teId));
		
		teUserList = teDAO.searchTESurveyUser(te, getCurUser(), getCurUser(), null);
		TESurveyUser pu = getTESurveyUserByAssessee(teUserList, getCurUser());
		if(pu==null){
			pu = new TESurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(te);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			te.addTEUsers(pu);
			teDAO.updateTESurvey(te);
		}
	
	}
	*/
	@CommitAfter
	public void onSuccessFromFormParticular(){
/*		particular.setUser(getCurUser());
		particular.setMdate(new Date());
		particular.setBriefDescriptionOfTheCrisis(Util.filterOutRestrictedHtmlTags(particular.getBriefDescriptionOfTheCrisis()));

		
		teDAO.saveOrUpdateParticular(particular);
		
*/		
		te = teDAO.getTESurveyById(teId);
		if(te==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", teId));
		
		teUserList = teDAO.searchTESurveyUser(te, getCurUser(), getCurUser(), null);
		TESurveyUser pu = getTESurveyUserByAssessee(teUserList, getCurUser());
		if(pu==null){
			pu = new TESurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(te);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			te.addTEUsers(pu);
			teDAO.updateTESurvey(te);
		}
	
	}
	
}
