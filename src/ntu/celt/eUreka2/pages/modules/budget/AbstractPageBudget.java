package ntu.celt.eUreka2.pages.modules.budget;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.PrivilegeBudget;
import ntu.celt.eUreka2.modules.budget.Transaction;
import ntu.celt.eUreka2.modules.budget.TransactionAttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageBudget {
	public String getModuleName(){
		return PredefinedNames.MODULE_BUDGET;
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private Messages messages;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Logger logger;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_BUDGET);
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
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
	public boolean canViewBudget(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBudget.VIEW_BUDGET )){
			return true;
		}
		return false;
	}
	
	public boolean canCreateTransaction(Project curProj){
		if(canAdminAccess(curProj))
			return true;
		if(curProj.isReference())
			return false;
		ProjUser pu = curProj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBudget.ADD_TRANSACTION )){
			return true;
		}
		return false;
	}
	
	public boolean canDeleteTransaction(Transaction t){
		Project proj = t.getBudget().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeBudget.DELETE_TRANSACTION ))
			||	getCurUser().equals(t.getCreator())){
			return true;
		}
		return false;
	}
	public boolean canEditTransaction(Transaction transact){
		Project proj = transact.getBudget().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(transact.getCreator().equals(getCurUser()))
			return true;
		
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBudget.EDIT_TRANSACTION )){
			return true;
		}
		return false;
	}
	
	public StreamResponse onRetrieveAttachment(String fId) {
		TransactionAttachedFile att = budgetDAO.getAttachedFileById(fId);
		if(att==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(att);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + att.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", att.getDisplayName()));
		}
	}
}
