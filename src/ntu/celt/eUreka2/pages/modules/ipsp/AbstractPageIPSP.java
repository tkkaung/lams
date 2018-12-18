package ntu.celt.eUreka2.pages.modules.ipsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupUser;
import ntu.celt.eUreka2.modules.ipsp.IPSPDAO;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurvey;
import ntu.celt.eUreka2.modules.ipsp.IPSPSurveyUser;
import ntu.celt.eUreka2.modules.ipsp.IDimension;
import ntu.celt.eUreka2.modules.ipsp.IQuestionSet;
import ntu.celt.eUreka2.modules.ipsp.IQuestionType;

@Import(stylesheet="context:lib/css/ipspGraphColor.css")
public abstract class AbstractPageIPSP {
	public String getModuleName(){
		return PredefinedNames.MODULE_INFLUENCING_PESUADING_SURVEY;
	}
	@Inject
	private IPSPDAO ipspDAO;
	@Inject
	private Messages messages;
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private GroupDAO groupDAO;
	@Inject
	private PageRenderLinkSource linkSource;
	@Inject
	private Logger logger;
	
		
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	
	
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_INFLUENCING_PESUADING_SURVEY);
	}
	public double addDouble(double a, double b){
		return a+b;
	}
	public int addInt(int a, int b){
		return a+b;
	}
	public int substractInt(int a, int b){
		return a-b;
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
	public String nvl(Object obj){
		if(obj==null)
			return "";
		return String.valueOf(obj);
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
	public Object getNull(){
		return null;
	}
	public String boolean2Y(Boolean b){
		if(b==null || !b)
			return "";
		return "Y";
	}
	public int roundFull(double num){
		return (int) Math.round(num);
	}
	public String formatDecimal(double num){
		return Util.formatDecimal(num, "0.##");
	}
	public boolean or(boolean b1, boolean b2){
		return b1 || b2;
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

	public boolean canManageIPSPSurvey(Project proj){
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
	public boolean canClearScore(Project proj){
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
	public boolean canManageQuestionSet(Project proj){
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
	public boolean canDeleteQuestionSet(IQuestionSet qset, Project project){
		if(qset.getOwner().equals(getCurUser()))
			return true;
		if(canManageQuestionSet(project))
			return true;
		return false;
	}

	public boolean canEditQSet(IQuestionSet qset, Project project){
		if(qset.getOwner().equals(getCurUser()))
			return true;
		if(canManageQuestionSet(project))
			return true;
		return false;
	}

	//can student submits
	public boolean canSubmit(IPSPSurvey ipsp){
		Date today = Util.getTodayWithoutTime();
		if(ipsp.getSdate()!=null && ipsp.getSdate().after(today)){
			return false;
		}
		if(ipsp.getEdate()!=null && ipsp.getEdate().before(today)){
			return false;
		}
		return true;
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
	

	
	
	public List<User> getNotSubmitedAssesseesByIPSPSurvey(IPSPSurvey ipsp){
		List<User> uList = ipspDAO.getNotSubmitedUsersByIPSPSurvey(ipsp);
		Project p = ipsp.getProject();
		for(int i=uList.size()-1; i>=0; i--){
			User u = uList.get(i);
			ProjUser pu = p.getMember(u);
			if(isLeader(pu))
				uList.remove(i);
		}
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return uList;
	}
	
	public List<User> getNotAssessedAssesseesByIPSPSurvey(IPSPSurvey ipsp){
		List<User> uList = ipspDAO.getNotAssessedUsersByIPSPSurvey(ipsp);
		Project p = ipsp.getProject();
		for(int i=uList.size()-1; i>=0; i--){
			User u = uList.get(i);
			ProjUser pu = p.getMember(u);
			if(isLeader(pu))
				uList.remove(i);
		}
		Collections.sort(uList, new Comparator<User>(){
			@Override
			public int compare(User o1, User o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return uList;
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
	public SelectModel getQsetModel(Project proj){
		List<IQuestionSet> qsetList1 =  ipspDAO.searchIQuestionSets(null, getCurUser(),  null);
		List<IQuestionSet> qsetList2 =  ipspDAO.searchIQuestionSets(proj);
		List<IQuestionSet> qsetList = Util.union(qsetList1, qsetList2);
		
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		for (IQuestionSet qset : qsetList) {
			OptionModel optModel = new OptionModelImpl(qset.getName(), qset);
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
	public String getGroupTutorName(Group group, User user){
		GroupUser gu = getGroupUser(group, user);
		if(gu!=null)
			if(null!=gu.getTutor())
				return gu.getTutor().getDisplayName();
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
	public int getMaxStudentPerGroup(Group group){
		if(group==null)
			return 0;
		int max = 0;
		for(int i=0 ; i<group.getGroupUsers().size(); i++){
			GroupUser gu = group.getGroupUsers().get(i);
			int size = gu.getUsers().size();
			if(size>max)
				max = size;
		}
		return max;
	}
	
	public String getStatus(IPSPSurveyUser ipspUser){
		return getStatus(ipspUser, "");
	}
	public String getStatus(IPSPSurveyUser ipspUser, String defaultStatusName){
		if(ipspUser == null)
			return defaultStatusName;
		if(ipspUser.isFinished())
			return "Done";
		if(ipspUser.getLastQuestionNum()==0)
			return "Last attempt " + ipspUser.getStartAssessTimeDisplay() + " - " + ipspUser.getLastAssessTimeDisplay();
		
		return ipspUser.getLastQuestionNum() + " of " + ipspUser.getSurvey().getQuestions().size();
	}
	public String getStatusForCheck(IPSPSurvey ipsp, User user) {
		IPSPSurveyUser cu = getIPSPSurveyUser(ipsp, user);
		return getStatus(cu, "Not-Attempt");
	}
	
	public IPSPSurveyUser getIPSPSurveyUser(IPSPSurvey ipsp, User assessor){
		List<IPSPSurveyUser> ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, assessor, assessor, null,  null);
		if(ipspUserList.size()>0)
			return ipspUserList.get(0);
		return null;
	}
	
	public long countIPSPSurveyByQuestionSet(IQuestionSet qset){
		return ipspDAO.countIPSPSurveyByIQuestionSet(qset);
	}
	
	@CommitAfter
	public void removeExpiredIPSPSurveyUser(){
		List<IPSPSurveyUser> puList =  ipspDAO.getExpiredIPSPSurveyUsers();
		for(IPSPSurveyUser pu : puList){
			pu.getQuestionScores().clear();
			pu.setLastQuestionNum(0);
			pu.increaseResetCount();
			
			ipspDAO.saveIPSPSurveyUser(pu);
		}
	}
	
	public String getGraphTDclass(int tdScaleNum, double score){
		if( (score+1) <= tdScaleNum){
			return "scEmpty";
		}
		if(tdScaleNum <= 2){
			return "scL";
		}
		if(tdScaleNum <= 5){
			return "scM";
		}
		if(tdScaleNum <= 7){
			return "scH";
		}
		return "";
	}
	
	public boolean isMyAssessment(IPSPSurveyUser ipspUser){
		if(ipspUser.getAssessor().equals(getCurUser()))
			return true;
		return false;
	}
	
	public double getAverageNorms(Project proj, Integer lDimensionID){
		return ipspDAO.getAverageNormsByDimension(proj, lDimensionID);
	}
	public double getSTDEVNorms(Project proj, Integer lDimensionID){
		return ipspDAO.getSTDEVNormsByDimension(proj, lDimensionID);
	}
	public double getAverageNormsByStyleGroup(Project proj, String StyleGroup){
		return ipspDAO.getAverageNormsByDimensionStyleGroup(proj, StyleGroup);
	}
	public double getSTDEVNormsByStyleGroup(Project proj, String StyleGroup){
		return ipspDAO.getSTDEVNormsByDimensionStyleGroup(proj, StyleGroup);
	}
	
	public int getTimeTen(int i){
		return i*10;
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
	
	@CommitAfter
	public void checkAndUpdateAvgScore(IPSPSurvey ipsp){
		List<IPSPSurveyUser> ipspUserList = ipspDAO.searchIPSPSurveyUser(ipsp, null, null, null, true);
		for(int i = 0 ; i<ipspUserList.size(); i++){
			IPSPSurveyUser su = ipspUserList.get(i);
		//	logger.error("...........su:" + su.getId());
			if(su.getScore1() == null){
				for(int j=1; j<=IDimension.NUM_DIMENSIONS; j++){
					double score = ipspDAO.getAverageScoreByDimension(su, j);
					if(score != -1.0){
						su.setScoreDim(score, j);
						//logger.error("...........j:" + j + ", score:" + score);
					}
				}
				ipspDAO.saveIPSPSurveyUser(su);
			}
		}
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


}
