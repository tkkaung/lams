package ntu.celt.eUreka2.pages.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.AssmtUserCriteriaGrade;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.pages.admin.project.ManageProjects;
import ntu.celt.eUreka2.pages.modules.assessment.GiveScore;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.RequestGlobals;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;



/*
 * this is a temp file for developer to run batch job, most on these are one-time job to run
 */
public class Batch {

	@Inject
	private Messages messages;
	@Inject
	private SchoolDAO schlDAO;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private UserDAO userDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private SchedulingDAO schdlDAO;
	
	@Inject
	private ResourceDAO resrcDAO;
	@Inject
	private Session session;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private EvaluationDAO evalDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	
	@Inject
	private Cookies cookies;
	@Inject
	private Logger logger;
	@Inject
	private RequestGlobals reqGlobal;
	@Inject
    private WebSessionDAO webSessionDAO;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    @SessionState
    private AppState appState ;
    
    @Property
    private String projID;
    
    @InjectPage
    private GiveScore giveScorePage;
    
    
    @Property
	private int dateYear = 2016;
    @Property
	private int dateMonth = 1;
    @Property
	private int numRecord = 200;
	@Property
	private int startFrom = 0;
	@Property
	private String assmtID;
	
    
    @Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER))
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		
		
		
		
		
	}
	
	void onActionFromTestUserExternalKey(){
		User u = userDAO.getUserByExKey("somsak");
		appState.recordInfoMsg(u.getId() + " " + u.getExternalKey());
		
	}
	
	
	
	public List getAllAssessmentsAfterDate(Date afterDate, int limitStart, int maxRow){
		Query q =  session.createQuery("SELECT a FROM Assessment AS a " +
		" WHERE a.mdate > :rDate " )
		.setParameter("rDate", afterDate)
		.setFirstResult(limitStart)
		.setMaxResults(maxRow);

		return  q.list();
	}
	public Long getCountAllAssessmentsAfterDate(Date afterDate){
		Query q =  session.createQuery("SELECT count(a) FROM Assessment AS a " +
		" WHERE a.mdate > :rDate " )
		.setParameter("rDate", afterDate);

		return  (Long) q.uniqueResult();
	}
	
	public List getAssessmentsByRubric(Rubric rubric) {
		Query q =  session.createQuery("SELECT a FROM Assessment AS a " +
				" WHERE a.rubric=:rRubric " )
				.setParameter("rRubric", rubric);
		
		return  q.list();
	}
	
	@CommitAfter
	void onActionFromDeleteProjects(){
		int maxRow = 33100;
		int count = 0;
		List<Project> projs = projDAO.getAllProjects(290, 50);
		//List<Project> projs = projDAO.searchProjects(null, projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_FYP),
		//		null, 0, 50);
		for(int i=0; i<maxRow; i++){
			int iMod = i%50;
			Project proj = projs.get(iMod);
			if(!proj.getId().equals("ADH-CELT-15-0004")){
				projDAO.deleteProjectPermanently(proj);
				count++;
			}
			if(i%50==49){
				projs = projDAO.getAllProjects(290, 50);
				//	projs = projDAO.searchProjects(null, projTypeDAO.getTypeByName(PredefinedNames.PROJTYPE_FYP),
				//		null, 0, 50);
			}
		}
		appState.recordInfoMsg("deleted "+ count + " projects");
		
	}

	@CommitAfter
	void onActionFromDeleteEvaluation(){
		int maxRow = 300;
		//projDAO.getAllProjects(160, 50);
		int count = 0;
		List<Evaluation> evals = evalDAO.getOldEvaluations("2015-12-31", 0, 50);
		for(int i=0; i<maxRow; i++){
			int iMod = i%50;
			Evaluation eval = evals.get(iMod);
			if(!eval.getProject().getId().equals("ADH-CELT-15-0004")){
				evalDAO.deleteEvaluation(eval);
				count++;
			}
			if(i%50==49){
				evals = evalDAO.getOldEvaluations("2015-12-31", 0, 50);
			}
		}
		appState.recordInfoMsg("deleted "+ count + " projects");
		
	}
	
	
	
	@CommitAfter
	void updateAssmtCriteriaToScoreAndUpdateTotal(Assessment assmt, int i){
		//testing
		//if(assmt.getId() != 44007424)
		//	return ;
		
		//if assmt not use Rubric, skip it
		if(assmt.getCriterias().size()==0)
			return ;
		
		
		int countAU = 0;
		int countAUCG = 0;
		int countAUCGScore = 0;
		
		
		for(AssessmentUser au : assmt.getAssmtUsers())
		{
			float total = 0;
			//logger.warn("au="+au.getId() );
			for(AssmtUserCriteriaGrade aucg : au.getSelectedCritGrades()){
				AssessCriteria ac = aucg.getCriteria();
				Float score = aucg.getScore();
		//		logger.warn( " ac="+ac.getId() );
			//	logger.warn("aucg="+aucg.getId()  + " score="+score);
				if(score==null){
						
					for(AssessCriterion auc : au.getSelectedCriterions()){
						if(auc.getCriteria().getId() == ac.getId()){
							score = (float) auc.getScore();
							aucg.setScore(score);
							countAUCGScore++;
							break;
						}

					}
					if(score==null)
						score = (float) 0;
					
					String result = (" assmtID: "+assmt.getId() + " assmtUserID="+au.getId() 
							 +" critID="+ ac.getId() + " score="+score); 
					
		//			logger.info(result);
					appState.recordWarningMsg(result);
					countAUCG++;
						
				}
				
				if(assmt.getGmat()){//if GMAT
					total += Float.parseFloat(Util.formatDecimal(score  * ac.getWeightage() / 100 ));
				}
				else{
					total += Float.parseFloat(Util.formatDecimal(
							score  * 
							ac.getWeightage() / 
							ac.getMaxCritScore() ));
				}
				
				total = Float.parseFloat(Util.formatDecimal(total)); //store as Rounded value
				
				au.setTotalScore(total);
				au.setTotalGrade(giveScorePage.convertScoreToGrade(total, 100)); //comment this out because already allow selected Grade on interface
					
				
			}
			countAU++;
		}
		assmtDAO.updateAssessment(assmt);
		
		String result = i+ " ProjID:"+ assmt.getProject().getId()+ " AssmtID="+ assmt.getId() 
			+ ", Total AssmtUser="+countAU + ", Total AssmtUserGrade="+countAUCG
//			+ ", Total AssmtUserScore updated=" + countAUCGScore;
			;
//		logger.info(result);
		appState.recordWarningMsg(result);
	}
	void onSuccessFromAssmtCriteriaToScoreAndUpdateTotalAllByLimitForm(){
		Calendar jan2016 = Calendar.getInstance();
		jan2016.set(dateYear, dateMonth, 1);
		
		List<Assessment> assmtList = getAllAssessmentsAfterDate(jan2016.getTime(),startFrom, numRecord);
	
		int i = 0;
		for(Assessment assmt: assmtList){
	//		logger.warn("assmt="+assmt.getId() );
		
			updateAssmtCriteriaToScoreAndUpdateTotal(assmt, i);		
			i++;
		}
		appState.recordWarningMsg("total: " + getCountAllAssessmentsAfterDate(jan2016.getTime()) + 
					", this run index: " + startFrom + "-" + (startFrom + assmtList.size()-1) ); 
		
	}
	/*@CommitAfter
	void onActionFromAssmtScoretoSelectedCritria(){
		Calendar jun2013 = Calendar.getInstance();
		jun2013.set(2013, 6, 1);
		
		List<Rubric> rList = assmtDAO.searchRubrics(null, null, null, null, null);
		for(Rubric r : rList){
			
			if(r.getCriterias().size()>0 
					&& r.getCriterias().get(0)!=null 
					&& r.getCriterias().get(0).getCriterions().size()>1
					&& r.getCriterias().get(0).getCriterions().get(0)!=null
					&& r.getCriterias().get(0).getCriterions().get(1)!=null
					){
			if(r.getCriterias().get(0).getCriterions().get(0).getScore() 
					> r.getCriterias().get(0).getCriterions().get(1).getScore()  ){
				List<Assessment> assmtList = getAssessmentsByRubric(r);
			//	logger.warn("r="+r.getId() );
				
				for(Assessment assmt: assmtList){
				//	logger.warn("assmt="+assmt.getId() );
				if(jun2013.getTime().after(assmt.getMdate() )){
					continue;
				}
					
					for(AssessmentUser au : assmt.getAssmtUsers())
					{
						au.getSelectedCriterions().clear();
						//logger.warn("au="+au.getId() );
						
						for(AssmtUserCriteriaGrade aucg : au.getSelectedCritGrades()){
							AssessCriteria ac = aucg.getCriteria();
							Float score = aucg.getScore();
							if(score!=null){
							//	logger.warn( " ac="+ac.getId() );
								
							//	logger.warn("aucg="+aucg.getId()  + " score="+score);
								
								AssessCriterion acrion = convertScoreToCriterion(score, ac);
								au.getSelectedCriterions().add(acrion);
								
								String result = (" assmtID: "+assmt.getId() + " assmtUserID="+au.getId() 
										 +" critID="+ ac.getId() 
										+ " aucgScore="+aucg.getScore() + " aucgID=" + aucg.getId())
										+ " acrionScore=" +acrion.getScore(); 
								;
								logger.info(result);
								appState.recordWarningMsg(result);
							}
							
						}
					}
					assmtDAO.updateAssessment(assmt);					
				}
			}
			}
		}
		
	}
	
	@CommitAfter
	void updateAssmtScoretoSelectedCritria(Assessment assmt){
		int countAU = 0;
		int countAUCG = 0;
		
		for(AssessmentUser au : assmt.getAssmtUsers())
		{
			au.getSelectedCriterions().clear();
			//logger.warn("au="+au.getId() );
			
			for(AssmtUserCriteriaGrade aucg : au.getSelectedCritGrades()){
				AssessCriteria ac = aucg.getCriteria();
				Float score = aucg.getScore();
				if(score!=null){
				//	logger.warn( " ac="+ac.getId() );
					
				//	logger.warn("aucg="+aucg.getId()  + " score="+score);
					
					AssessCriterion acrion = convertScoreToCriterion(score, ac);
					au.getSelectedCriterions().add(acrion);
					
					String result = (" assmtID: "+assmt.getId() + " assmtUserID="+au.getId() 
							 +" critID="+ ac.getId() 
							+ " aucgScore="+aucg.getScore() + " aucgID=" + aucg.getId())
							+ " acrionScore=" +acrion.getScore(); 
					;
					logger.info(result);
					appState.recordWarningMsg(result);
				
					countAUCG++;
				}
				
			}
			countAU++;
		}
		assmtDAO.updateAssessment(assmt);
		
		String result = "ProjID:"+ assmt.getProject().getId()+ " AssmtID="+ assmt.getId() + ", Total AssmtUser="+countAU + ", Total AssmtUserCriteria="+countAUCG;
		logger.info(result);
		appState.recordWarningMsg(result);
	}
	
	
	void onSuccessFromAssmtToCriteriaAllByLimitForm(){
		Calendar jun2013 = Calendar.getInstance();
		jun2013.set(2013, 6, 1);
		
		List<Assessment> assmtList = getAllAssessmentsAfterDate(jun2013.getTime(),startFrom, numRecord);
	
		
		for(Assessment assmt: assmtList){
		//	logger.warn("assmt="+assmt.getId() );
		
			updateAssmtScoretoSelectedCritria(assmt);
							
		}
		appState.recordWarningMsg("total: " + getCountAllAssessmentsAfterDate(jun2013.getTime()) + 
					", this run index: " + startFrom + "-" + (startFrom + assmtList.size()-1) ); 
		
	}
	
	void onSuccessFromAssmtToCriteriaForm(){
		Project proj = projDAO.getProjectById(projID);
		if(proj == null){
			appState.recordErrorMsg("Invalid Project ID: " + projID );
			return;
		}
		List<Assessment> assmtList = assmtDAO.getAssessmentsByProject(proj);
		
		for(Assessment assmt: assmtList){
			updateAssmtScoretoSelectedCritria(assmt);
							
		}
	}
	
	void onSuccessFromAssmtToCriteriaByAssmtForm(){
		Assessment assmt = assmtDAO.getAssessmentById(Integer.parseInt(assmtID));
		if(assmt == null){
			appState.recordErrorMsg("Invalid Assmt ID: " + assmtID );
			return;
		}
		updateAssmtScoretoSelectedCritria(assmt);
							
		
	}
	
	
	
	private AssessCriterion convertScoreToCriterion(float score, AssessCriteria ac){
		
		int prev = 0;
		
		if(ac.getCriterions().size() > 1){
			if(ac.getCriterions().get(0).getScore() <= ac.getCriterions().get(1).getScore()){ //ascending
				if((score <= ac.getCriterions().get(prev).getScore()))
					return ac.getCriterions().get(prev);
				for(int i=1; i < ac.getCriterions().size(); i++ ){
					int cur = i;
					if(score <= ac.getCriterions().get(cur).getScore()){
						if(score < ac.getCriterions().get(cur).getScore() ){
							return ac.getCriterions().get(prev);
						}
						else{
							return ac.getCriterions().get(cur);
						}
					}
					prev = cur;
				}
				
				
				
			}
			else{ //descending
				
				
				if(score >= ac.getCriterions().get(0).getScore() )
					return ac.getCriterions().get(0);
				for(int i=1;  i < ac.getCriterions().size(); i++ ){
					int cur = i;
					if(score >= ac.getCriterions().get(cur).getScore()){
						if(score >= ac.getCriterions().get(prev).getScore() ){
							return ac.getCriterions().get(prev);
						}
						else{
							return ac.getCriterions().get(cur);
						}
					}
					prev = cur;
				}
				return ac.getCriterions().get(ac.getCriterions().size()-1);
			}
			
		}
		else{
			if(ac.getCriterions().size() == 1)
				return ac.getCriterions().get(0);
			else
				return null;
			
		}
		return null;
		
	}
	
	@CommitAfter
	void updateAssmtDashToGrade(Assessment assmt){
		int countAU = 0;
		for(AssessmentUser au : assmt.getAssmtUsers())
		{
			if("-".equals(au.getTotalGrade())){
				
				au.setTotalGrade(giveScorePage.convertScoreToGrade(au.getTotalScore(), assmt.getPossibleScore()));
				countAU++;
				
			}
				
			
		}
		assmtDAO.updateAssessment(assmt);
		
		String result = "ProjID:"+ assmt.getProject().getId()+ " AssmtID="+ assmt.getId() + ", Total '-' AssmtUser="+countAU ;
		logger.info(result);
		appState.recordWarningMsg(result);
	}
	
	void onSuccessFromAssmtDashToGradeAllForm(){
		Calendar jun2013 = Calendar.getInstance();
		jun2013.set(2013, 6, 1);
		
		List<Assessment> assmtList = getAllAssessmentsAfterDate(jun2013.getTime(),startFrom, numRecord);
	
		
		for(Assessment assmt: assmtList){
		//	logger.warn("assmt="+assmt.getId() );
		
			updateAssmtDashToGrade(assmt);
							
		}
		appState.recordWarningMsg("total: " + getCountAllAssessmentsAfterDate(jun2013.getTime()) + 
					", this run index: " + startFrom + "-" + (startFrom + assmtList.size()-1) ); 
		
	}
	
	void onSuccessFromAssmtDashToGradeProjForm(){
		Project proj = projDAO.getProjectById(projID);
		if(proj == null){
			appState.recordErrorMsg("Invalid Project ID: " + projID );
			return;
		}
		List<Assessment> assmtList = assmtDAO.getAssessmentsByProject(proj);
		
		for(Assessment assmt: assmtList){
			updateAssmtDashToGrade(assmt);
							
		}
	}
	
	void onSuccessFromAssmtDashToGradeAssmtForm(){
		Assessment assmt = assmtDAO.getAssessmentById(Integer.parseInt(assmtID));
		if(assmt == null){
			appState.recordErrorMsg("Invalid Assmt ID: " + assmtID );
			return;
		}
		updateAssmtDashToGrade(assmt);
							
		
	}
	*/
	
	/*
	@CommitAfter
	void onActionFromCreateSchoolAdmins(){
		String usernameSuffix = messages.get("usernameSuffix");
		String defaultEmail = messages.get("defaultEmail");
		String firstnameSuffix = " "+messages.get("firstnameSuffix");
		String lastnameSuffix = messages.get("lastnameSuffix");
		String defaultPassword = messages.get("defaultPassword");
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		int count = 0;
		
		List<School> schlList = schlDAO.getAllSchools();
		String ip = reqGlobal.getHTTPServletRequest().getRemoteAddr();
		for(School s : schlList){
			String newUsername = s.getName().replace(" ", "_") + usernameSuffix;
			if(!userDAO.isUsernameExist(newUsername)){
				User u = new User(); 
				u.setCreateDate(new Date());
				u.setEmail(defaultEmail);
				u.setEnabled(true);
				u.setFirstName(s.getName() + firstnameSuffix);
				u.setLastName(lastnameSuffix);
				//u.setId(id)
				u.setIp(ip);
				u.setModifyDate(new Date());
				//u.setJobTitle()
				//u.setMphone(mphone)
				//u.setOrganization(organization)
				u.setPassword(Util.generateHashValue(defaultPassword));
				//u.setPhone(phone)
				//u.setRemarks();
				u.setSchool(s);
				u.setSysRole(sysRole);
				u.setUsername(newUsername);
				
				userDAO.save(u);
				count++;
			}
		}
		appState.recordInfoMsg(count+" user(s) created.");
		
	}
	*/
/*	
	@CommitAfter
	void onActionFromChangeSchoolName(){
		List<School> schlList = schlDAO.searchSchools("New_Dept");
		int i = 1;
		for(School s : schlList){
			if(s.getName().endsWith("New_Dept")){
				s.setName("Dept_"+i);
				i++;
				schlDAO.save(s);
			}
		}
		appState.recordInfoMsg(--i +" records changed.");
	}
	*/
/*	
	@CommitAfter
	void onActionFromChangeMilestoneCreator(){
		List<Milestone> mList = schdlDAO.getAllMilestone();
		int i = 1;
		ProjRole projRole = projRoleDAO.getRoleByName(PredefinedNames.PROJROLE_SCHOOL_TUTOR);
		
		for(Milestone m : mList){
			if(m.getManager()==null){
				i++;
				
				List<User> uList = m.getSchedule().getProject().getMembersByRole(projRole);
				m.setManager(uList.isEmpty()? null: uList.get(0));
				schdlDAO.saveMilestone(m);
			}
		}
		appState.recordInfoMsg(--i +" records changed.");
	}
	*/
/*	@CommitAfter
	void onActionFromImportDuplicateSchoolNameMap(){
		List<SchoolNameMap> snmList = new ArrayList<SchoolNameMap>();
		snmList.add(new SchoolNameMap("School of Physical and Mathematical Sciences"
				, "School of Physical & Mathematical Sciences"));
		snmList.add(new SchoolNameMap("School of Arts, Design & Media"
				, "School of Art, Design and Media"));
		snmList.add(new SchoolNameMap("Centre for Educational Development"
				, "Centre for Excellence in Learning & Teaching"));
		snmList.add(new SchoolNameMap("Centre for Excellence in Learning and Teaching"
				, "Centre for Excellence in Learning & Teaching"));
		snmList.add(new SchoolNameMap(""
				, "Others"));
		snmList.add(new SchoolNameMap("Nanyang Business School"
				, "College of Business (Nanyang Business School)"));
		snmList.add(new SchoolNameMap("School of Communication and Information"
				, "Wee Kim Wee School of Communication and Information"));
		
		
		int i = 1;
		
		for(SchoolNameMap s : snmList){
			schlDAO.saveOrUpdateNameMap(s);
			
			i++;
				
		}
		appState.recordInfoMsg(--i +" records changed.");
	}
	*/
	
	/**
	 * re-assign School in Project and in User, then delete to first School
	 */
/*	@CommitAfter
	void onActionFromMergeDuplicateSchoolName(){ 
		List<SchoolNameMap> nameList = schlDAO.getAllSchoolMappedName();
		
		int i = 1;
		
		for(SchoolNameMap snm : nameList){
			School sch1 = schlDAO.getFirstSchoolByDescriptionWithoutDuplicateCheck(snm.getNameFrom());
			School sch2 = schlDAO.getFirstSchoolByDescriptionWithoutDuplicateCheck(snm.getNameTo());
			if(sch1!=null && sch2!=null && !(sch1.getId()==sch2.getId())){
				List<Project> projList = projDAO.getProjectsBySchool(sch1, null, null);
				for(Project p : projList){
					p.setSchool(sch2);
					projDAO.saveProject(p);
					i++;
				}
				projList = null; //clean up
				
				List<User> userList = userDAO.searchUsers(null, null, null, null, sch1, null, null);
				for(User u : userList){
					u.setSchool(sch2);
					userDAO.save(u);
					i++;
				}
				userList = null; //clean up
				
				schlDAO.delete(sch1);
			}
				
		}
		appState.recordInfoMsg(--i +" records changed.");
	}
	*/
/*	@CommitAfter
	void onActionFromResourceToResource2(){
		List<Resource> rList = resrcDAO.getAllResources();
		int i = 1;
		for(Resource r : rList){
			i++;
			switch(r.getType()){
			case Folder:
				ResourceFolder rfd = new ResourceFolder();
				rfd.setCdate(r.getCdate());
				rfd.setDes(r.getDes());
				rfd.setEditor(r.getEditor());
				rfd.setMdate(r.getMdate());
				rfd.setName(r.getName());
				rfd.setOwner(r.getOwner());
				rfd.setId(r.getId());
				if(r.getParent()!=null){
					ResourceFolder rfP = resrc2DAO.getFolderById(r.getParent().getId());
					rfd.setParent(rfP);
				}
				rfd.setProj(r.getProj());
				rfd.setType(ntu.celt.eUreka2.modules.resources2.ResourceType.Folder);
				
				resrc2DAO.addResource(rfd);
				
				break;
			case File:
				
					ResourceFile rf = new ResourceFile();
					rf.setCdate(r.getCdate());
					rf.setDes(r.getDes());
					rf.setEditor(r.getEditor());
					rf.setMdate(r.getMdate());
					rf.setName(r.getName());
					rf.setOwner(r.getOwner());
					rf.setId(r.getId());
					if(r.getParent()!=null){
						ResourceFolder rfP = resrc2DAO.getFolderById(r.getParent().getId());
						rf.setParent(rfP);
					}
					rf.setProj(r.getProj());
					rf.setType(ntu.celt.eUreka2.modules.resources2.ResourceType.File);
					
					//rf.setLockedBy(lockedBy) //no need to set
					
					ResourceFileVersion rfv = new ResourceFileVersion();
					rfv.setCdate(r.getCdate());
					rfv.setCmmt(r.getDes());
					rfv.setContentType(r.getContent());
					//rfv.setId(id)
					rfv.setName(r.getName());
					rfv.setNumDownload(r.getNumDownload());
					rfv.setOwner(r.getOwner());
					rfv.setPath(r.getPath());
					//rfv.setRfile(file)
					rfv.setSize(r.getSize());
					rfv.setVersion(1);
					
					rf.addFileVersion(rfv);
					
					resrc2DAO.addResource(rf);
*/				/*if(r.getVersion()==1){
				}
				else{
					Resource f = resrcDAO.getFile(r.getName(), r.getParent(), r.getVersion());
					
					ResourceFile rf = resrc2DAO.getFileById(f.getId());
					if(rf==null){
						
						logger.warn(f.getVersion()+"....."+f.getId()+"....."+f.getName()+"....."+f.getPath());
						continue; //
					}
					ResourceFileVersion rfv = new ResourceFileVersion();
					rfv.setCdate(r.getCdate());
					rfv.setCmmt(r.getDes());
					rfv.setContentType(r.getContent());
					//rfv.setId(id)
					rfv.setName(r.getName());
					rfv.setNumDownload(r.getNumDownload());
					rfv.setOwner(r.getOwner());
					rfv.setPath(r.getPath());
					//rfv.setRfile(file)
					rfv.setSize(r.getSize());
					rfv.setVersion(r.getVersion());
					
					rf.addFileVersion(rfv);
					resrc2DAO.updateResource(rf);
					
				}
				*/
/*				break;
			case Link:
				
				ResourceLink rl = new ResourceLink();
				rl.setCdate(r.getCdate());
				rl.setDes(r.getDes());
				rl.setEditor(r.getEditor());
				rl.setMdate(r.getMdate());
				rl.setName(r.getName());
				rl.setOwner(r.getOwner());
				rl.setId(r.getId());
				if(r.getParent()!=null){
					ResourceFolder rfP = resrc2DAO.getFolderById(r.getParent().getId());
					rl.setParent(rfP);
				}
				rl.setProj(r.getProj());
				rl.setType(ntu.celt.eUreka2.modules.resources2.ResourceType.Link);
				
				rl.setUrl(r.getPath());
				
				resrc2DAO.addResource(rl);
				break;
				
			
			
				
				
				
			}
			
			
		}
		appState.recordInfoMsg(--i +" records changed.");
		
		
	}
*/	
/*	
	@Inject
	private UsageDAO usageDao;
	@Inject
	private AttachedFileManager attachedFileManager;
	void onActionFromDeleteUnusedProjects(){ 
		List<Project> projList = projDAO.getAllProjects();
		int i = 0;
		int j = 0;
		int k = 0;
		Calendar lastYear = Calendar.getInstance();
		lastYear.add(Calendar.YEAR, -1);
		Calendar lastMonth = Calendar.getInstance();
		lastMonth.add(Calendar.MONTH, -1);
		
		for(Project p : projList){
			if( (p.getLastAccess()==null && lastMonth.getTime().after(p.getCdate())) 
				|| lastYear.getTime().after(p.getCdate())
			){
				if(!usageDao.hasItemInProj(p)){
					deleteProject(p);
					
					logger.info("k="+k+" j="+j+" i="+i + ", Delete proj="+p.getId());
					i++;
				}
				j++;
			}
			k++;
		}
		appState.recordInfoMsg("j="+j+" i="+i +" records changed.");
	}
	@CommitAfter
	private void deleteProject(Project proj){
		projDAO.deleteProject(proj);
	}
	@CommitAfter
	private void permanentDeleteProject(Project proj){
		projDAO.deleteProjectPermanently(proj);
	}
	
	void onActionFromPermanentDeleteProjects(){ 
		List<Project> projList = projDAO.getAllProjects();
		int i = 0;
		int j = 0;
		Calendar lastMonth = Calendar.getInstance();
		lastMonth.add(Calendar.MONTH, -1);
		String repoRootFolder = attachedFileManager.getRootFolder();
		
		for(Project p : projList){
			if(p.getStatus().getName().equals(PredefinedNames.PROJSTATUS_DELETED) 
					&& lastMonth.getTime().after(p.getLastStatusChange()) ){ 
				permanentDeleteProject(p);
				
				String folderLocation = repoRootFolder+"/"+p.getId()+"/";
				File folder = new File(folderLocation);
				if(folder.exists()){
					folder.renameTo(new File(repoRootFolder+ "/deleted_"+folder.getName()));
				}
				
				logger.info("j="+j+" i="+i + ", Deleted proj="+p.getId());
				i++;
			}
			j++;
			
		}
		appState.recordInfoMsg("j="+j+" i="+i +" records changed.");
	}
*/	
/*	
	
	void onActionFromMoveResourceFolders(){ 
		int i = 0;
		int j = 0;
		int k = 0;
		List<Project> projList = projDAO.getAllProjects(i,9999);
		
		for(Project p : projList){
			List<ResourceFolder> sharedFolders = resrcDAO.getSharedRootFolders(p);
			logger.debug("i="+i+" j="+j+" k="+k + ", proj="+p.getId());
			if(sharedFolders.isEmpty()){
				//do nothing
			}
			else{
				ResourceFolder topFolder = null;
				for(ResourceFolder fd : sharedFolders){
					if(fd.getName().equals("Shared Folder")){ //find existing topFolder
						topFolder = fd;
						sharedFolders.remove(fd);
						break;
					}
				}
				if(topFolder==null){
					topFolder = createDefaultSharedFolder(p);
					j++;
				}
				for(ResourceFolder fd : sharedFolders){ //process remaining shared folder
					fd.setParent(topFolder);
					updateResource(fd);
					k++;
					logger.info("i="+i+" j="+j+" k="+k + ", folder="+fd.getName());
				}
			}
			i++;
		}
		
		appState.recordInfoMsg("j="+j+" k="+k +" records changed.");
		
	}
	@CommitAfter
	private void updateResource(ResourceFolder fd){
		resrcDAO.updateResource(fd);
	}
	@CommitAfter
	private ResourceFolder createDefaultSharedFolder(Project p){

		ResourceFolder fd = new ResourceFolder();
		fd.setDes("File in this folder are shared among project members");
		fd.setCdate(new Date());
		fd.setMdate(new Date());
		fd.setName("Shared Folder");
		if(p.getCreator()!=null){
			fd.setOwner(p.getCreator());
		}
		else{
			for(ProjUser pu : p.getMembers()){
				if(pu.hasPrivilege(PrivilegeProject.IS_LEADER)){
					fd.setOwner(pu.getUser());
					break;
				}
			}
		}
		fd.setProj(p);
		fd.setShared(true);
		resrcDAO.addResource(fd);
		
		return fd;
	}
	*/
/*	void onActionFromDeleteUnusedProjects(){ 
		List<Project> projList = projDAO.getAllProjects();
		
		int i = 0;
		int j = 0;
		int k = 0;
		Calendar lastYear = Calendar.getInstance();
		lastYear.add(Calendar.YEAR, -1);
		
		
		for(Project p : projList){
			if( p.getLastAccess()==null ){ //p.getEdate()==null ||lastYear.getTime().after(p.getCdate())
				
				long getNumAnnouncement = getNumAnnouncement(p);
				long getNumTransaction = getNumTransaction(p);
				long getNumBlog = getNumBlog(p);
				long getNumElog = getNumElog(p);
				long getNumFile = getNumFile(p);
				long getNumLink = getNumLink(p);
				long getNumForum = getNumForum(p);
				long getNumTask = getNumTask(p);
				long getNumPhase = getNumPhase(p);
				long getNumLlog = getNumLlog(p);
				
				long totalItem = 0
					+ getNumAnnouncement
					+ getNumTransaction
					+ getNumBlog
					+ getNumElog
					+ getNumFile
					+ getNumLink
					+ getNumForum
					+ getNumTask
					+ getNumPhase
					+ getNumLlog
					;
				
				if(totalItem==0){
					deleteProject(p);
					
					logger.info("k="+k+" j="+j+" i="+i 
							+ ", Delete proj="+p.getId());
					i++;
				}
				else{
					logger.info("k="+k+" j="+j+" i="+i  
							+" ," + getNumAnnouncement
							+" ," + getNumTransaction
							+" ," + getNumBlog
							+" ," + getNumElog
							+" ," + getNumFile
							+" ," + getNumLink
							+" ," + getNumForum
							+" ," + getNumTask
							+" ," + getNumPhase
							+" ," + getNumLlog
							+" =="+p.getId());
				}
				
				j++;
			}
			k++;
			
				
		}
		appState.recordInfoMsg("j="+j+" i="+i +" records changed.");
	}
	@CommitAfter
	private void deleteProject(Project proj){
		projDAO.deleteProject(proj);
	}
	
	
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private SchlTypeAnnouncementDAO schlTypeAnnmtDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ResourceDAO resourceDAO;
	@Inject
	private SchedulingDAO scheduleDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private LearningLogDAO llogDAO;
	@Inject
	private BudgetDAO budgetDAO;
	public int getNumView(Project proj){
		return proj.getNumVisit();
	}
	public int getNumMember(Project proj){
		return proj.getMembers().size();
	}
	public long getNumAdminAnnouncement(Project proj){
		return schlTypeAnnmtDAO.countSchlTypeAnnouncements(proj);
	}
	public long getNumAnnouncement(Project proj){
		return annmtDAO.countAnnouncements(proj);
	}
	public long getNumTransaction(Project proj){
		return budgetDAO.countTransactions(proj);
	}
	public long getNumBlog(Project proj){
		return blogDAO.countBlogs(proj);
	}
	public long getNumBlogComment(Project proj){
		return blogDAO.countBlogComments(proj);
	}
	public long getNumElog(Project proj){
		return elogDAO.countElogs(proj);
	}
	public long getNumElogComment(Project proj){
		return elogDAO.countElogComments(proj);
	}
	public long getNumFolder(Project proj){
		return resourceDAO.countFolders(proj);
	}
	public long getNumFile(Project proj){
		return resourceDAO.countFiles(proj);
	}
	public long getNumLink(Project proj){
		return resourceDAO.countLinks(proj);
	}
	public int getNumDownload(Project proj){
		return resourceDAO.countNumDownloads(proj);
	}
	public long getNumForum(Project proj){
		return forumDAO.getTotalForums(proj);
	}
	public long getNumThread(Project proj){
		return forumDAO.getTotalThreads(proj);
	}
	public long getNumThreadReply(Project proj){
		return forumDAO.getTotalThreadReplies(proj);
	}
	public long getNumThreadReflection(Project proj){
		return forumDAO.getTotalThreadReflection(proj, getCurUser());
	}
	public long getNumMilestone(Project proj){
		return scheduleDAO.countMilestones(proj);
	}
	public long getNumPhase(Project proj){
		return scheduleDAO.countPhases(proj);
	}
	public long getNumTask(Project proj){
		return scheduleDAO.countTasks(proj);
	}
	public long getNumLlog(Project proj){
		return llogDAO.countLlogs(proj);
	}
	*/
/*	
	@CommitAfter
	void onActionFromSetScheduleItemIdentifiers(){
		int i = 0;
		int j = 0;
		Set<Project> projList = new HashSet<Project>();
		
		List<Schedule> inactiveSchdList = schdlDAO.getInactiveSchedules();
		for(Schedule s : inactiveSchdList){
			projList.add(s.getProject());
		}
		
		for(Project p : projList){
			logger.info("j="+j+" i="+i + " proj="+p.getId());
			List<Schedule> schdList = schdlDAO.getInactiveSchedules(p);
			if(!schdList.isEmpty()){
				Schedule activeSchd = schdlDAO.getActiveSchedule(p);
				
				for(Schedule s : schdList){
					for(Milestone m : s.getMilestones()){
						Milestone activeM = (Milestone) findItem(m, activeSchd);
						if(activeM!=null){
							logger.debug(j+"..m1..."+m.getId()+"..."+m.getIdentifier()+"..."+m.getSchedule().getId()+"..."+m.getSchedule().isActive());
							
							m.setIdentifier(activeM.getIdentifier());
							schdlDAO.saveMilestone(m);
							
							j++;
						}
						
						for(Task t: m.getTasks()){
							Task activeT = (Task) findItem(t, activeSchd);
							if(activeT!=null){
								logger.debug(j+"..t..."+activeT.getMilestone().getId()+"..."+activeT.getMilestone().getIdentifier()+"..."+activeT.getMilestone().getSchedule().getId()+"..."+activeT.getMilestone().getSchedule().isActive());
								
								t.setIdentifier(activeT.getIdentifier());
								schdlDAO.saveTask(t);
								j++;
							}
						}
						
						for(Phase ph: m.getPhases()){
							Phase activePh = (Phase) findItem(ph, activeSchd);
							if(activePh!=null){
								logger.debug(j+"..ph..."+activePh.getMilestone().getId()+"..."+activePh.getMilestone().getIdentifier()+"..."+activePh.getMilestone().getSchedule().getId()+"..."+activePh.getMilestone().getSchedule().isActive());
								
								ph.setIdentifier(activePh.getIdentifier());
								schdlDAO.savePhase(ph);
								j++;
							}
						}
					}
				}
			}
			i++;
		}
		appState.recordInfoMsg(j +" records changed.");
	}
	private Object findItem(Object obj, Schedule schd){
		//check by identifier
		//check by createDate since it's never been changed
		
		if(obj instanceof Task){
			Task tObj = (Task) obj;
			for(Milestone m : schd.getMilestones()){
				for(Task t: m.getTasks()){
					if(tObj.getName().equals(t.getName())){
						return t;
					}
				}
			}
		}
		else if(obj instanceof Phase){
			Phase pObj = (Phase) obj;
			for(Milestone m : schd.getMilestones()){
				for(Phase p : m.getPhases()){
					if(pObj.getName().equals(p.getName())){
						return p;
					}
				}
			}		
		}
		else if(obj instanceof Milestone){
			Milestone mObj = (Milestone) obj;
			for(Milestone m : schd.getMilestones()){
				if(mObj.getName().equals(m.getName())){
					return m;
				}
			}
		}
		
		return null;
	}
	*/
}
