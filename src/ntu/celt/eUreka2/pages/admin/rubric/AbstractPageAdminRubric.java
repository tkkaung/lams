package ntu.celt.eUreka2.pages.admin.rubric;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
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

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AbstractPageAdminRubric {
	@Inject
	private AssessmentDAO aDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private Messages messages;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public String encode(String str){
		return Util.encode(str);
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public String textarea2html(String str){
		return Util.textarea2html(str); 
	}
	
	public boolean canManageRubrics(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC))
			return true;
		return false;
	}
	public boolean canManageSchoolRubrics(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC))
			return true;
		if(canManageRubrics())
			return true;
		return false;
	}
	public String getBreadcrumbAdmin(){
		if(canManageRubrics())
			return messages.get("admin-manage-rubric")+"=admin/rubric/manage";
		return "";
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
	public SelectModel getRubricModel(){
		List<OptionGroupModel> optGroupModelList = new ArrayList<OptionGroupModel>();
		List<Rubric> mRList = aDAO.getMasterRubricsAllSchool();
		List<Rubric> rList = aDAO.getRubricsByOwner(getCurUser());
		rList.removeAll(mRList); //not include 'master rubrics' in 'my-rubrics'
		
		
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		if(getCurUser().getSchool()!=null && !accessibleSchlIDs.contains(getCurUser().getSchool().getId())){
			accessibleSchlIDs.add(getCurUser().getSchool().getId());
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
	public SelectModel getSchoolModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = schoolDAO.getAllSchools();
		if(canManageRubrics()){
			for (School s : schoolList) {
				OptionModel optModel = new OptionModelImpl(s.getDisplayNameLong(), s);
				optModelList.add(optModel);
			}
		}
		else{
			List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
			
			//*** filter 
			for(int i=schoolList.size()-1; i>=0; i--){
				if(!accessibleSchlIDs.contains(schoolList.get(i).getId()))
					schoolList.remove(i);
			}
			for (School s : schoolList) {
				OptionModel optModel = new OptionModelImpl(s.getDisplayNameLong(), s);
				optModelList.add(optModel);
			}
		}
		
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
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
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
	}
	
	public int getTotalMaxScore(List<RubricCriteria> rCritList){
		int max = 0;
		if(rCritList.isEmpty())
			return 0;
		for(RubricCriterion rcion : rCritList.get(0).getCriterions()){
			if(max<rcion.getScore())
				max = rcion.getScore();
		}
		return max * rCritList.size();
	}
	public int getTotalWeight(List<RubricCriteria> rCritList){
		int total = 0;
		if(rCritList.isEmpty())
			return 0;
		for(RubricCriteria rc : rCritList){
			total += rc.getWeightage();
		}
		return total;
	}
	public long getCountAssessmentByRubric(Rubric rubric){
		return aDAO.countAssessmentByRubric(rubric);
	}
	public long getCountEvaluationByRubric(Rubric rubric){
		return aDAO.countEvaluationByRubric(rubric);
	}
	
	public boolean canViewRubric(Rubric r){
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(r.isShared())
			return true;
		if(r.isMaster())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC) 
				&& (r.getSchool()==null || getAccessibleSchlIDs(getCurUser()).
				contains(r.getSchool().getId())))
			return true;
		
		return false;
	}
	
	public boolean canEditRubric(Rubric r){
		if(r.getOwner().equals(getCurUser()))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC))
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC) 
				&& (r.getSchool()==null || getAccessibleSchlIDs(getCurUser()).
						contains(r.getSchool().getId())))
					return true;
		
		
		return false;
	}
	public boolean canUseOpenQuestion(){
		//allow all users to use
		return true;
		
		/*if(getCurUser().hasPrivilege(PrivilegeSystem.EVAL_WITHOUT_GROUP)){
			return true;
		}
		return false;*/
	}
	
	public int getTextareaWidth(int numColum){
		int width = 974;
		int initwidth = 400;
		int padding = 4;
		int minwidth = 100;
		
		int num = Math.round((width - initwidth)/numColum - padding);
		return Math.max(num, minwidth);
	}
}
