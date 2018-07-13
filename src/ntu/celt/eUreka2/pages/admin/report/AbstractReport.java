package ntu.celt.eUreka2.pages.admin.report;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.dao.ProjectDAO;
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


public abstract class AbstractReport {
	@Inject
	private SysRoleDAO sysRoleDAO; 
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Inject
	private Messages messages;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private ProjectDAO projDAO;
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public String textarea2html(String str){
		return Util.textarea2html(str); 
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	public String truncateString(String str, int numChar){
		return Util.truncateString(str, numChar);
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public String getBooleanDisplay(Boolean bool){
		if (bool == null)
			return messages.get("no");
		if(bool)
			return messages.get("yes");
		else
			return messages.get("no");
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
	
	public boolean canManageSystemData(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		
		return false;
	}
	public boolean canManageReport(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		return false;
	}
	public boolean canViewAssessmentReport(){
		if(canManageReport())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC))
			return true;
		
		return false;
	}
	public boolean canViewEvaluationReport(){
		if(canManageReport())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC))
			return true;
		
		return false;
	}
	public boolean canViewAnalyticReport(){
		if(canManageReport())
			return true;
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC))
			return true;
		
		return false;
	}
	
	public School getDefaultSchool(){
		List<Integer> accessibleSchlIDs = getAccessibleSchlIDs(getCurUser());
		for(Integer schlId : accessibleSchlIDs){
			School s = schoolDAO.getSchoolById(schlId);
			if(s!=null){
				return s;
			}
		}
		return null;
	}
	public SelectModel getSchoolModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = schoolDAO.getAllSchools();
		if(canManageReport()){
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
	public SelectModel getTermModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
			List<String> termList = projDAO.getAllTerms();
			for (String t : termList) {
				OptionModel optModel = new OptionModelImpl(t, t);
				optModelList.add(optModel);
			}
		
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
}
