package ntu.celt.eUreka2.pages.modules.assessment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessCriteria;
import ntu.celt.eUreka2.modules.assessment.AssessCriterion;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentUser;
import ntu.celt.eUreka2.modules.assessment.AssmtFile;
import ntu.celt.eUreka2.modules.assessment.AssmtInstructorFile;
import ntu.celt.eUreka2.modules.assessment.AssmtUserCriteriaGrade;
import ntu.celt.eUreka2.modules.assessment.PrivilegeAssessment;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.pages.modules.resources.Home;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

@Import(stylesheet="context:lib/css/gradeColor.css")
public abstract class AbstractPageAssessment {
	public String getModuleName(){
		return PredefinedNames.MODULE_ASSESSMENT;
	}
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private Messages messages;
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Logger logger;
	
	@InjectPage
	private Home pageResourceHome;
		
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	
	
	public static final int NUM_SHADE = 1;
	
	
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_ASSESSMENT);
	}
	public int addInt(int a, int b){
		return a+b;
	}
	public int multipleInt(int a, int b){
		return a*b;
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	public String truncateString(String str, int numChar){
		return Util.truncateString(str, numChar);
	}
	public String encode(String str){
		return Util.encode(str);
	}
	public String textarea2html(String str){
		return Util.textarea2html(str); 
	}
	public boolean isFirst(int index){
		if(index == 0)
			return true;
		return false;
	}
	public boolean isLast(int index, List<Object> list){
		if(index == (list.size()-1))
			return true;
		return false;
	}
	public String isLastClass(int index, List<Object> list){
		if(index == (list.size()-1))
			return "last";
		return "";
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
	}
	public boolean isASC(String ASCorDESC){
		if("ASC".equalsIgnoreCase(ASCorDESC) )
			return true;
		return false;
	}
	
	
	public SelectModel getRubricModel(Project proj){
		List<OptionGroupModel> optGroupModelList = new ArrayList<OptionGroupModel>();
		List<Rubric> mRList = aDAO.getMasterRubricsAllSchool();
		List<Rubric> rList = aDAO.getRubricsByOwner(getCurUser());
		rList.removeAll(mRList); //not include 'master rubrics' in 'my-rubrics'
		
		
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		if(getCurUser().getSchool()!=null && !accessibleSchlIDs.contains(getCurUser().getSchool().getId())){
			accessibleSchlIDs.add(getCurUser().getSchool().getId());
		}
		if(proj.getSchool()!=null && !accessibleSchlIDs.contains(proj.getSchool().getId())){
			accessibleSchlIDs.add(proj.getSchool().getId());
		}
		
		for(int schlID : accessibleSchlIDs){
			School schl = schoolDAO.getSchoolById(schlID);
			if(schl==null)
				continue;
			List<Rubric> schlRList = aDAO.getMasterRubrics(schl);
			
			rList.removeAll(schlRList);//not include 'School rubrics' in 'my-rubrics'
			mRList.removeAll(schlRList);//not include 'School rubrics' in 'master-rubrics'
			
			List<OptionModel> optModelList = new ArrayList<OptionModel>();
			for (Rubric r : schlRList) {
				OptionModel optModel = new OptionModelImpl(r.getName(), r);
				optModelList.add(optModel);
			}
			if(!optModelList.isEmpty()){
				OptionGroupModel optGroupModel1 = new OptionGroupModelImpl(messages.format("x-rubrics", schl.getDisplayName()), false, optModelList);
				optGroupModelList.add(optGroupModel1);
			}
		}
		
		
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (Rubric r : rList) {
			OptionModel optModel = new OptionModelImpl(r.getName(), r);
			optModelList.add(optModel);
		}
		if(!optModelList.isEmpty()){
			OptionGroupModel optGroupModel1 = new OptionGroupModelImpl(messages.get("my-rubrics"), false, optModelList);
			optGroupModelList.add(optGroupModel1);
		}
		
		optModelList = new ArrayList<OptionModel>();
		for (Rubric r : mRList) {
			OptionModel optModel = new OptionModelImpl(r.getName(), r);
			optModelList.add(optModel);
		}
		if(!optModelList.isEmpty()){
			OptionGroupModel optGroupModel2 = new OptionGroupModelImpl(messages.get("master-rubrics"), false, optModelList);
			optGroupModelList.add(optGroupModel2);
		}
		
		SelectModel selModel = new SelectModelImpl(optGroupModelList, null);
		return selModel;
	}
	public SelectModel getRubricModelSchool(Project proj){
		List<OptionGroupModel> optGroupModelList = new ArrayList<OptionGroupModel>();
		
		
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		if(getCurUser().getSchool()!=null && !accessibleSchlIDs.contains(getCurUser().getSchool().getId())){
			accessibleSchlIDs.add(getCurUser().getSchool().getId());
		}
		if(proj.getSchool()!=null && !accessibleSchlIDs.contains(proj.getSchool().getId())){
			accessibleSchlIDs.add(proj.getSchool().getId());
		}
		
		for(int schlID : accessibleSchlIDs){
			School schl = schoolDAO.getSchoolById(schlID);
			if(schl==null)
				continue;
			List<Rubric> schlRList = aDAO.getMasterRubrics(schl);
			
			
			List<OptionModel> optModelList = new ArrayList<OptionModel>();
			for (Rubric r : schlRList) {
				OptionModel optModel = new OptionModelImpl(r.getName(), r);
				optModelList.add(optModel);
			}
			if(!optModelList.isEmpty()){
				OptionGroupModel optGroupModel1 = new OptionGroupModelImpl(messages.format("x-rubrics", schl.getDisplayName()), false, optModelList);
				optGroupModelList.add(optGroupModel1);
			}
		}
		
				
		SelectModel selModel = new SelectModelImpl(optGroupModelList, null);
		return selModel;
	}
	public SelectModel getRubricModelMy(){
		List<OptionGroupModel> optGroupModelList = new ArrayList<OptionGroupModel>();
		List<Rubric> rList = aDAO.getRubricsByOwner(getCurUser());
		
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (Rubric r : rList) {
			OptionModel optModel = new OptionModelImpl(r.getName(), r);
			optModelList.add(optModel);
		}
		if(!optModelList.isEmpty()){
			OptionGroupModel optGroupModel1 = new OptionGroupModelImpl(messages.get("my-rubrics"), false, optModelList);
			optGroupModelList.add(optGroupModel1);
		}
		
		SelectModel selModel = new SelectModelImpl(optGroupModelList, null);
		return selModel;
	}
	public SelectModel getRubricModelMaster(){
		List<OptionGroupModel> optGroupModelList = new ArrayList<OptionGroupModel>();
		List<Rubric> mRList = aDAO.getMasterRubricsAllSchool();
		
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (Rubric r : mRList) {
			OptionModel optModel = new OptionModelImpl(r.getName(), r);
			optModelList.add(optModel);
		}
		if(!optModelList.isEmpty()){
			OptionGroupModel optGroupModel2 = new OptionGroupModelImpl(messages.get("master-rubrics"), false, optModelList);
			optGroupModelList.add(optGroupModel2);
		}
		
		SelectModel selModel = new SelectModelImpl(optGroupModelList, null);
		return selModel;
	}
	protected List<Integer> getAccessibleSchlIDs(User u){
		List<Integer> idList = new ArrayList<Integer>();
		SysRole sysRole = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		for(SysroleUser srUser : u.getExtraRoles()){
			if(srUser.getSysRole().equals(sysRole)){
				idList.add(srUser.getParam());
			}
		}
		return idList;
	}
	
	public String getAssesseeRoleNames(Project p){
		String str = "";
		for(ProjRole pr : p.getType().getRoles()){
			if(pr.getPrivileges().contains(new Privilege(PrivilegeAssessment.IS_ASSESSEE))){
				str += pr.getDisplayName()+" / ";
			}
		}
		
		return Util.removeLastSeparator(str, " / ");
	}
	public int getTotalMaxScore(List<RubricCriteria> rCritList){
		int max = Integer.MIN_VALUE;
		for(RubricCriterion rcion : rCritList.get(0).getCriterions()){
			if(max<rcion.getScore())
				max = rcion.getScore();
		}
		return max * rCritList.size();
	}
	public int getTotalWeight(List<AssessCriteria> rCritList){
		int total = 0;
		if(rCritList.isEmpty())
			return 0;
		for(AssessCriteria rc : rCritList){
			total += rc.getWeightage();
		}
		return total;
	}
	public float getPossibleScore(List<AssessCriteria> aCritList){
		return aCritList.get(0).getMaxCritScore() * aCritList.size();
	}
	private int[] gmatScoreList = {Integer.parseInt(messages.get("gmat-6-H")), Integer.parseInt(messages.get("gmat-6-L"))
			,Integer.parseInt(messages.get("gmat-5-H")), Integer.parseInt(messages.get("gmat-5-L"))
			,Integer.parseInt(messages.get("gmat-4-H")), Integer.parseInt(messages.get("gmat-4-L"))
			,Integer.parseInt(messages.get("gmat-3-H")), Integer.parseInt(messages.get("gmat-3-L"))
			,Integer.parseInt(messages.get("gmat-2-H")), Integer.parseInt(messages.get("gmat-2-L"))
			,Integer.parseInt(messages.get("gmat-1-H")), Integer.parseInt(messages.get("gmat-1-L"))
			};
	private String[] gmatNumList = {"6-H","6-L","5-H","5-L","4-H","4-L","3-H","3-L" ,"2-H","2-L","1-H","1-L"};

	public int convertGMATtoScore(String scoreHL){
		int score = 0;
		for(int i=0; i < gmatNumList.length; i++){
		if(gmatNumList[i].equals(scoreHL)){
			score = gmatScoreList[i];
			break;
		}
		}
		return score;
	}
	private String[] gradeList = {"A+","A","A-","B+","B","B-","C+","C","D+","D","F"};
	
	private final int aplus = Integer.parseInt(messages.get("grade-a-plus"));
	private final int a = Integer.parseInt(messages.get("grade-a"));
	private final int aminus = Integer.parseInt(messages.get("grade-a-minus"));
	private final int bplus = Integer.parseInt(messages.get("grade-b-plus"));
	private final int b = Integer.parseInt(messages.get("grade-b"));
	private final int bminus = Integer.parseInt(messages.get("grade-b-minus"));
	private final int cplus = Integer.parseInt(messages.get("grade-c-plus"));
	private final int c = Integer.parseInt(messages.get("grade-c"));
	private final int dplus = Integer.parseInt(messages.get("grade-d-plus"));
	private final int d = Integer.parseInt(messages.get("grade-d"));
	private final int f = Integer.parseInt(messages.get("grade-f"));
	
	public String convertScoreToGradeClass(float score, int possibleScore){
		if(possibleScore!=0){
			float percent = 100 * score/possibleScore;
			if(percent>=aplus)
				return "aplus";
			if(percent>=a)
				return "a";
			if(percent>=aminus)
				return "aminus";
			if(percent>=bplus)
				return "bplus";
			if(percent>=b)
				return "b";
			if(percent>=bminus)
				return "bminus";
			if(percent>=cplus)
				return "cplus";
			if(percent>=c)
				return "c";
			if(percent>=dplus)
				return "dplus";
			if(percent>=d)
				return "d";
			if(percent>=f)
				return "f";
		}
		return "na";
	}
	
	public String convertScoreToGrade(float score, int possibleScore){
		if(possibleScore!=0){
			float percent = 100 * score/possibleScore;
			if(percent>=aplus)
				return "A+";
			if(percent>=a)
				return "A";
			if(percent>=aminus)
				return "A-";
			if(percent>=bplus)
				return "B+";
			if(percent>=b)
				return "B";
			if(percent>=bminus)
				return "B-";
			if(percent>=cplus)
				return "C+";
			if(percent>=c)
				return "C";
			if(percent>=dplus)
				return "D+";
			if(percent>=d)
				return "D";
			if(percent>=f)
				return "F";
		}
		return "NA";
	}
	
	public String getColorCodeRangeAplus(){
		return (aplus) + "-" +"100%" ;
	}
	public String getColorCodeRangeA(){
		return (a) + "-"+ (aplus-0.01) +"%" ;
	}
	public String getColorCodeRangeAMinus(){
		return (aminus) + "-"+ (a-1) +"%" ;
	}
	public String getColorCodeRangeBplus(){
		return (bplus) + "-" + (aminus-0.01)+ "%" ;
	}
	public String getColorCodeRangeB(){
		return (b) + "-"+ (bplus-0.01) +"%" ;
	}
	public String getColorCodeRangeBMinus(){
		return (bminus) + "-"+ (b-0.01) +"%" ;
	}
	public String getColorCodeRangeCplus(){
		return (cplus) + "-" + (bminus-0.01)+ "%" ;
	}
	public String getColorCodeRangeC(){
		return (c) + "-"+ (cplus-0.01) +"%" ;
	}
	public String getColorCodeRangeDplus(){
		return (dplus) + "-" + (c-0.01)+ "%" ;
	}
	public String getColorCodeRangeD(){
		return (d) + "-"+ (dplus-0.01) +"%" ;
	}
	public String getColorCodeRangeF(){
		return (f) + "-"+ (d-0.01) +"%" ;
	}
	
	public String getColorCodeGradeAplus() {
		return "A+";
	}
	public String getColorCodeGradeA() {
		return "A";
	}
	public String getColorCodeGradeAminus() {
		return "A-";
	}
	public String getColorCodeGradeBplus() {
		return "B+";
	}
	public String getColorCodeGradeB() {
		return "B";
	}
	public String getColorCodeGradeBminus() {
		return "B-";
	}
	public String getColorCodeGradeCplus() {
		return "C+";
	}
	public String getColorCodeGradeC() {
		return "C";
	}
	public String getColorCodeGradeDplus() {
		return "D+";
	}
	public String getColorCodeGradeD() {
		return "D";
	}
	public String getColorCodeGradeF() {
		return "F";
	}
	
	public SelectModel getGradeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for(String grd : gradeList){
			optModelList.add(new OptionModelImpl(grd, grd));
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
	public boolean canAdminAccess(Project proj){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC)){
			List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
			if(proj.getSchool() != null && accessibleSchlIDs.contains(proj.getSchool().getId()) )
				return true;
		}
		return false;
	}
	
	public boolean canAccessModule(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.ACCESS_MODULE )){
			return true;
		}
		return false;
	}
	public boolean canGradeAssessment(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.GRADE_ASSESSMENT )){
			return true;
		}
		return false;
	}
	public boolean canViewAssessmentGrade(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT_GRADES )){
			return true;
		}
		return false;
	}
	public boolean canCreateAssessment(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.CREATE_ASSESSMENT )){
			return true;
		}
		return false;
	}
	
	public boolean canViewAssessment(Assessment a){
		if(canAdminAccess(a.getProject()))
			return true;
		if(a.getProject().isReference())
			return true;
		if(a.getCreator().equals(getCurUser()))
			return true;
		ProjUser pu = a.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT )){
			return true;
		}
		if(a.isAllowView())
			return true;
		
		return false;
	}
	public boolean canEditAssessment(Assessment a){
		if(canAdminAccess(a.getProject()))
			return true;
		if(a.getProject().isReference())
			return false;
		ProjUser pu = a.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.EDIT_ASSESSMENT )){
			return true;
		}
		if(a.getCreator().equals(getCurUser()))
			return true;
		return false;
	}
	public boolean canDeleteAssessment(Assessment a){
		if(canAdminAccess(a.getProject()))
			return true;
		if(a.getProject().isReference())
			return false;
		ProjUser pu = a.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.DELETE_ASSESSMENT )){
			return true;
		}
		if(a.getCreator().equals(getCurUser()))
			return true;
		return false;
	}
	public boolean canRelease(Assessment assmt) {
		if (!assmt.getAllowReleaseResult()) return false;
		
		Date toDay = new Date();
		if (assmt.getRdate() == null) return true;
		if (toDay.before(assmt.getRdate())) {
			return false;
		}
		return true;
	}
	public boolean canViewComments(AssessmentUser assmtUser){
		if (assmtUser == null) return false;
		Assessment assmt = assmtUser.getAssessment();
		
		//if (! assmtUser.hasComments())		return false;
				
		if(canAdminAccess(assmt.getProject()))
			return true;
		ProjUser pu = assmt.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT_GRADES )){
			return true;
		}
		
		if (assmt.getAllowViewCommentRightAway()) return true;
		
		if (!canRelease(assmt)) return false;

		if(assmt.getAllowViewComment()
			&& assmtUser.getAssessee().equals(getCurUser())){
			return true;
		}
		return false;
	}
	public boolean canViewCrit(Assessment assmt){
//		if (!canRelease(assmt)) return false;   'not need to limit the date for this
		if(canAdminAccess(assmt.getProject()))
			return true;
		
		ProjUser pu = assmt.getProject().getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeAssessment.VIEW_ASSESSMENT )){
			return true;
		}

		if(assmt.isAllowViewGradeCriteria()){
			return true;
		}
		
		return false;
	}
	public boolean isAssessee(ProjUser pu){
		if(pu.hasPrivilege(PrivilegeAssessment.IS_ASSESSEE))
			return true;
		return false;
	}
	public List<User> getAllAssessees(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(isAssessee(pu))
				uList.add(pu.getUser());
		}
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		
		return uList;
	}
	public List<User> getAllAssessees(Project p, Assessment assmt){
		List<User> uList = new ArrayList<User>();
		if(assmt.getGroup()!=null){
			for(GroupUser gu : assmt.getGroup().getGroupUsers()){
				List<User> guList = gu.getUsers();
				Collections.sort(guList, new Comparator<User>(){
					@Override
					public int compare(User o1, User o2) {
						return o1.getDisplayName().compareTo(o2.getDisplayName());
					}
				});
				uList.addAll(guList);
			}
			uList.addAll(getNonGroupedUsers(assmt.getGroup()));
		}
		else{
			uList = getAllAssessees(p);
		}
		
		return uList;
	}
	
	public List<User> getNonGroupedUsers(Group group){
		List<User> uList = getAllAssessees(group.getProject());	
		for(int i=0; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			uList.removeAll(gu.getUsers());
		}
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return uList;
	}
	
	public int getTextareaWidth(int numColum){
		return getTextareaWidth(numColum, 400);
	}
	public int getTextareaWidth2(int numColum){
		return getTextareaWidth(numColum, 450);
	}
	public int getTextareaWidth(int numColum, int initwidth){
		int width = 974;
		int padding = 4;
		int minwidth = 70;
		
		int num = Math.round((width - initwidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
	public String getCritNameWidthPercentNum(int critWidthPercent,  int numColum){
		if(numColum==0) 
			return "1%";
		
		int num = Math.round((critWidthPercent)/numColum);
		return num + "%";
	}
	
	public String getCritNameWidthPercent(int critWidthPercent,  Assessment assmt){
		int numColum = 0;
		if(assmt.getCriterias()==null) //in case not-use-rubric
			numColum = 0;
		else
			numColum = assmt.getCriterias().size();
		
		if(numColum==0) 
			return "1%";
		
		int num = Math.round((critWidthPercent)/numColum);
		return num + "%";
	}
	public String getComputedCritScore(AssessCriteria crit, AssessmentUser assmtUser){
		String s = getComputedCritScoreDisplay(crit, assmtUser);
		if ("-".equals(s))
			return "0";
		return s;
	}
	public String getComputedCritScoreDisplay(AssessCriteria crit, AssessmentUser assmtUser){
		String critScoreDisplay = getCritScoreDisplay(crit, assmtUser) ;
		float critScore = 0;
		if ("-".equals(critScoreDisplay))
			return "-";
		else
			critScore = Float.parseFloat(critScoreDisplay);
		
		float max = crit.getMaxCritScore();
		if(max==0)
			return "0";
		
		return Util.formatDecimal2((float) crit.getWeightage() * critScore / max);	
	}
	public String getComputedCritScoreDisplayGMAT(AssessCriteria crit, AssessmentUser assmtUser){
		String critScoreDisplay = getCritScoreDisplay(crit, assmtUser) ;
		float critScore = 0;
		if ("-".equals(critScoreDisplay))
			return "-";
		else
			critScore = Float.parseFloat(critScoreDisplay);
		
		
		
		return Util.formatDecimal2((float) crit.getWeightage() * critScore / 100);	
	}
	public float getCritScore(AssessCriteria crit, AssessmentUser assmtUser){
		String s = getCritScoreDisplay(crit, assmtUser);
		if("-".equals(s))
			return 0;
		return Float.parseFloat(s);
	}
	public String getCritScoreDisplay(AssessCriteria crit, AssessmentUser assmtUser){
		if(assmtUser ==null)
			return "-";
		for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
			if(aucg != null){
				if(aucg.getCriteria().equals(crit)){
					if(aucg.getScore() != null)
						return aucg.getScore()+"";
					else
						break;
				}
			}
		}
		
		for(AssessCriterion ac : assmtUser.getSelectedCriterions()){
			if(ac !=null)
				if(ac.getCriteria().equals(crit))
					return ac.getScore() +"";
		}
		return "-";	
	}
	
	
	
	public String getCritComment(AssessCriteria crit, AssessmentUser assmtUser){
		for(AssmtUserCriteriaGrade aucg : assmtUser.getSelectedCritGrades()){
			if(aucg != null){
				if(aucg.getCriteria().equals(crit)){
					if(aucg.getComment() != null)
						return aucg.getComment();
					else
						break;
				}
			}
		}
		return "";
	}
	
	
	public SelectModel getGroupModel(Project proj){
		List<Group> gList = groupDAO.getGroupsByProject(proj);
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (Group g : gList) {
			OptionModel optModel = new OptionModelImpl(g.getGroupType(), g);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public String getGroupTypeNumber(Group group, User user){
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return ""+ gu.getGroupNum();
		else
			return "";
	}
	public String getGroupTypeName(Group group, User user){
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return gu.getGroupNumNameDisplay();
		else
			return "";
	}
	
	public GroupUser getGroupUser(Group group, User user){
		for(int i=0 ; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			if(gu.getUsers().contains(user)){
				return gu;
			}
		}
		return null;
	}
	public List<User> getOtherStudentsInSameGroup(Group group, User user){
		List<User> uList = new ArrayList<User>();
		for(int i=0 ; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			if(gu.getUsers().contains(user)){
				 int index = gu.getUsers().indexOf(user);
				 for(int j=0; j<gu.getUsers().size(); j++){
					 if(j!=index){
						uList.add(gu.getUsers().get(j)); 
					 }
				 }
				 break;
			}
		}
		return uList;
	}
	
	
	public Link getViewCritCmmtURL(AssessCriteria ACrit, AssessmentUser assmtUser){
		return linkSource.createPageRenderLinkWithContext(ViewCritComment.class, ACrit.getId(), assmtUser.getId());
	}
	
	public Asset getAttachFileIcon(AttachedFile attFile){
		return pageResourceHome.getIcon(attFile.getContentType(), attFile.getFileName());
	}
	public StreamResponse onRetrieveFile(String fId) {
		AssmtFile f = aDAO.getAssmtFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(f);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + f.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", f.getDisplayName()));
		}
	}
	public StreamResponse onRetrieveInstructorFile(String fId) {
		AssmtInstructorFile f = aDAO.getAssmtInstructorFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(f);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + f.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", f.getDisplayName()));
		}
	}
	
	public String getRubricOrderBy(Assessment assmt){
		if(assmt.getCriterias()!=null && assmt.getCriterias().size()>1 )
			if(assmt.getCriterias().get(0).getCriterions()!=null && assmt.getCriterias().get(0).getCriterions().size()>1 ){
				
				if(assmt.getCriterias().get(0).getCriterions().get(0)!=null 
						&& assmt.getCriterias().get(0).getCriterions().get(1)!=null
						&& assmt.getCriterias().get(0).getCriterions().get(0).getScore() 
							> assmt.getCriterias().get(0).getCriterions().get(1).getScore()){
					return "DESC";
				}
			}
		return "ASC";
	}
	
	
	
	
//	public String getSelectedClass(AssessmentUser assmtUser, String rubricOrder, AssessCriterion crion, AssessCriteria crit, int index){
//			float score = getCritScore(crit,assmtUser);
//			float lowerScore = 0;//crion.getScore();
//			float upperScore = crion.getScore();//crit.getCriterions().get(crit.getCriterions().size()-1).getScore();
//			int range = 0;
//			if("ASC".equals(rubricOrder)){
//				if(index > 0)
//					lowerScore = crit.getCriterions().get(index-1).getScore();
//				range = Math.round((NUM_SHADE*(score - lowerScore)) / (upperScore - lowerScore));
//				
//			}else{
//				if(index < crit.getCriterions().size()-1)
//					lowerScore = crit.getCriterions().get(index+1).getScore();
//				range = NUM_SHADE - Math.round((NUM_SHADE*(score - lowerScore)) / (upperScore - lowerScore));
//				
//			}
//			
//			if(lowerScore < score && score <= upperScore)
//				return "selected" + range;
//			else
//				return "";
//	}
	public String getSelectedClass(AssessmentUser assmtUser, String rubricOrder, AssessCriterion crion, AssessCriteria crit, int index){
			String s = getCritScoreDisplay(crit, assmtUser);
			if("-".equals(s))
				return "";
			
			float score = Float.parseFloat(s);
		//	float lowerScore = 0;//crion.getScore();
		//	float upperScore = crion.getScore();//crit.getCriterions().get(crit.getCriterions().size()-1).getScore();
		//	int range = 0;
			if(crion.equals(scoreToEvalCriterion(crit, score, rubricOrder)))
				return "selected" ;
			else
				return "";
	}
	public AssessCriterion scoreToEvalCriterion(AssessCriteria assmtCrit, float score, String rubricOrder){
		if(assmtCrit.getCriterions().size()==1)
			return assmtCrit.getCriterions().get(0);
			
		if("ASC".equals(rubricOrder)){
			AssessCriterion prev = assmtCrit.getCriterions().get(0);
			if(score <= prev.getScore())
				return prev;
			for(int i=1; i<assmtCrit.getCriterions().size(); i++ ){
				AssessCriterion cur = assmtCrit.getCriterions().get(i);
				if(score <= cur.getScore()){
					float mid = (float)(prev.getScore() + cur.getScore()) / 2;
					if(score < mid ){
						return prev;
					}
					else{
						return cur;
					}
				}
				prev = cur;
			}
		}
		else{
			AssessCriterion prev = assmtCrit.getCriterions().get(assmtCrit.getCriterions().size()-1);
			if(score <= prev.getScore())
				return prev;
			for(int i=assmtCrit.getCriterions().size()-2; i>=0; i-- ){
				AssessCriterion cur = assmtCrit.getCriterions().get(i);
				if(score <= cur.getScore()){
					float mid = (float)(prev.getScore() + cur.getScore()) / 2;
					if(score < mid ){
						return prev;
					}
					else{
						return cur;
					}
				}
				prev = cur;
			}
		}
		
		return null;
	}
}
