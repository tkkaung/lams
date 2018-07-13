package ntu.celt.eUreka2.pages.modules.care;

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
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CAREParticular;
import ntu.celt.eUreka2.modules.care.CAREQuestion;
import ntu.celt.eUreka2.modules.care.CARESurvey;
import ntu.celt.eUreka2.modules.care.CARESurveyUser;
import ntu.celt.eUreka2.modules.care.CDimension;
import ntu.celt.eUreka2.modules.care.CQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageCARE{
	@Property
	private Project project;
	private long careId;
	@Property
	private CARESurvey care;
	@Property
	private CARESurveyUser careUser;
	@Property
	private List<CARESurveyUser> careUserList;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private boolean createMode;
	@Property
	private int rowIndex;
	@Property
	private int tempScore;
	@Property
	private CAREQuestion que;
	@Property
	private boolean finished;
	@Property
	private CAREParticular particular;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private CAREDAO careDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		careId = ec.get(Long.class, 0);
		
		care = careDAO.getCARESurveyById(careId);
		if(care==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", careId));
		
		project = care.getProject();
		
		
		
		careUserList = careDAO.searchCARESurveyUser(care, getCurUser(), null,  null);
		
		if(careUserList.size()==0){
			createMode = true;
			que = care.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(careUserList);
			if(redirectPage!=null)
				return redirectPage;
			careUser = careUserList.get(0);
			que = care.getQuestions().get(careUser.getLastQuestionNum());
			
			finished = careUser.isFinished();
		}
		
		assessees = new ArrayList<User>();
		assessees.add(getCurUser());
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {careId};
	}
	
	void setupRender(){
		if (!canSubmit(care)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		particular = careDAO.getParticularByUser(getCurUser());
		if(particular==null){
			particular = new CAREParticular();
			particular.setUser(getCurUser());
		}
				
	}
	void onPrepareForSubmitFromFormParticular(){
		particular = careDAO.getParticularByUser(getCurUser());
		if(particular==null){
			particular = new CAREParticular();
			particular.setUser(getCurUser());
		}

	}
	
	
	//assume careUserList is not null
	Object checkExpiredDeleteRedirect(List<CARESurveyUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* CARESurveyUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		CARESurveyUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetCARESurveyUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetCARESurveyUsers(List<CARESurveyUser> puList){
		for(CARESurveyUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			careDAO.saveCARESurveyUser(pu);
		}
	}
	
	public int getNumQuestionWithoutNumber(CARESurvey care){
		return care.getQuestions().size();
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
		double percent = (double)(que.getNumber()-1) / care.getQuestions().size();
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
	
	
	private CARESurveyUser getCARESurveyUserByAssessee(List<CARESurveyUser> puList, User u){
		for(CARESurveyUser pu : puList){
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
			
			CARESurveyUser pu = getCARESurveyUserByAssessee(careUserList, u);
			if(pu==null){
				pu = new CARESurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(care);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
			}
			
			int score = Integer.parseInt(selScore);
			int[] scores = distributeToScore(score, que);
			
			CQuestionScore qs = new CQuestionScore(que, score, scores, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == care.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, care.getId());
				
				for(int j=1; j<=CDimension.NUM_DIMENSIONS; j++){
					double avgScore = careDAO.getAverageScoreByDimension(pu, j);
					if(avgScore != -1.0){
						pu.setScoreDim(avgScore, j);
					}
				}
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, care.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				care.addCareUsers(pu);
			}
		}
		careDAO.updateCARESurvey(care);
		
		
		return pagelink;
	}
	
	private int[] distributeToScore(int score, CAREQuestion que){
		int[] scores = new int[CDimension.NUM_DIMENSIONS];
		for(int i=0; i<CDimension.NUM_DIMENSIONS; i++){
			String dim = que.getDimension(i+1);
			if(dim==null)
				continue;
			if("R".equals(dim))
				scores[i] = CQuestionScore.MAX_SCORE+1 - score;
			else if("X".equals(dim))
				scores[i] = score;
		}
		
		return scores;
	}
	
	
	public String getSubmitText(){
		if(careUser==null)
			return "Next";
		if(careUser.getLastQuestionNum() == (care.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}

	/*@CommitAfter
	public void onStartSurvey(Long careId)  {
		care = careDAO.getCARESurveyById(careId);
		if(care==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", careId));
		
		careUserList = careDAO.searchCARESurveyUser(care, getCurUser(), getCurUser(), null);
		CARESurveyUser pu = getCARESurveyUserByAssessee(careUserList, getCurUser());
		if(pu==null){
			pu = new CARESurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(care);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			care.addCareUsers(pu);
			careDAO.updateCARESurvey(care);
		}
	
	}
	*/
	@CommitAfter
	public void onSuccessFromFormParticular(){
		particular.setUser(getCurUser());
		particular.setMdate(new Date());
		particular.setBriefDescriptionOfTheCrisis(Util.filterOutRestrictedHtmlTags(particular.getBriefDescriptionOfTheCrisis()));

		
		careDAO.saveOrUpdateParticular(particular);
		
		
		care = careDAO.getCARESurveyById(careId);
		if(care==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", careId));
		
		careUserList = careDAO.searchCARESurveyUser(care, getCurUser(), getCurUser(), null);
		CARESurveyUser pu = getCARESurveyUserByAssessee(careUserList, getCurUser());
		if(pu==null){
			pu = new CARESurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(care);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			care.addCareUsers(pu);
			careDAO.updateCARESurvey(care);
		}
	
	}
	
}
