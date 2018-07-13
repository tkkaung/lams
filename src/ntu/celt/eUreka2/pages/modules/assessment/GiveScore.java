package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.AssmtUserCriteriaGrade;
import ntu.celt.eUreka2.modules.group.Group;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

public class GiveScore extends AbstractPageAssessment {
	@Property
	private Project project;
	private int aId;
	@Property
	private Assessment assmt;
	@Property
	private List<User> assessees;
	@SuppressWarnings("unused")
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempCrit;
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempCriterion; 
	@Property
	private int colIndex;
	@Property
	private int rowIndex;
	
	
	@SessionState
	private AppState appState;
	
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private AuditTrailDAO auditDAO;
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
	}
	Object[] onPassivate() {
		return new Object[] {aId};
	}
	
	void setupRender(){
		if(!canViewAssessmentGrade(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		assessees = getAllAssessees(project, assmt);
	}
	
	public int getColNum(){
		return colIndex+1;
	}
	public int getRowNum(){
		return rowIndex+1;
	}
	
	public String loadAssmtUser(Assessment assmt, User user){
		assmtUser = assmt.getAssmtUser(user);
		/*if(assmtUser == null){
			assmtUser = new AssessmentUser();
			assmtUser.setAssessedDate(new Date());
			assmtUser.setAssessee(user);
			assmtUser.setAssessment(assmt);
			assmtUser.setAssessor(getCurUser());
		}*/
		return ""; //to return empty string
	}
	
	public String getTempScore(){
		if (assmtUser!=null)
			return Util.formatDecimal(assmtUser.getTotalScore());
		return "";
	}
	public void setTempScore(String tempScore){
		return;
	}
	
	
	public boolean hasGraded(){
		if(assmtUser==null)
			return false;
		return true;
	}
	
	public float getTotalScore(){
		if(assmtUser==null)
			return 0;
		return assmtUser.getTotalScore();
	}
	public String getTotalScoreDisplay(){
		if((assmt.getCriterias()==null || assmt.getCriterias().isEmpty()) && assmtUser!=null)
			return Util.formatDecimal(getTotalScore());
			
		if(assmtUser==null || assmtUser.getSelectedCriterions().isEmpty())
			return "-";
		return Util.formatDecimal(getTotalScore());
	}
	public String getTotalScoreClass(){
		if((assmt.getCriterias()==null || assmt.getCriterias().isEmpty()) && assmtUser!=null)
			return convertScoreToGradeClass(getTotalScore(), 100);
		
		if(assmtUser==null || assmtUser.getSelectedCriterions().isEmpty())
			return null;
		return convertScoreToGradeClass(getTotalScore(), 100);
	}
	
	public String getJSArrCritID(){
		String str = "";
		for(AssessCriteria ac : assmt.getCriterias()){
			str += "'" + ac.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		str = "[" + str + "]";
		return str;
	}
	public String getJSArrUserID(){
		String str = "";
		for(User u : assessees){
			str += "'" + u.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		str = "[" + str + "]";
		return str;
	}
	
	/*public String getTotalAvgGradeClass(){
		return convertScoreToGradeClass(getTotalAverageScore(), 100); 
	}
	public String getTotalGradeClass(){
		if(assmtUser==null)
			return null;
		return convertScoreToGradeClass(assmtUser.getTotalScore(), 100); 
	}
	public String getAverageScore(AssessCriteria crit){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				total += Float.parseFloat(getComputedCritScoreDisplay(crit,assmtUser));
				i++;
			}
		}
		if(i==0)
			return "";
		
		return Util.formatDecimal(total/i);
	}
	public String getAvgGradeClass(AssessCriteria crit){
		String avgScore = getAverageScore(crit);
		if("".equals(avgScore)){
			return null;
		}
		return convertScoreToGradeClass(Float.parseFloat(avgScore), crit.getWeightage()); 
	}
	public float getTotalAverageScore(){
		float total = 0;
		int i =0;
		for(User u : assessees){
			loadAssmtUser(assmt, u);
			if(assmtUser!=null){
				total += assmtUser.getTotalScore();
				i++;
			}
		}
		if(i==0)
			return 0;
		return total/i;
	}
	public String getTotalAvgScoreDisplay(){
		return Util.formatDecimal(getTotalAverageScore());
	}*/
	public String getDisplayTotal(double dd){
		return Util.formatDecimal(dd); 
	}
	
	
	private int initWidth = 330;//330;
	private int pageWidth = 974;//974;
	private int padding = 4;
	public int getCritNameWidth(){
		int numColum = assmt.getCriterias().size();
		int minwidth = 100;
		if(numColum==0) //in case not-use-rubric
			return 1;
		
		int num = Math.round((pageWidth - initWidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getTableWidth(){
		int numColum = assmt.getCriterias().size();
		if(numColum==0) //in case not-use-rubric
			return "99%";
		
		int width = initWidth + getCritNameWidth()*numColum + padding ;
		if(pageWidth < width)
			return (width-30) +"px";
		return "98%";
	}
	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
	public int getNumCritCols(){
		if(assmt.getCriterias()==null || assmt.getCriterias().size()==0)
			return 1;
		return assmt.getCriterias().get(0).getCriterions().size();
	}
	
	public int getTotalNumCriterions() {
		return assmt.getCriterias().size() * assmt.getCriterias().get(0).getCriterions().size();
	}
	public int getNumCriterions() {
		return assmt.getCriterias().get(0).getCriterions().size();
	}
	public String getSelectedClass(AssessCriterion crion){
		if(assmtUser ==null)
			return "";
		if(assmtUser.getSelectedCriterions().contains(crion))
				return "selected";
		return "";
	}
	public String getSelectedClassGMATL(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "L");
	}
	public String getSelectedClassGMATH(AssessCriterion crion){
		return getSelectedClassGMAT(crion, "H");
	}
	public String getSelectedClassGMAT(AssessCriterion crion, String HL){
		if(assmtUser == null )
			return "";
		int index = assmtUser.getSelectedCriterions().indexOf(crion);
		if(index != -1){
			if(assmtUser.getSelectedGMATHL().size()>index){
				if(HL.equals(assmtUser.getSelectedGMATHL().get(index)))
					return "selected";
			}
		}
		return "";
	}
	
	@CommitAfter
	Object onSuccessFromForm() {
		String prevAuditValue = "";
		String newAuditValue = "";

		if (assessees == null) assessees = getAllAssessees(project, assmt);
		for (User u : assessees) {
			Float prevScore = null;
			
			AssessmentUser assmtU = assmt.getAssmtUser(u);
			boolean createMode = false;
			if (assmtU == null) {
				createMode = true;
				assmtU = new AssessmentUser();
				assmtU.setStudentViewComments(true);
			}
			else{
				prevScore = assmtU.getTotalScore();
			}
			
			if (assmt.getCriterias().isEmpty()) { //no rubric

				String totalScoreStr = request.getParameter("totalScore_"+ u.getId() );
				if(totalScoreStr==""){
					if (createMode){
						//do nothing
					}
					else{
						logger.info(getCurUser().getUsername() + " clear grade for u="+ u.getUsername() 
								+ ", assmt="+assmt.getId() 
								+ " oldComment=" + assmtU.getComments());
						assmt.removeAssmtUsers(assmtU);
						assmtDAO.updateAssessment(assmt);
					}
				}
				else{ //if has Score to save
					float totalScore = Float.parseFloat(totalScoreStr);
					assmtU.setTotalScore(totalScore);
					assmtU.setTotalGrade(convertScoreToGrade(totalScore, 100));
					
					assmtU.setAssessedDate(new Date());
					assmtU.setAssessee(u);
					assmtU.setAssessment(assmt);
					assmtU.setAssessor(getCurUser());
	
					if(createMode){
						assmtDAO.saveAssessmentUser(assmtU);
						assmt.addAssmtUsers(assmtU);
						assmtDAO.updateAssessment(assmt);
	//					return linkSource.createPageRenderLinkWithContext(Home.class, project.getId());
					}
					else{
						assmtDAO.updateAssessment(assmt);
	//					return linkSource.createPageRenderLinkWithContext(ViewGrade.class, assmt.getId(), user.getId());
					}
				}
			}
			else { //if has Rubric
				
				float total = 0;
				String selCriterions = request.getParameter(Integer.toString(u.getId()) + "_selCriterions");
				List<AssessCriterion> acList = new ArrayList<AssessCriterion>();
				List<String> hlList = new ArrayList<String>();
				
				if(selCriterions!=null && !selCriterions.isEmpty()){
					String crionIDs[] = selCriterions.split(",");
					if(assmt.getGmat()){//if GMAT
						for(String crionID : crionIDs){
							String crionIDsGMAT[] = crionID.split("_");
							long crID = Long.parseLong(crionIDsGMAT[0]);
							AssessCriterion acrion = assmtDAO.getAssessCriterionById(crID);
							acList.add(acrion);
							hlList.add(crionIDsGMAT[1]);
							String gmatNum = acrion.getScore() + "-" + crionIDsGMAT[1];
								
							AssessCriteria acrit = acrion.getCriteria();
							total += ((float) convertGMATtoScore(gmatNum) * acrit.getWeightage() / 100 );
						}
					}
					else{ //if not GMAT
						for(String crionID : crionIDs){
							long crID = Long.parseLong(crionID);
							AssessCriterion acrion = assmtDAO.getAssessCriterionById(crID);
							acList.add(acrion);
							AssessCriteria acrit = acrion.getCriteria();
							total += ((float) acrion.getScore() * acrit.getWeightage() / acrit.getMaxCritScore() );
						}
					}
					
				}
				
				if(assmtU.getSelectedCriterions().size()!=0 && acList.size()==0){
					String msg = "There might be an error in your submission. " +
						"your score submission has not been saved. If you are trying to clear all your grades, please click on the Clear Grade button." +
						" student: "+ u.getDisplayName() + 
						"<br/> If the problem persists, please contact administrator. " 

					;
					appState.recordErrorMsg(msg);
					logger.error(msg);
					continue;
				}
				assmtU.getSelectedCriterions().clear();  
				assmtU.setSelectedCriterions(acList);
				assmtU.getSelectedGMATHL().clear();
				assmtU.setSelectedGMATHL(hlList);
				
				if(assmt.getGmat()){
					List<AssmtUserCriteriaGrade> aucgList = new ArrayList<AssmtUserCriteriaGrade>();
					int i = 0;
					for(AssessCriterion acrion : acList){
						AssmtUserCriteriaGrade aucg = new AssmtUserCriteriaGrade();
						aucg.setCriteria(acrion.getCriteria());
						String gmatNum = acrion.getScore() + "-" + hlList.get(i);
							
						float s = (float) convertGMATtoScore(gmatNum);
						aucg.setScore(s);
						
						aucgList.add(aucg);
						i++;
					}
					assmtU.getSelectedCritGrades().clear(); 
					assmtU.setSelectedCritGrades(aucgList);
				}
				
				total = Float.parseFloat(Util.formatDecimal(total)); //store as Rounded value
				assmtU.setTotalScore(total);
				if(assmt.getGmat()){//if GMAT
					assmtU.setTotalGrade(convertScoreToGrade(total, 100));
				}
				else{
					assmtU.setTotalGrade(convertScoreToGrade(total, assmt.getPossibleScore()));
				}
				
				assmtU.setAssessedDate(new Date());
				assmtU.setAssessee(u);
				assmtU.setAssessment(assmt);
				assmtU.setAssessor(getCurUser());

				if(createMode){
					assmtDAO.saveAssessmentUser(assmtU);
					assmt.addAssmtUsers(assmtU);
					assmtDAO.updateAssessment(assmt);
//					return linkSource.createPageRenderLinkWithContext(Home.class, project.getId());
				}
				else{
					assmtDAO.updateAssessment(assmt);
//					return linkSource.createPageRenderLinkWithContext(ViewGrade.class, assmt.getId(), user.getId());
				}
			}
			
			Float newScore = assmtU.getTotalScore();
			if(!newScore.equals(prevScore)){
				prevAuditValue += u.getDisplayName() + " Score: " + (prevScore==null ? "" : prevScore)
					+ System.lineSeparator();
				newAuditValue += u.getDisplayName() + " Score: " + newScore
						+ System.lineSeparator();
				
			}
		}
			
	
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Grade Students", getCurUser());
		if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
			ad.setPrevValue(prevAuditValue);
			ad.setNewValue(newAuditValue);
			
			auditDAO.saveAuditTrail(ad);
		}
		
		return linkSource.createPageRenderLinkWithContext(Score.class, assmt.getId());
	}
	
	
	@SuppressWarnings("unused")
	@Property
	private String curGroupNumber ="";
	public String loadGroupNum(Group group, User user){
		curGroupNumber = getGroupTypeNumber(group, user);
		return "";
	}
	
	
	@CommitAfter
	public Object onActionFromClearGrade(int aId){
		assmt = assmtDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		
		project = assmt.getProject();
		assessees = getAllAssessees(project, assmt);
		
		String prevAuditValue = "";
		
		for (User u : assessees) {
			assmtUser = assmt.getAssmtUser(u);
			if(assmtUser == null)
				continue;
			
			/*//keep old grade as backup
			String oldSelectedCritGrades = "";
			for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
				oldSelectedCritGrades += aucg.getScore() + ", " + aucg.getComment() + ";;";
			}
			String oldSelectedCriterions = "";
			for(AssessCriterion acrion : assmtUser.getSelectedCriterions()){
				oldSelectedCriterions += acrion.getId()  + ",";
			}
			String oldComment = assmtUser.getComments();
			*/
			prevAuditValue += "Student: " + u.getDisplayName();
			int i = 1;
			for (AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
				prevAuditValue += "-Criteria_" + i + ", score: " + aucg.getScore() + ", comment: " + aucg.getComment() + System.lineSeparator();
				i++;
			}
			prevAuditValue += "-Total Score:" + assmtUser.getTotalScore() + System.lineSeparator();
			prevAuditValue += "-Total Grade:" + assmtUser.getTotalGrade() + System.lineSeparator();
			prevAuditValue += "-Comment:" + assmtUser.getComments() + System.lineSeparator();
			
			
			assmtUser.getSelectedCriterions().clear();
			assmtUser.getSelectedGMATHL().clear();
			assmtUser.getSelectedCritGrades().clear();
			assmtUser.setTotalGrade("-");
			assmtUser.setTotalScore(0);
			assmtUser.setComments("");
			assmtUser.setAssessedDate(new Date());
			assmtDAO.saveAssessmentUser(assmtUser);
			assmt.removeAssmtUsers(assmtUser);
			logger.info(getCurUser().getUsername() + " clear grade for u="+ u.getUsername() + ", assmt="+assmt.getId() );
					//+ " oldSelectedCritGrades=" + oldSelectedCritGrades
					//+ " oldSelectedCriterions=" + oldSelectedCriterions
					//+ " oldComment=" + oldComment)
					;
			
			
		}
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Clear Grade all Students " , getCurUser());
		ad.setPrevValue(prevAuditValue);
		ad.setNewValue("");
		auditDAO.saveAuditTrail(ad);
		
//		assmtDAO.updateAssessment(assmt);
		
		return linkSource.createPageRenderLinkWithContext(Score.class, assmt.getId());
		
	}
}
