package ntu.celt.eUreka2.pages.modules.lcdp;

import java.io.IOException;
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
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPQuestion;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurvey;
import ntu.celt.eUreka2.modules.lcdp.LCDPSurveyUser;
import ntu.celt.eUreka2.modules.lcdp.PQuestionScore;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageLCDP{
	@Property
	private Project project;
	private long lcdpId;
	@Property
	private LCDPSurvey lcdp;
	@Property
	private LCDPSurveyUser lcdpUser;
	@Property
	private List<LCDPSurveyUser> lcdpUserList;
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
	private LCDPQuestion que;
	@Property
	private boolean finished;
	
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private LCDPDAO lcdpDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		lcdpId = ec.get(Long.class, 0);
		
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
		if(lcdp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", lcdpId));
		
		project = lcdp.getProject();
		
		
		
		lcdpUserList = lcdpDAO.searchLCDPSurveyUser(lcdp, getCurUser(), null,  null);
		
		if(lcdpUserList.size()==0){
			createMode = true;
			que = lcdp.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(lcdpUserList);
			if(redirectPage!=null)
				return redirectPage;
			lcdpUser = lcdpUserList.get(0);
			que = lcdp.getQuestions().get(lcdpUser.getLastQuestionNum());
			
			finished = lcdpUser.isFinished();
		}
		
		assessees = new ArrayList<User>();
		assessees.add(getCurUser());
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {lcdpId};
	}
	
	void setupRender(){
		if (!canSubmit(lcdp)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
				
	}
	
	
	//assume lcdpUserList is not null
	Object checkExpiredDeleteRedirect(List<LCDPSurveyUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* LCDPSurveyUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		LCDPSurveyUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetLCDPSurveyUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetLCDPSurveyUsers(List<LCDPSurveyUser> puList){
		for(LCDPSurveyUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			lcdpDAO.saveLCDPSurveyUser(pu);
		}
	}
	
	public int getNumQuestionWithoutNumber(LCDPSurvey lcdp){
		return lcdp.getQuestions().size();
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
		double percent = (double)(que.getNumber()-1) / lcdp.getQuestions().size();
		return Util.formatDecimal(percent * 100);
	}
	public int getProgressWidth(){	
		return (int) Math.round(Double.parseDouble(getProgressPercent()) * 0.01 * progressBarMaxWidth);
	}
	public int getProgressBarWidth(){
		return progressBarMaxWidth;
	}
	public boolean isHalfWay(){
		if(Math.round(Double.parseDouble(getProgressPercent())) == 50)
				return true;
		return false;
	}
	
	
	private LCDPSurveyUser getLCDPSurveyUserByAssessee(List<LCDPSurveyUser> puList, User u){
		for(LCDPSurveyUser pu : puList){
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
			
			LCDPSurveyUser pu = getLCDPSurveyUserByAssessee(lcdpUserList, u);
			if(pu==null){
				pu = new LCDPSurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(lcdp);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
			}
			
			int score = Integer.parseInt(selScore);
			PQuestionScore qs = new PQuestionScore(que, score, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == lcdp.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, lcdp.getId());
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, lcdp.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				lcdp.addLcdpUsers(pu);
			}
		}
		lcdpDAO.updateLCDPSurvey(lcdp);
		
		
		return pagelink;
	}
	
	
	public String getSubmitText(){
		if(lcdpUser==null)
			return "Next";
		if(lcdpUser.getLastQuestionNum() == (lcdp.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}

	@CommitAfter
	public void onStartSurvey(Long lcdpId)  {
		lcdp = lcdpDAO.getLCDPSurveyById(lcdpId);
		if(lcdp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", lcdpId));
		
		lcdpUserList = lcdpDAO.searchLCDPSurveyUser(lcdp, getCurUser(), getCurUser(), null);
		LCDPSurveyUser pu = getLCDPSurveyUserByAssessee(lcdpUserList, getCurUser());
		if(pu==null){
			pu = new LCDPSurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(lcdp);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			lcdp.addLcdpUsers(pu);
			lcdpDAO.updateLCDPSurvey(lcdp);
		}
	
	}
}
