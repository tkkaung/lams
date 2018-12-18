package ntu.celt.eUreka2.pages.modules.ipsp;

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
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPQuestion;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionScore;
import ntu.celt.eUreka2.modules.ipsp.IQuestionType;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;


public class AssesseeEdit extends AbstractPageIPSP{
	@Property
	private Project project;
	private long ipspId;
	@Property
	private IPSPSurvey ipsp;
	@Property
	private IPSPSurveyUser ipspUser;
	@Property
	private List<IPSPSurveyUser> ipspUserList;
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
	private IPSPQuestion que;
	@Property
	private boolean finished;
	@Property
	private Group group;
	private	int[] scores = new int[IDimension.NUM_DIMENSIONS];
	
	@SessionState
	private AppState appState;
	
	
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private UserDAO uDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	
	
	Object onActivate(EventContext ec) {
		ipspId = ec.get(Long.class, 0);
		
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", ipspId));
		
		project = ipsp.getProject();
		
		
		
		ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, getCurUser(), null, ipsp.getQuestionType(), null);
		
		if(ipspUserList.size()==0){
			createMode = true;
			que = ipsp.getQuestions().get(0);
		}
		else{
			Object redirectPage = checkExpiredDeleteRedirect(ipspUserList);
			if(redirectPage!=null)
				return redirectPage;
			ipspUser = ipspUserList.get(0);
			que = ipsp.getQuestions().get(ipspUser.getLastQuestionNum());
			
			finished = ipspUser.isFinished();
		}
		
		
		group = ipsp.getGroup();
		if(group!=null){
			assessees = getOtherStudentsInSameGroup(group, getCurUser());
		//	assessees.add(uDAO.getUserByUsername("student1")); //TEST ONLY
		}
		else{
			assessees = new ArrayList<User>();
			assessees.add(getCurUser());
		//	assessees.add(uDAO.getUserByUsername("student2")); //TEST ONLY
		}
		
		return null;
	}
	Object[] onPassivate() {
		return new Object[] {ipspId};
	}
	
	void setupRender(){
		if (!canSubmit(ipsp)){
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
				
	}
	void onPrepareForSubmitFromFormParticular(){

	}
	
	
	//assume ipspUserList is not null
	Object checkExpiredDeleteRedirect(List<IPSPSurveyUser> puList){
		if(puList.size()==0)
			return null;
		
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.add(Calendar.MINUTE, (-1)* IPSPSurveyUser.VALID_MINUTE_PER_QUESTION);
		
		//assume all pu.lastAssessTime are same
		IPSPSurveyUser pu1 = puList.get(0);
		if(!pu1.isFinished() && pu1.getLastQuestionNum()!=0 && pu1.getLastAssessTime().before(expiredTime.getTime())){
			resetIPSPSurveyUsers(puList);		
			//redirect
			appState.recordInfoMsg("Allowance Time exceeded, please try again");
			return (linkSource.createPageRenderLinkWithContext(AssesseeHome.class, project.getId()));
		}
		return null;
	}
	
	@CommitAfter
	private void resetIPSPSurveyUsers(List<IPSPSurveyUser> puList){
		for(IPSPSurveyUser pu : puList){
			
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			ipspDAO.saveIPSPSurveyUser(pu);
		}
	}
	
	public int getNumQuestionWithoutNumber(IPSPSurvey ipsp){
		return ipsp.getQuestions().size();
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
		double percent = (double)(que.getNumber()-1) / ipsp.getQuestions().size();
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
	
	
	private IPSPSurveyUser getIPSPSurveyUserByAssessee(List<IPSPSurveyUser> puList, User u){
		for(IPSPSurveyUser pu : puList){
			if(u.equals(pu.getAssessee())){
				return pu;
			}
		}
		return null;
	}
	
	
	
	@CommitAfter
	Object onSuccessFromForm() {
		Link pagelink = null ;
		String selScore = "0";
		int score = 0;
		for (User u : assessees) {
			
			for(int j=1; j<=IDimension.NUM_DIMENSIONS; j++){
				selScore = request.getParameter("cbx_" + Integer.toString(u.getId()) + "_" + j );
				score = Integer.parseInt(selScore);
				scores[j-1] = score;
			}
			
			IPSPSurveyUser pu = getIPSPSurveyUserByAssessee(ipspUserList, u);
	
			if(pu==null){
				pu = new IPSPSurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(ipsp);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
				createMode = true;
				
			}
			
			IQuestionScore qs = new IQuestionScore(que, score, scores, pu , pu.getLastQuestionNum()+1);
			pu.addQuestionScore(qs);
			pu.increaseLastQuestionNum();
			if(pu.getLastQuestionNum() == ipsp.getQuestions().size()){
				pu.setFinished(true);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeFinish.class, ipsp.getId());
				
				for(int j=1; j<=IDimension.NUM_DIMENSIONS; j++){
					double sumScore = (double) ipspDAO.getSumScoreByDimension(pu, j);
					if(sumScore != -1.0){
						pu.setScoreDim(sumScore, j);
					}
				}
			}
			else{
				pu.setFinished(false);
				pagelink = linkSource.createPageRenderLinkWithContext(AssesseeEdit.class, ipsp.getId());
			}
			pu.setLastAssessTime(new Date());
			
			if(createMode){
				ipsp.addIpspUsers(pu);
			}
			
		}

		ipspDAO.updateIPSPSurvey(ipsp);
		
		
		return pagelink;
	}
	
	/*private int[] distributeToScore(int score, IPSPQuestion que){
		int[] scores = new int[IDimension.NUM_DIMENSIONS];
		for(int i=0; i<IDimension.NUM_DIMENSIONS; i++){
			String dim = que.getDimension(i+1);
			if(dim==null)
				continue;
			if("R".equals(dim))
				scores[i] = IQuestionScore.MAX_SCORE+1 - score;
			else if("X".equals(dim))
				scores[i] = score;
		}
		
		return scores;
	}
	*/
	
	public String getSubmitText(){
		if(ipspUser==null)
			return "Next";
		if(ipspUser.getLastQuestionNum() == (ipsp.getQuestions().size() -1)){
			return "Finish";
		}
		return "Next";
			
	}
	

	/*@CommitAfter
	public void onStartSurvey(Long ipspId)  {
		ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", ipspId));
		
		ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, getCurUser(), getCurUser(), null);
		IPSPSurveyUser pu = getIPSPSurveyUserByAssessee(ipspUserList, getCurUser());
		if(pu==null){
			pu = new IPSPSurveyUser();
			pu.setAssessor(getCurUser());
			pu.setAssessee(getCurUser());
			pu.setSurvey(ipsp);
			pu.setStartAssessTime(new Date());
			pu.setResetCount(0);
			pu.setFinished(false);
			ipsp.addIpspUsers(pu);
			ipspDAO.updateIPSPSurvey(ipsp);
		}
	
	}
	*/
		@CommitAfter
	public void onSuccessFromFormParticular(){
			/*		particular.setUser(getCurUser());
		particular.setMdate(new Date());
		particular.setBriefDescriptionOfTheCrisis(Util.filterOutRestrictedHtmlTags(particular.getBriefDescriptionOfTheCrisis()));

		
		ipspDAO.saveOrUpdateParticular(particular);
		
*/		
		/*ipsp = ipspDAO.getIPSPSurveyById(ipspId);
		if(ipsp==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "Survey ID", ipspId));
		*/
		ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, getCurUser(), null, ipsp.getQuestionType(), null);
		for (User u : assessees) {
			
			IPSPSurveyUser pu = getIPSPSurveyUserByAssessee(ipspUserList, u);
			if(pu==null){
				pu = new IPSPSurveyUser();
				pu.setAssessor(getCurUser());
				pu.setAssessee(u);
				pu.setSurvey(ipsp);
				pu.setStartAssessTime(new Date());
				pu.setResetCount(0);
				pu.setFinished(false);
				ipsp.addIpspUsers(pu);
				ipspDAO.updateIPSPSurvey(ipsp);
			}
		}
	}
	
}
