package ntu.celt.eUreka2.pages.modules.big5;

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
import ntu.celt.eUreka2.modules.big5.BIG5DAO;
import ntu.celt.eUreka2.modules.big5.BIG5Question;
import ntu.celt.eUreka2.modules.big5.BIG5Survey;
import ntu.celt.eUreka2.modules.big5.BIG5SurveyUser;
import ntu.celt.eUreka2.modules.big5.BDimension;
import ntu.celt.eUreka2.modules.big5.BQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageBIG5{
	@Property
	private Project project;
	private long big5Id;
	@Property
	private BIG5Survey big5;
	@Property
	private BIG5SurveyUser big5User;
	@Property
	private List<BIG5SurveyUser> big5UserList;
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
	private BIG5Question que;
	@Property
	private boolean finished;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private BIG5DAO big5DAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		big5Id = ec.get(Long.class, 0);
		
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", big5Id));
		
		project = big5.getProject();
		
		
		
		big5UserList = big5DAO.searchBIG5SurveyUser(big5, getCurUser(), null,  null);
		
		if(big5UserList.size()==0){
			createMode = true;
			que = big5.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(big5UserList);
			if(redirectPage!=null)
				return redirectPage;
			big5User = big5UserList.get(0);
			que = big5.getQuestions().get(big5User.getLastQuestionNum());
			
			finished = big5User.isFinished();
		}
		
		assessees = new ArrayList<User>();
		assessees.add(getCurUser());
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {big5Id};
	}
	
	void setupRender(){
		if (!canSubmit(big5)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
				
	}
	void onPrepareForSubmitFromFormParticular(){

	}
	
	
	//assume big5UserList is not null
	Object checkExpiredDeleteRedirect(List<BIG5SurveyUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* BIG5SurveyUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		BIG5SurveyUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetBIG5SurveyUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetBIG5SurveyUsers(List<BIG5SurveyUser> puList){
		for(BIG5SurveyUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			big5DAO.saveBIG5SurveyUser(pu);
		}
	}
	
	public int getNumQuestionWithoutNumber(BIG5Survey big5){
		return big5.getQuestions().size();
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
		double percent = (double)(que.getNumber()-1) / big5.getQuestions().size();
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
	public boolean isThreeFourthWay(){
		double percent = Double.parseDouble(getProgressPercent());
		if(percent >= 75 && percent <= 75.4)
				return true;
		return false;
	}
	
	
	private BIG5SurveyUser getBIG5SurveyUserByAssessee(List<BIG5SurveyUser> puList, User u){
		for(BIG5SurveyUser pu : puList){
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
			
			BIG5SurveyUser pu = getBIG5SurveyUserByAssessee(big5UserList, u);
			if(pu==null){
				pu = new BIG5SurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(big5);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
			}
			
			int score = Integer.parseInt(selScore);
			int[] scores = distributeToScore(score, que);
			
			BQuestionScore qs = new BQuestionScore(que, score, scores, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == big5.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, big5.getId());
				
				for(int j=1; j<=BDimension.NUM_DIMENSIONS; j++){
					double avgScore = big5DAO.getAverageScoreByDimension(pu, j);
					if(avgScore != -1.0){
						pu.setScoreDim(avgScore, j);
					}
				}
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, big5.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				big5.addBig5Users(pu);
			}
		}
		big5DAO.updateBIG5Survey(big5);
		
		
		return pagelink;
	}
	
	private int[] distributeToScore(int score, BIG5Question que){
		int[] scores = new int[BDimension.NUM_DIMENSIONS];
		for(int i=0; i<BDimension.NUM_DIMENSIONS; i++){
			String dim = que.getDimension(i+1);
			if(dim==null)
				continue;
			if("R".equals(dim))
				scores[i] = BQuestionScore.MAX_SCORE+1 - score;
			else if("X".equals(dim))
				scores[i] = score;
		}
		
		return scores;
	}
	
	
	public String getSubmitText(){
		if(big5User==null)
			return "Next";
		if(big5User.getLastQuestionNum() == (big5.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}

	/*@CommitAfter
	public void onStartSurvey(Long big5Id)  {
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", big5Id));
		
		big5UserList = big5DAO.searchBIG5SurveyUser(big5, getCurUser(), getCurUser(), null);
		BIG5SurveyUser pu = getBIG5SurveyUserByAssessee(big5UserList, getCurUser());
		if(pu==null){
			pu = new BIG5SurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(big5);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			big5.addBig5Users(pu);
			big5DAO.updateBIG5Survey(big5);
		}
	
	}
	*/
	@CommitAfter
	public void onSuccessFromFormParticular(){
/*		particular.setUser(getCurUser());
		particular.setMdate(new Date());
		particular.setBriefDescriptionOfTheCrisis(Util.filterOutRestrictedHtmlTags(particular.getBriefDescriptionOfTheCrisis()));

		
		big5DAO.saveOrUpdateParticular(particular);
		
*/		
		big5 = big5DAO.getBIG5SurveyById(big5Id);
		if(big5==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", big5Id));
		
		big5UserList = big5DAO.searchBIG5SurveyUser(big5, getCurUser(), getCurUser(), null);
		BIG5SurveyUser pu = getBIG5SurveyUserByAssessee(big5UserList, getCurUser());
		if(pu==null){
			pu = new BIG5SurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(big5);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			big5.addBig5Users(pu);
			big5DAO.updateBIG5Survey(big5);
		}
	
	}
	
}
