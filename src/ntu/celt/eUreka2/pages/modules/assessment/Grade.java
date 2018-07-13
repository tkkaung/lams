package ntu.celt.eUreka2.pages.modules.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;


@Import(library={"${tapestry.scriptaculous}/controls.js"
		//,"context:lib/js/scriptaculous.1.9.0.js"
		//,"context:lib/js/prototype.1.7.js"
		,"context:lib/js/ie10loadjs.js"
		,"context:lib/js/slider-custom.js"
		}, 
		stylesheet="context:lib/css/slider.css")
public class Grade extends AbstractPageAssessment{
	@Property
	private Project project;
	private int aId;
	private int uId;
	@Property
	private Assessment assmt;
	@Property
	private User user;
	@Property
	private AssessmentUser assmtUser;
	
	@SuppressWarnings("unused")
	@Property
	private AssessCriteria tempRCrit; 
	@SuppressWarnings("unused")
	@Property
	private AssessCriterion tempRCriterion; 
	@SuppressWarnings("unused")
	@Property
	private int rowIndex;
	@SuppressWarnings("unused")
	@Property
	private int colIndex;
	private boolean createMode;
	@Property
	private boolean groupGrading = true;
	@Property
	private String rubricOrder;
	
	private Float prevTotalScore;
	private String prevComment;
	private List<AssmtUserCriteriaGrade> prevAUCGs;
	
	
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private UserDAO uDAO;
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
	@SessionState
	private AppState appState;
	
	
	void onActivate(EventContext ec) {
		aId = ec.get(Integer.class, 0);
		uId = ec.get(Integer.class, 1);
		
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		user = uDAO.getUserById(uId);
		if(user==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		project = assmt.getProject();
		assmtUser = assmt.getAssmtUser(user);
		if(assmtUser==null){
			createMode = true;
			assmtUser = new AssessmentUser();
			assmtUser.setAssessee(user);
			assmtUser.setAssessment(assmt);
			assmtUser.setAssessor(getCurUser());
			assmtUser.setStudentViewComments(true);
		}
		rubricOrder = getRubricOrderBy(assmt);
	}
	Object[] onPassivate() {
		return new Object[] {aId, uId};
	}
	
	
	void setupRender(){
		if(!canGradeAssessment(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
	}
	
	void onPrepareForSubmitFromForm(){
		//keep previous score for Audit log check
		prevTotalScore = assmtUser.getTotalScore();
		prevComment = assmtUser.getComments();
		prevAUCGs = new ArrayList<AssmtUserCriteriaGrade>();
		for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
			prevAUCGs.add(aucg.clone());
		}
	}
	
	public boolean isCreateMode(){
		return createMode;
	}
	
	@SuppressWarnings("unused")
	@Component(id = "Form")
	private Form form;
	void onValidateFormFromForm(){
		if(assmt.getCriterias().isEmpty()){ //input grade manually
		//	if(assmtUser.getTotalScore()>assmt.getPossibleScore())
		//		form.recordError(messages.get("totalScore-label") + " " + messages.get("cant-be-bigger-than")+ " "+assmt.getPossibleScore());
		}
		else{
	/*		String selCriterions = request.getParameter("selCriterions");
			if(selCriterions==null ){
				form.recordError(messages.get("must-select-criterion"));
				return;
			}
			//Map<critId , criterionCount>
			Map<Integer,Integer> critCMap = new HashMap<Integer, Integer>();
			String crionIDs[] = selCriterions.split(",");
			for(String crionID : crionIDs){
				long crID = Long.parseLong(crionID);
				AssessCriterion acrion = aDAO.getAssessCriterionById(crID);
				if(acrion==null){
					form.recordError(messages.format("entity-not-exists", "CriterionID", crID));
					return;
				}
				if(!acrion.getCriteria().getAssessment().equals(assmt)){
					form.recordError(messages.format("invalid-criterion", crID));
					return;
				}
				//check all row must selected only once
				if(critCMap.containsKey(acrion.getCriteria().getId())){
					form.recordError(messages.get("Cant-select-multiple-criterions-per-crit"));
					return;
				}
				critCMap.put(acrion.getCriteria().getId(), 1);
			}
			//check all row must be selected
			for(AssessCriteria ac : assmt.getCriterias()){
				if(!critCMap.containsKey(ac.getId())){
					form.recordError(messages.format("criteria-x-not-selected"));
				}
			}
	*/		
		}
	}
	
	@CommitAfter
	Object onSuccessFromForm(){
		String prevAuditValue = "";
		String newAuditValue = "";

		if(assmt.getCriterias().isEmpty()){ //input grade manually
			//do nothing
		}
		else{
			float total = 0;
			String selCriterions = request.getParameter("selCriterions");
			List<AssessCriterion> acList = new ArrayList<AssessCriterion>();
			List<String> hlList = new ArrayList<String>();
			
			if(selCriterions!=null && !selCriterions.isEmpty()){
				String crionIDs[] = selCriterions.split(",");
				if(assmt.getGmat()){//if GMAT
					for(String crionID : crionIDs){
						String crionIDsGMAT[] = crionID.split("_");
						long crID = Long.parseLong(crionIDsGMAT[0]);
						AssessCriterion acrion = aDAO.getAssessCriterionById(crID);
						acList.add(acrion);
						hlList.add(crionIDsGMAT[1]);
					}
				}
				else{
					for(String crionID : crionIDs){
						long crID = Long.parseLong(crionID);
						AssessCriterion acrion = aDAO.getAssessCriterionById(crID);
						acList.add(acrion);
					}
				}
			}
			if(assmtUser.getSelectedCriterions().size()!=0 && acList.size()==0 ){
				String msg = "There might be an error in your submission. " +
						"your score submission has not been saved. If you are trying to clear all your grades, please click on the Clear Grade button." +
				"<br/> If the problem persists, please contact administrator " 
				;
				appState.recordErrorMsg(msg);
				logger.error(msg);
				return null;
			}
			
			assmtUser.getSelectedCriterions().clear(); 
			assmtUser.setSelectedCriterions(acList);
			assmtUser.getSelectedGMATHL().clear(); 
			assmtUser.setSelectedGMATHL(hlList);
			
			List<AssmtUserCriteriaGrade> aucgList = new ArrayList<AssmtUserCriteriaGrade>();
			for(AssessCriteria acrit : assmt.getCriterias()){
				AssmtUserCriteriaGrade aucg = new AssmtUserCriteriaGrade();
				aucg.setCriteria(acrit);
				
				String criteriaComment = request.getParameter("cmmt_" + acrit.getId());
				aucg.setComment(criteriaComment);
				
				String selCriteriaScore = request.getParameter("selCritScore_" + acrit.getId());
				//logger.error(".....selCriteriaScore="+ selCriteriaScore + " acrit.getId=" + acrit.getId() + " criteriaComment="+criteriaComment);	
				
				if(selCriteriaScore!=null && !selCriteriaScore.isEmpty()){
					if ("-".equals(selCriteriaScore)){
						aucg.setScore(null);
					}
					else{
						float s = Float.parseFloat(selCriteriaScore);
						aucg.setScore(s);
						if(assmt.getGmat()){//if GMAT
							total += Float.parseFloat(Util.formatDecimal2(s  * acrit.getWeightage() / 100 ));
						}
						else{
							total += Float.parseFloat(Util.formatDecimal2(s  * acrit.getWeightage() / acrit.getMaxCritScore() ));
						}
					}
				}
				
				aucgList.add(aucg);
			}
			assmtUser.getSelectedCritGrades().clear(); 
			assmtUser.setSelectedCritGrades(aucgList);
			
			total = Float.parseFloat(Util.formatDecimal(total)); //store as Rounded value
			
			assmtUser.setTotalScore(total);
			assmtUser.setTotalGrade(convertScoreToGrade(total, 100)); //comment this out because already allow selected Grade on interface
			
		}
		
		
		assmtUser.setAssessedDate(new Date());
		assmtUser.setAssessee(user);
		assmtUser.setAssessment(assmt);
		assmtUser.setAssessor(getCurUser());
		
		
		Link returnPageLink = null;
		if(createMode){
			aDAO.saveAssessmentUser(assmtUser);
			assmt.addAssmtUsers(assmtUser);
			aDAO.updateAssessment(assmt);
			
			returnPageLink = linkSource.createPageRenderLinkWithContext(Home.class, project.getId());
		}
		else{
			aDAO.updateAssessment(assmt);
			returnPageLink = linkSource.createPageRenderLinkWithContext(ViewGrade.class, assmt.getId(), user.getId());
		}
		
		
		//audit change check
		for(int j=1; j<=assmtUser.getSelectedCritGrades().size(); j++){
			if(prevAUCGs.size()==0){
				// not log this for first Grading
			}
			else{
				AssmtUserCriteriaGrade oldAUCG = prevAUCGs.get(j-1);
				AssmtUserCriteriaGrade newAUCG = assmtUser.getSelectedCritGrades().get(j-1);
				if(oldAUCG == null && newAUCG != null){
					prevAuditValue += "Crit"+j+" Score: -"  + System.lineSeparator();
					newAuditValue += "Crit"+j+" Score: " + newAUCG.getScore() + System.lineSeparator();
					
					prevAuditValue += "Crit"+j+" Cmmt: -"  + System.lineSeparator();
					newAuditValue += "Crit"+j+" Cmmt: " + newAUCG.getComment() + System.lineSeparator();
				}
				else if(oldAUCG != null && newAUCG == null){
					prevAuditValue += "Crit"+j+" Score: "+ oldAUCG.getScore()  + System.lineSeparator();
					newAuditValue += "Crit"+j+" Score: -"  + System.lineSeparator();
					
					prevAuditValue += "Crit"+j+" Cmmt: " + oldAUCG.getScore() + System.lineSeparator();
					newAuditValue += "Crit"+j+" Cmmt: -"  + System.lineSeparator();
				}
				else if (oldAUCG != null && newAUCG != null){
					if(oldAUCG.getScore() != (newAUCG.getScore())){
						prevAuditValue += "Crit"+j+" Score: " + oldAUCG.getScore() + System.lineSeparator();
						newAuditValue += "Crit"+j+" Score: " + newAUCG.getScore() + System.lineSeparator();
					}
					String oldComment = Util.nvl(oldAUCG.getComment());
					String newComment = Util.nvl(newAUCG.getComment());
					if(!oldComment.equals(newComment)){
						prevAuditValue += "Crit"+j+" Cmmt: " + oldComment + System.lineSeparator();
						newAuditValue += "Crit"+j+" Cmmt: " + newComment + System.lineSeparator();
					}
				}
					
				
			}
		}
		Float newTotalScore = assmtUser.getTotalScore();
		if(!newTotalScore.equals(prevTotalScore)){
			prevAuditValue += "TotalScore: " + (prevTotalScore==null ? "" : prevTotalScore)
				+ System.lineSeparator();
			newAuditValue += "TotalScore: " + newTotalScore + System.lineSeparator();
		}
		String newComment = assmtUser.getComments();
		if(!Util.nvl(newComment).equals(Util.nvl(prevComment))){
			prevAuditValue += "Comment: " + Util.nvl(prevComment)
				+ System.lineSeparator();
			newAuditValue += "Comment: " + Util.nvl(newComment) + System.lineSeparator();
		}
		
		
		
		
		//save grades for Other students in the same group
		if(assmt.getGroup() != null && groupGrading){
			
			List<User> uList = getOtherStudentsInSameGroup(assmt.getGroup(), user);
			for(int i=0; i<uList.size(); i++){
				Float prTotalScore = null;
				String prComment = null;
				List<AssmtUserCriteriaGrade> prAUCGs = new ArrayList<AssmtUserCriteriaGrade>();
				
				User u = uList.get(i);
				AssessmentUser au = assmt.getAssmtUser(u);
				if(au==null){
					au = assmtUser.clone();
					au.setAssessee(u);
					au.getSelectedCriterions().clear();
					for(AssessCriterion ac : assmtUser.getSelectedCriterions()){  
						au.getSelectedCriterions().add(ac);
					}
					for(String hl : assmtUser.getSelectedGMATHL()){  
						au.getSelectedGMATHL().add(hl);
					}
					for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){  
						au.getSelectedCritGrades().add(aucg);
					}
					aDAO.saveAssessmentUser(au);
					assmt.addAssmtUsers(au);
					aDAO.updateAssessment(assmt);
				}
				else { //if au is not null
					//keep pr for audit check
					prTotalScore = au.getTotalScore();
					prComment = au.getComments();
					for(AssmtUserCriteriaGrade aucg : au.getSelectedCritGrades()){
						prAUCGs.add(aucg.clone());
					}			
					
					//assmtUser.setAssessee(u);
					au.setAssessedDate(assmtUser.getAssessedDate());
					//au.setAssessee(u);
					//au.setAssessment(assmt);
					au.setAssessor(assmtUser.getAssessor());
					au.setComments(assmtUser.getComments());
					//au.setStudentViewComments(assmtUser.isStudentViewComments());
					au.setTotalGrade(assmtUser.getTotalGrade());
					au.setTotalScore(assmtUser.getTotalScore());
					au.getSelectedCriterions().clear();
					for(AssessCriterion ac : assmtUser.getSelectedCriterions()){  
						au.getSelectedCriterions().add(ac);
					}
					au.getSelectedGMATHL().clear();
					for(String hl : assmtUser.getSelectedGMATHL()){  
						au.getSelectedGMATHL().add(hl);
					}
					au.getSelectedCritGrades().clear();
					for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){  
						au.getSelectedCritGrades().add(aucg);
					}
					
					aDAO.updateAssessment(assmt);					
				}
				
				
				
				//audit change check
				for(int j=1; j<=au.getSelectedCritGrades().size(); j++){
					if(prAUCGs.size()==0){
						// not log this for first Grading
					}
					else{
						AssmtUserCriteriaGrade oldAUCG = prAUCGs.get(j-1);
						AssmtUserCriteriaGrade newAUCG = au.getSelectedCritGrades().get(j-1);
						if(oldAUCG == null && newAUCG != null){
							prevAuditValue += "Crit"+j+" Score: -"  + System.lineSeparator();
							newAuditValue += "Crit"+j+" Score: " + newAUCG.getScore() + System.lineSeparator();
							
							prevAuditValue += "Crit"+j+" Cmmt: -"  + System.lineSeparator();
							newAuditValue += "Crit"+j+" Cmmt: " + newAUCG.getComment() + System.lineSeparator();
						}
						else if(oldAUCG != null && newAUCG == null){
							prevAuditValue += "Crit"+j+" Score: "+ oldAUCG.getScore()  + System.lineSeparator();
							newAuditValue += "Crit"+j+" Score: -"  + System.lineSeparator();
							
							prevAuditValue += "Crit"+j+" Cmmt: " + oldAUCG.getScore() + System.lineSeparator();
							newAuditValue += "Crit"+j+" Cmmt: -"  + System.lineSeparator();
						}
						else if (oldAUCG != null && newAUCG != null){
							if(oldAUCG.getScore() != (newAUCG.getScore())){
								prevAuditValue += "Crit"+j+" Score: " + oldAUCG.getScore() + System.lineSeparator();
								newAuditValue += "Crit"+j+" Score: " + newAUCG.getScore() + System.lineSeparator();
							}
							
						}
					}
				}
				Float neTotalScore = au.getTotalScore();
				if(!neTotalScore.equals(prTotalScore)){
					prevAuditValue += u.getDisplayName() + " TotalScore: " + (prTotalScore==null ? "" : prTotalScore)
						+ System.lineSeparator();
					newAuditValue += u.getDisplayName() + " TotalScore: " + neTotalScore + System.lineSeparator();
				}
				String neComment = au.getComments();
				if(!Util.nvl(neComment).equals(Util.nvl(prComment))){
					prevAuditValue += u.getDisplayName() + " Comment: " + Util.nvl(prComment)
						+ System.lineSeparator();
					newAuditValue += u.getDisplayName() + " Comment: " + Util.nvl(neComment) + System.lineSeparator();
				}
				
				
				
			}
		}
		
		
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Grade Student: "+ user.getDisplayName() , getCurUser());
		if (!prevAuditValue.isEmpty() || !newAuditValue.isEmpty()){
			ad.setPrevValue(prevAuditValue);
			ad.setNewValue(newAuditValue);
			
			auditDAO.saveAuditTrail(ad);
		}
		
		
		
		return returnPageLink;
	}
	
	
	
//	public String getSelectedClass(AssessCriterion crion){
//		if(assmtUser.getSelectedCriterions().contains(crion))
//			return "selected";
//		return "";
//	}
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
	
	
	public String getScoreValues(){
	/*	Collections.sort(getFirstCriterions(), new Comparator<AssessCriterion>(){
			@Override
			public int compare(AssessCriterion ec1,
					AssessCriterion ec2) {
				return ec1.getScore() - ec2.getScore();
			}
		});
	*/	
		String str = "[";
		for (  AssessCriterion ac : getFirstCriterions()){
			str += ac.getScore() + ",";
		}
		str = Util.removeLastSeparator(str, ",");
		return str + "]";
	}
	
	/*public String getCritIds(){
		
		String str = "[";
		for (  AssessCriteria c : assmt.getCriterias()){
			str +=  "'" + c.getId() + "',";
		}
		str = Util.removeLastSeparator(str, ",");
		return str + "]";
	}*/
	
/*	public int getTotalScore(){
		int total =0 ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			total += ac.getScore();
		}
		return total;
	}
	public String getSelCrterions(){
		String str = "" ;
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			str += ac.getId()+",";
		}
		return str;
	}
*/	
	public List<AssessCriterion> getFirstCriterions(){
		return assmt.getCriterias().get(0).getCriterions();
	}
	
	@CommitAfter
	public Object onActionFromClearGrade(int aId, int uId){
		assmt = aDAO.getAssessmentById(aId);
		if(assmt==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AssessmentID", aId));
		user = uDAO.getUserById(uId);
		if(user==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", uId));
		
		project = assmt.getProject();
		assmtUser = assmt.getAssmtUser(user);
		if(assmtUser==null)
			return null;
		if(!canGradeAssessment(project))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		String prevAuditValue = "";
		int i = 1;
		for (AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
			prevAuditValue += "Criteria_" + i + ", score: " + aucg.getScore() + ", comment: " + aucg.getComment() + System.lineSeparator();
			i++;
		}
		prevAuditValue += "Total Score:" + assmtUser.getTotalScore() + System.lineSeparator();
		prevAuditValue += "Total Grade:" + assmtUser.getTotalGrade() + System.lineSeparator();
		prevAuditValue += "Comment:" + assmtUser.getComments() + System.lineSeparator();
		
		
		assmtUser.getSelectedCriterions().clear();
		assmtUser.getSelectedGMATHL().clear();
		assmtUser.getSelectedCritGrades().clear();
		assmtUser.setTotalGrade("-");
		assmtUser.setTotalScore(0);
		assmtUser.setComments("");
		assmtUser.setAssessedDate(new Date());
		
		aDAO.saveAssessmentUser(assmtUser);
		assmt.removeAssmtUsers(assmtUser);
		aDAO.updateAssessment(assmt);
		logger.info(getCurUser().getUsername() + " clear grade for u="+user.getUsername() + ", assmt="+assmt.getId());
		
		AuditTrail ad = new AuditTrail(project, getModuleName(), String.valueOf(assmt.getId())
				, "Clear Grade Student: "+ user.getDisplayName() , getCurUser());
		ad.setPrevValue(prevAuditValue);
		ad.setNewValue("");
		auditDAO.saveAuditTrail(ad);
	
		
		return linkSource.createPageRenderLinkWithContext(Home.class, project.getId());
		
	}
	
	
	public int getColIndexGMAT(){
		return colIndex*2;
	}
	public int getColIndexGMATplus1(){
		return colIndex*2 + 1;
	}
	
}
