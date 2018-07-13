package ntu.celt.eUreka2.pages.modules.blog;

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
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.blog.BlogComment;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogFile;
import ntu.celt.eUreka2.modules.blog.BlogStatus;
import ntu.celt.eUreka2.modules.blog.PrivilegeBlog;
import ntu.celt.eUreka2.pages.modules.resources.Home;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageBlog {
	public String getModuleName(){
		return PredefinedNames.MODULE_BLOG;
	}
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_BLOG);
	}
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private Messages messages;
	@Inject
	private Logger logger;
	@Inject
	private Request request;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private AttachedFileManager attFileManager;
	@InjectPage
	private Home pageResourceHome;
	
	
	
	public String getNull(){
		return null;
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[]{o1,o2};
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	public String encode(String str){
		return Util.encode(str);
	}
	
	public boolean isPublished(Blog b){
		if(b.getStatus().equals(BlogStatus.PUBLISHED))
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
	public boolean canViewBlog(Project proj){
		if(canAdminAccess(proj))
			return true;
		
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBlog.VIEW_BLOG )){
			return true;
		}
		
		return false;
	}
	public boolean canCreateBlog(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBlog.CREATE_BLOG )){
			return true;
		}
		
		return false;
	}
	public boolean canEditBlog(Blog b){
		Project proj = b.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(b.getAuthor().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeBlog.EDIT_BLOG )){
			return true;
		}
		
		return false;
	}

	public boolean canDeleteBlog(Blog b) {
		Project proj = b.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeBlog.DELETE_BLOG ))
			||	getCurUser().equals(b.getAuthor()) ){
			return true;       
		}
		return false;
	}
	
	public boolean canAddComment(Blog b) {
		Project proj = b.getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeBlog.ADD_COMMENT ))
			||	getCurUser().equals(b.getAuthor()) ){
			return true;       
		}
		return false;
	}
	public boolean canDeleteComment(BlogComment c) {
		Project proj = c.getBlog().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if((pu !=null && pu.hasPrivilege(PrivilegeBlog.DELETE_COMMENT ))
			||	getCurUser().equals(c.getAuthor()) ){
			return true;
		}
		return false;
	}
	
	
	public StreamResponse onRetrieveFile(String fId) {
		BlogFile att = blogDAO.getBlogFileById(fId);
		if(att==null)
			throw new RecordNotFoundException(messages.format("entity-not-exists", "AttachmentID", fId));
		try {
			return attFileManager.getAttachedFileAsStream(att);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound: path=" + att.getPath() +"\n"+e.getMessage());
			throw new AttachedFileNotFoundException(messages.format("attachment-x-not-found", att.getDisplayName()));
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
		return request.getContextPath() + "/getfsvc/getblogimagethumb/"+imageId;
	}
	public String getRetrieveImageLink(String imageId){
		return request.getContextPath() + "/getfsvc/getblogimage/"+imageId;
	}
	public Asset getAttachFileIcon(AttachedFile attFile){
		return pageResourceHome.getIcon(attFile.getContentType(), attFile.getFileName());
	}
	
	public boolean hasCommonTags(Project proj){
		return blogDAO.hasBlogTags(proj);
	}
	public List<String> getCommonTags(Project proj){
		return blogDAO.getBlogTags(proj, Integer.parseInt(messages.get("MAX_TAG_DISPLAY")));
	}
	public long getBlogsByTags(Project project, String tagName){
		return blogDAO.countBlogsByTags(project, tagName, getCurUser());
	}
}
