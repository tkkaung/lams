package ntu.celt.eUreka2.pages.modules.elog;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
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
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.elog.ElogComment;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogFile;
import ntu.celt.eUreka2.modules.elog.ElogStatus;
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
import ntu.celt.eUreka2.pages.modules.resources.Home;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageElog {
	public String getModuleName(){
		return PredefinedNames.MODULE_ELOG;
	}
	@Inject
	private ModuleDAO moduleDAO;
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_ELOG);
	}
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Messages messages;
	@Inject
	private Logger logger;
	@Inject
	private Request request;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@InjectPage
	private Home pageResourceHome;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public Object[] getParams(String p, String f) {
		return new Object[] {p, f}; 
	}
	public String getNull(){
		return null;
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public String stripTags(String htmlStr){
		return Util.stripTags(htmlStr);
	}
	public String capitalize(String str){
		return Util.capitalize(str);
	}
	public StreamResponse onRetrieveFile(String fId) {
		ElogFile f = elogDAO.getElogFileById(fId);
		if(f==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(f);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + f.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", f.getDisplayName()));
		}
	}
	
	public List<AttachedFile> getImageAttachedFiles(List<AttachedFile> attList){
		List<AttachedFile> list = new ArrayList<AttachedFile>();
		for(AttachedFile att : attList){
			if(Util.isImageExtension(att.getFileName())){
				list.add(att);
			}
		}
		return list;
	}
	
	
	public List<AttachedFile> getNonImageAttachedFiles(List<AttachedFile> attList){
		List<AttachedFile> list = new ArrayList<AttachedFile>();
		for(AttachedFile att : attList){
			if(!Util.isImageExtension(att.getFileName())){
				list.add(att);
			}
		}
		return list;
	}
	public String getRetrieveImageThumbLink(String imageId){
		return request.getContextPath() + "/getfsvc/getelogimagethumb/"+imageId;
	}
	public String getRetrieveImageLink(String imageId){
		return request.getContextPath() + "/getfsvc/getelogimage/"+imageId;
	}
	
	public Asset getAttachFileIcon(AttachedFile attFile){
		return pageResourceHome.getIcon(attFile.getContentType(), attFile.getFileName());
	}
	
	public List<User> getApprovers(Project project){
		List<User> approvers = new ArrayList<User>();
		
		for(ProjUser pu : project.getMembers()){
			if(pu.hasPrivilege(PrivilegeElog.APPROVE_ELOG))
				approvers.add(pu.getUser());
		}
		return approvers;
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
	public boolean canEditElog(Elog e){
		Project proj = e.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(e.getAuthor().equals(getCurUser()) 
				&& (e.getStatus().equals(ElogStatus.DRAFT) 
						|| e.getStatus().equals(ElogStatus.REJECTED))
					)
				return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.EDIT_ELOG))
			return true;
		
		
		return false;
	}
	public boolean canViewApproveList(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.VIEW_APPROVAL_LIST ))
			return true;
		return false;
	}
	public boolean canCreateElog(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.CREATE_ELOG ))
			return true;
		return false;
	}
	public boolean canViewElog(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.VIEW_ELOG ))
			return true;
		return false;
	}
	public boolean canDeleteElog(Elog e){
		Project proj = e.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(e.getStatus().equals(ElogStatus.DRAFT) && e.getAuthor().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.DELETE_ELOG))
			return true;
		
		return false;
	}
	public boolean canApproveElog(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeElog.APPROVE_ELOG))
			return true;
		
		return false;
	}
	public boolean canAddComment(Elog e) {
		Project proj = e.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference() )
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeElog.ADD_COMMENT ))
			||	getCurUser().equals(e.getAuthor()) ){
			return true;       
		}
		return false;
	}
	public boolean canDeleteComment(ElogComment c) {
		Project proj = c.getElog().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference() )
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeElog.DELETE_COMMENT ))
			||	getCurUser().equals(c.getAuthor()) ){
			return true;
		}
		return false;
	}
}
