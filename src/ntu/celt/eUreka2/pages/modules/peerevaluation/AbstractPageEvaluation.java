package ntu.celt.eUreka2.pages.modules.peerevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.assessment.RubricCriteria;
import ntu.celt.eUreka2.modules.assessment.RubricCriterion;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriteria;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationCriterion;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationUser;

@Import(stylesheet="context:lib/css/gradeColor.css")
public abstract class AbstractPageEvaluation {
	public String getModuleName(){
		return PredefinedNames.MODULE_PEER_EVALUATION;
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
	

	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_PEER_EVALUATION);
	}
	public int addInt(int a, int b){
		return a+b;
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
	public String getFirstClass(int index){
		if(isFirst(index))
			return "first";
		return "";
	}
	public boolean isLast(int index, List<Object> list){
		if(index == (list.size()-1))
			return true;
		return false;
	}
	public String getLastClass(int index, List<Object> list){
		if(isLast(index, list))
			return "last";
		return "";
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
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
	
	
	public int getTotalMaxScore(List<RubricCriteria> rCritList){
		int max = Integer.MIN_VALUE;
		for(RubricCriterion rcion : rCritList.get(0).getCriterions()){
			if(max<rcion.getScore())
				max = rcion.getScore();
		}
		return max * rCritList.size();
	}
	public int getTotalWeight(List<EvaluationCriteria> rCritList){
		int total = 0;
		if(rCritList.isEmpty())
			return 0;
		for(EvaluationCriteria rc : rCritList){
			total += rc.getWeightage();
		}
		return total;
	}
	public int getPossibleScore(List<EvaluationCriteria> aCritList){
		return aCritList.get(0).getMaxScore() * aCritList.size();
	}
	
	
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
	
	private String[] gradeList = {"A+","A","A-","B+","B","B-","C+","C","D+","D","F"};
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
	
	public boolean canManageEvaluation(Project proj){
		if(canAdminAccess(proj))
			return true;

		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && isLeader(pu)){
			return true;
		}
		return false;
	}
	public boolean canModerate(Evaluation eval){
		Date today = Util.getTodayWithoutTime();
		if(eval.getEdate()==null)
			return true;
		if(eval.getEdate().before(today))
			return true;

		return false;
	}
	//can student submits
	public boolean canSubmit(Evaluation eval){
		Date today = Util.getTodayWithoutTime();
		if(eval.getSdate()!=null && eval.getSdate().after(today)){
			return false;
		}
		if(eval.getEdate()!=null && eval.getEdate().before(today)){
			return false;
		}
		if(messages.get("Edited-by-Instructor").equals(getEvalStatus(eval, getCurUser()))){
			return false;
		}
		return true;
	}
	public boolean canCreateEvalWithoutGroup(){
		//allow all users to use
		return true;
		
		/*if(getCurUser().hasPrivilege(PrivilegeSystem.EVAL_WITHOUT_GROUP)){
			return true;
		}
		return false;*/
	}
	public boolean canUseOpenQuestion(){
		//allow all users to use
		return true;
		
		/*if(getCurUser().hasPrivilege(PrivilegeSystem.EVAL_WITHOUT_GROUP)){
			return true;
		}
		return false;*/
	}
	public boolean canUseAllowStudentViewGradeDetail(){
		//allow all users to use
		return true;
		
		/*if(getCurUser().hasPrivilege(PrivilegeSystem.EVAL_WITHOUT_GROUP)){
			return true;
		}
		return false;*/
	}
	
	public boolean isLeader(ProjUser pu){
		if(pu.hasPrivilege(PrivilegeProject.IS_LEADER))
			return true;
		return false;
	}
	public List<User> getAllAssessees(Project p){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : p.getMembers()){
			if(!isLeader(pu))
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
	public List<User> getAllAssessees(Project p, Group group){
		List<User> uList = new ArrayList<User>();
		if(group==null){
			return getAllAssessees(p);
		}
		for(GroupUser gu : group.getGroupUsers()){
			List<User> guList = gu.getUsers();
			Collections.sort(guList, new Comparator<User>(){
				@Override
				public int compare(User o1, User o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
			uList.addAll(guList);
		}
		uList.addAll(getNonGroupedUsers(group));
		
		return uList;
	}

	public List<User> getNonGroupedUsers(Group group){
		List<User> uList = new ArrayList<User>();
		if(group==null){
			return uList;
		}
		for(ProjUser pu : group.getProject().getMembers()){
			if(!isLeader(pu))
				uList.add(pu.getUser());
		}
		
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
	public String getComputedCritScore(EvaluationCriteria crit, EvaluationUser evalUser){
		int critScore = 0;
		for(EvaluationCriterion ac : evalUser.getSelectedCriterions()){
			if(ac.getCriteria().equals(crit)){
				critScore = ac.getScore();
				break;
			}
		}
		int max = crit.getMaxScore();
		if(max==0)
			return "0";
		
		return Util.formatDecimal((float) crit.getWeightage() * critScore / max);	
	}

	public int getCritScoreOriginal(EvaluationCriteria crit, EvaluationUser evalUser){
		int critScore = 0;
		if(evalUser!=null){
			for(EvaluationCriterion ac : evalUser.getSelectedCriterions()){
				if(ac.getCriteria().equals(crit)){
					critScore = ac.getScore();
					break;
				}
			}
		}
		return critScore;
	}
	public String getCritScoreOriginalDisplay(EvaluationCriteria crit, EvaluationUser evalUser){
		int score = getCritScoreOriginal(crit, evalUser);
		if(score==0)
			return "Nothing";
		else
			return score + "";
	}
	

	public int getCritScore(EvaluationCriteria crit, EvaluationUser evalUser){
		if(evalUser==null)
			return 0;
		Integer score = evalUser.getCritScoreEdited(crit);
		if(score == null){
			for(EvaluationCriterion ac : evalUser.getSelectedCriterions()){
				if(ac.getCriteria().equals(crit)){
					return ac.getScore();
				}
			}
			return 0;
		}
		return score;
	}
	
	public String getCritScoreDisplay(EvaluationCriteria crit, EvaluationUser evalUser){
		int score = getCritScore(crit, evalUser);
		if(score==0)
			return "";
		return ""+score;
	}
	public float getTotalScore(EvaluationUser evalUser){
		if(evalUser==null)
			return 0;
		float total = 0;
		if(evalUser.getEvaluation().getUseFixedPoint()){
			for(EvaluationCriterion ec : evalUser.getSelectedCriterionsDisplay()){
				total +=  ec.getScore();
			}
		}
		else{
			for(EvaluationCriterion ec : evalUser.getSelectedCriterionsDisplay()){
				total += (double) ec.getScore()* ec.getCriteria().getWeightage() /ec.getCriteria().getMaxScore();
			}
		}
		
		
		return total;
	}
	
/*	public float getTotalScoreOriginal(EvaluationUser evalUser){
		if(evalUser==null)
			return 0;
		float total = 0;
		for(EvaluationCriterion ec : evalUser.getSelectedCriterions()){
			total += ec.getScore()* ec.getCriteria().getWeightage() /ec.getCriteria().getMaxScore();
		}
		return total;
	}
*/	
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
		if(group ==null)
			return "";
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return ""+ gu.getGroupNum();
		else
			return "";
	}
	public String getGroupTypeName(Group group, User user){
		if(group ==null)
			return "";
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			return gu.getGroupNumNameDisplay();
		else
			return "";
	}
	public GroupUser getGroupUser(Group group, User user){
		if(group ==null)
			return null;
		
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
		if(group ==null)
			return uList;
		
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
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return uList;
	}
	
	
	public String getBooleanDisplay(Boolean bool){
		if (bool == null)
			return messages.get("no");
		if(bool)
			return messages.get("yes");
		else
			return messages.get("no");
	}
	public boolean canRelease(Evaluation eval) {
		return eval.getReleased();
	}
	
	
	public boolean canViewScoredCrit(Evaluation eval){
		if (!canRelease(eval)) return false;
		
		if(eval.getProject().isReference())
			return true;
		if(eval.isAllowViewScoredCriteria()){
			return true;
		}
		return false;
	}
	public boolean canViewGradeDetail(Evaluation eval){
		if (!canRelease(eval)) return false;
		
		if(eval.getProject().isReference())
			return true;
		if(eval.getAllowViewGradeDetail() || eval.getAllowViewGradeDetailPeer()){
			return true;
		}
		return false;
	}
	public boolean canViewGrade(Evaluation eval){
		if (!canRelease(eval)) return false;

		if(eval.getProject().isReference())
			return true;
		if(eval.isAllowViewGrade()){
			return true;
		}
		
		return false;
	}
	
	public String getEvalStatusStudentView(Evaluation eval, User assessor){
		return getEvalStatus(eval, assessor, false);
	}	
	public String getEvalStatus(Evaluation eval, User assessor){
		return getEvalStatus(eval, assessor, true);
	}
	//when change, please update to BbIntegrateService also
	public String getEvalStatus(Evaluation eval, User assessor, boolean isInstructor){
		List<EvaluationUser> evalUserList = eval.getEvalUsersByAssessor(assessor);
		if(evalUserList.size()==0){
			String extraInfo = "";
			Date today = Util.getTodayWithoutTime();
			if(eval.getEdate()!=null && eval.getEdate().before(today)){
				extraInfo = " (Submission Closed)";
			}
			
			return messages.get("Not-Attempted") + extraInfo;
			
			//return messages.get("Not-Attempted");
		}
		
		boolean hasEdited = false;
		for(EvaluationUser eu : evalUserList){
			if(eu.getSelectedCriterionsByLeader().size()>0 
					|| eu.getEditedCmtStrength()!=null || eu.getEditedCmtWeakness()!=null
					|| eu.getEditedCmtOther()!=null){
				hasEdited = true;
				break;
			}
		}
		if(hasEdited){
			if(isInstructor)
				return messages.get("Edited-by-Instructor");
			else
				return messages.get("Edited-by-Instructor-studentview");
		}

		boolean hasNotSubmitted = false;
		for(EvaluationUser eu : evalUserList){
			if(!eu.isSubmited()){
				hasNotSubmitted = true;
				break;
			}
		}
		if(hasNotSubmitted)
			return messages.get("Not-Submited"); 
		
		return messages.get("Submited");
	}
	public String getRubricOrderBy(Evaluation eval){
		if(eval.getCriterias()!=null && eval.getCriterias().size()>1 )
			if(eval.getCriterias().get(0).getCriterions()!=null && eval.getCriterias().get(0).getCriterions().size()>1 ){
				
				if(eval.getCriterias().get(0).getCriterions().get(0)!=null 
						&& eval.getCriterias().get(0).getCriterions().get(1)!=null
						&& eval.getCriterias().get(0).getCriterions().get(0).getScore() 
							> eval.getCriterias().get(0).getCriterions().get(1).getScore()){
					return "DESC";
				}
			}
		return "ASC";
	}
	public EvaluationCriterion scoreToEvalCriterion(EvaluationCriteria evalCrit, float score){
		if(evalCrit.getCriterions().size()==1)
			return evalCrit.getCriterions().get(0);
			
		if("ASC".equals(getRubricOrderBy(evalCrit.getEvaluation()))){
			
//			Collections.sort(evalCrit.getCriterions(), new Comparator<EvaluationCriterion>(){
//				@Override
//				public int compare(EvaluationCriterion ec1,
//						EvaluationCriterion ec2) {
//					return ec1.getScore() - ec2.getScore();
//				}
//			});
			EvaluationCriterion prev = evalCrit.getCriterions().get(0);
			if(score <= prev.getScore())
				return prev;
			for(int i=1; i<evalCrit.getCriterions().size(); i++ ){
				EvaluationCriterion cur = evalCrit.getCriterions().get(i);
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
			EvaluationCriterion prev = evalCrit.getCriterions().get(evalCrit.getCriterions().size()-1);
			if(score <= prev.getScore())
				return prev;
			for(int i=evalCrit.getCriterions().size()-2; i>=0; i-- ){
				EvaluationCriterion cur = evalCrit.getCriterions().get(i);
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
	public Link getViewCmtStrengthURL(String evalUserId){
		return linkSource.createPageRenderLinkWithContext(ViewComment.class, evalUserId, "s");
	}
	public Link getViewCmtWeaknessURL(String evalUserId){
		return linkSource.createPageRenderLinkWithContext(ViewComment.class, evalUserId, "w");
	}
	public Link getViewCmtOtherURL(String evalUserId){
		return linkSource.createPageRenderLinkWithContext(ViewComment.class, evalUserId, "o");
	}
	
	public boolean isInvalidGroup(Group group){
		if(group==null)
			return false;  //	return true;
		for(GroupUser gu : group.getGroupUsers()){
			if(gu.getUsers().size()<2)
				return true;
		}
		if(getNonGroupedUsers(group).size()>0)
			return true;
		
		return false;
	}
	
	public String getCmmtStrengthName(Evaluation eval){
		if( eval.getCustomNameCmtStrength()!=null)
			return eval.getCustomNameCmtStrength();
		return messages.get("cmtStrength-label");
	}
	public String getCmmtWeaknessName(Evaluation eval){
		if(eval.getCustomNameCmtWeakness()!=null)
			return eval.getCustomNameCmtWeakness();
		return messages.get("cmtWeakness-label");
	}
	public String getCmmtOtherName(Evaluation eval){
		if(eval.getCustomNameCmtOther()!=null)
			return eval.getCustomNameCmtOther();
		return messages.get("cmtOther-label");
	}
	public int getNumQualitativeFeedback(Evaluation eval){
		int num = 0;
		if(eval.getUseCmtStrength())
			num++;
		if(eval.getUseCmtWeakness())
			num++;
		if(eval.getUseCmtOther())
			num++;
		
		return num;
	}
	public boolean hasQualitativeFeedback(Evaluation eval){
		if(getNumQualitativeFeedback(eval) > 0)
			return true;
		return false;
	}
	
	
	public String getNoGroupMessage(){
		if(canCreateEvalWithoutGroup()){
			return messages.get("random-grouping");
		}
		return "";
	}
	
	public String getCritNameWidthPercentNum(int critWidthPercent,  int numColum){
		if(numColum==0) 
			return "1%";
		
		int num = Math.round((critWidthPercent)/numColum);
		return num + "%";
	}
	
	public String getCritNameWidthPercent(int critWidthPercent,  Evaluation eval){
		int numColum = 0;
		if(eval.getCriterias()==null) //in case not-use-rubric
			numColum = 0;
		else
			numColum = eval.getCriterias().size();
		
		if(numColum==0) 
			return "1%";
		
		int num = Math.round((critWidthPercent)/numColum);
		return num + "%";
	}
	public String getOpQuesWidthPercent(int opquesWidthPercent,  Evaluation eval){
		int numColum = 0;
		if(eval.getOpenEndedQuestions()==null)
			numColum = 0;
		else
			numColum = eval.getOpenEndedQuestions().size();
		if(numColum==0) 
			return "1%";
		
		int num = Math.round((opquesWidthPercent)/numColum);
		return num + "%";
	}
	public int getGroupSize(Group group){
		if(group == null)
			return 0;
		int max = 0;
		for(int i=0; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			int size = gu.getUsers().size();
			if(size > max)
				max = size;
		}	
		return max;
	}
	
	public int getStudentTotalFixedPoint(List<EvaluationUser> evalUserList){
		int total=0;
		for(EvaluationUser eu : evalUserList){
			//if( !eu.getAssessee().equals(eu.getAssessor())){
				for(EvaluationCriterion ec : eu.getSelectedCriterions()){
					EvaluationCriteria crit = ec.getCriteria();
					Integer score = eu.getCritScoreEdited(crit);
					if(score == null){
						score = ec.getScore();
					}
					total += score;
				}
			//}
		}
		return total;
	}
	public int getStudentCritFixedPoint(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		return getStudentCritFixedPoint(evalCrit, evalUserList, false);
	}
	public int getStudentCritFixedPoint(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList, boolean excludeSelf){
		int total=0;
		for(EvaluationUser eu : evalUserList){
			if(!excludeSelf && eu.getAssessee().equals(eu.getAssessor())){
				Integer score = getCritScore(evalCrit, eu);
				total += score;
			}else{
				Integer score = getCritScore(evalCrit, eu);
				total += score;
			}
		}
		return total;
	}
	public int getMaxGroupStudentTotalFixedPoint(GroupUser groupUser, Evaluation eval){
		int max = -1;
		if(groupUser == null)
			return -1;
		for(User u : groupUser.getUsers()){
			int t = getStudentTotalFixedPoint(eval.getEvalUsersByAssessee(u));
			max = Math.max(t, max);
		}
		return max;
	}
	
	public float getComputedCritScore(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		//get Average
		float total=0;
		int num = 0;
		for(EvaluationUser eu : evalUserList){
			if(!eu.getAssessee().equals(eu.getAssessor())){
				float score = getCritScore(evalCrit, eu);
				if(score!=0){
					total += score;
					num++;
				}
			}
		}
	//	logger.debug(".........num="+num+",total="+ total + ", evalCrit=" + evalCrit. getName() );
				
		if(num==0)
			return 0;
		return total/num;
	}
	
	public String getComputedCritScoreDisplay(EvaluationCriteria evalCrit, List<EvaluationUser> evalUserList){
		if(evalUserList ==null || evalUserList.isEmpty())
			return "-";
		
		return Util.formatDecimal(getComputedCritScore(evalCrit, evalUserList));
	}
}
