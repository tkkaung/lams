package ntu.celt.eUreka2.pages.admin.project;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ProjTypeSetting;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageAdminProject {
	
	@Inject
	private Messages messages;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private Request request;
	@Inject
	private Logger logger;
	@Inject
	private AttachedFileManager attFileManager;

	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1, o2};
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public boolean isFirst(int index, List<Object> list){
		if(index == 0)
			return true;
		return false;
	}
	public boolean isLast(int index, List<Object> list){
		if(index == list.size()-1)
			return true;
		return false;
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	
	public String getContextPath(){
		return request.getContextPath();
	}
	
	public boolean canManageSystemData(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA))
			return true;
		
		return false;
	}
	public boolean canManageProjects(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJECT))
			return true;
		
		return false;
	}
	public boolean canCreateProject(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.CREATE_PROJECT))
			return true;
		
		return false;
	}
	public boolean canDeleteProject(Project proj){
	/*	if(proj.isReference())
			return false;
	*/	
		if(canManageProjects())
			return true;
		if(proj.getType().getName().equals(PredefinedNames.PROJTYPE_CAO))
			return false;
		if(proj.getType().getName().equals(PredefinedNames.PROJTYPE_FYP))
			return false;
		
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeProject.IS_LEADER)){
			return true;
		}
		
		return false;
	}
	public boolean canEditProject(Project proj){
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!proj.getType().getSettings().contains(ProjTypeSetting.CAN_UPDATE_BASE_INFO))
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.UPDATE_PROJECT))
			return false;
			
		return true;
	}
	public boolean canChangeProjectStatus(Project proj){
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.CHANGE_STATUS))
			return false;
		
		return true;
	}
	public boolean canEnrollMember(Project proj){
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER))
			return false;
		
		return true;
	}
	public boolean canAssignModule(Project proj){
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(!pu.hasPrivilege(PrivilegeProject.ASSIGN_MODULE))
			return false;
		
		return true;
	}
	public boolean canAssignLeaderShipProfiling(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.LEADSHIP_PROFILING))
			return true;
		return false;
	}
	
	public StreamResponse onRetrieveAttachment(String fId) {
		ProjectAttachedFile att = projDAO.getAttachedFileById(fId);
		if(att==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(att);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + att.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", att.getDisplayName()));
		}
	}
	public boolean isCAO(Project project){
		if(project.getType().getName().equals(PredefinedNames.PROJTYPE_CAO))
			return true;
		return false;
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
		if(canManageSystemData()){
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
}
