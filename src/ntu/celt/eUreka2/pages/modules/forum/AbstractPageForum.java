package ntu.celt.eUreka2.pages.modules.forum;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Path;
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
import ntu.celt.eUreka2.modules.elog.PrivilegeElog;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.PrivilegeForum;
import ntu.celt.eUreka2.modules.forum.Thread;
import ntu.celt.eUreka2.modules.forum.ThreadReply;
import ntu.celt.eUreka2.modules.forum.ThreadType;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.services.attachFiles.AttachedFile;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileNotFoundException;

public abstract class AbstractPageForum {
	public String getModuleName(){
		return PredefinedNames.MODULE_FORUM;
	}
	public String getModuleDisplayName(Project curProj){
		if(PredefinedNames.PROJTYPE_SENATE.equalsIgnoreCase(curProj.getType().getName())){
			return messages.get("senate-module-forum-name");
		}
		return getModule().getDisplayName();
	}
	@Inject
	private ModuleDAO moduleDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private Logger logger;
	@Inject
	private AttachedFileManager attFileManager;
	@Inject
	private Messages messages;
	@Inject
	private Request request;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public Module getModule(){
		return moduleDAO.getModuleByName(PredefinedNames.MODULE_FORUM);
	}
	public String encode(String str){
		return Util.encode(str);
	}
	public String truncateStr(String str){
		return Util.truncateString(str, 50);
	}
	public Object[] getParams(Object o1, Object o2){
		return new Object[] {o1, o2};
	}
	public Object[] getParams(Object o1, Object o2, Object o3){
		return new Object[]{o1, o2, o3};
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	
	@Inject
	@Path("context:lib/img/frm_type/DISCUSS_TOPIC.png")
	private Asset thtype0_png;
	@Inject
	@Path("context:lib/img/frm_type/ASK_A_QUESTION.png")
	private Asset thtype1_png;
	@Inject
	@Path("context:lib/img/frm_type/SUGGEST_IMPROVMENTS.png")
	private Asset thtype2_png;
	@Inject
	@Path("context:lib/img/frm_type/DEVELOPING_SOLUTIONS.png")
	private Asset thtype3_png;
	@Inject
	@Path("context:lib/img/frm_type/IDENTITFY_PROBLEMS.png")
	private Asset thtype4_png;
	@Inject
	@Path("context:lib/img/frm_type/ADD_OPINION_OR_FEEDBACK.png")
	private Asset thtype5_png;
	public Asset getThreadTypeIcon(ThreadType type){
		switch(type){
			case DISCUSS_TOPIC:
				return thtype0_png;
			case ASK_A_QUESTION:
				return thtype1_png;
			case SUGGEST_IMPROVMENTS:
				return thtype2_png;
			case DEVELOPING_SOLUTIONS:
				return thtype3_png;
			case IDENTITFY_PROBLEMS:
				return thtype4_png;
			case ADD_OPINION_OR_FEEDBACK:
				return thtype5_png;
		}
		return null;
	}
	public String getThreadTypeIconTip(ThreadType type){
		switch(type){
		case DISCUSS_TOPIC:
			return messages.get("thtype0-label");
		case ASK_A_QUESTION:
			return messages.get("thtype1-label");
		case SUGGEST_IMPROVMENTS:
			return messages.get("thtype2-label");
		case DEVELOPING_SOLUTIONS:
			return messages.get("thtype3-label");
		case IDENTITFY_PROBLEMS:
			return messages.get("thtype4-label");
		case ADD_OPINION_OR_FEEDBACK:
			return messages.get("thtype5-label");
		}
		return null;
	}
	
	
	public StreamResponse onRetrieveFile(String fId) {
		AttachedFile att = forumDAO.getAttachedFileById(fId);
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
		return request.getContextPath() + "/getfsvc/getforumimagethumb/"+imageId;
	}
	public String getRetrieveImageLink(String imageId){
		return request.getContextPath() + "/getfsvc/getforumimage/"+imageId;
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
	public boolean canCreateForum(Project proj){
		if(canAdminAccess(proj))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeForum.CREATE_FORUM ))
			return true;
		return false;
	}
	public boolean canViewForum(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeForum.VIEW_FORUM ))
			return true;
		return false;
	}
	public boolean canEditForum(Forum forum){
		Project proj = forum.getProject();
		
		if(canAdminAccess(proj))
			return true;
		if(getCurUser().equals(forum.getCreator()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeForum.EDIT_FORUM ))
			return true;
		return false;
	}
	public boolean canDeleteForum(Forum forum){
		Project proj = forum.getProject();
		
		if(canAdminAccess(proj))
			return true;
		if(getCurUser().equals(forum.getCreator()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeForum.DELETE_FORUM ))
			return true;
		return false;
	}
	public boolean canViewThread(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeForum.VIEW_THREAD ))
			return true;
		return false;
	}
	public boolean canCreateThread(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.CREATE_THREAD )){
			return true;
		}
		return false;
	}
	public boolean canDeleteThread(Thread th){
		Project proj = th.getForum().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(th.getAuthor().equals(getCurUser()) && th.getReplies().size()==0)
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.DELETE_THREAD )){
			return true;
		}
		return false;
	}
	
	public boolean canDeleteForumThread(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.DELETE_THREAD )){
			return true;
		}
		return false;
	}
	public boolean canEditThread(Thread th){
		Project proj = th.getForum().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(th.getAuthor().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.EDIT_THREAD )){
			return true;
		}
		return false;
	}
	public boolean canEditThreadReply(ThreadReply thR){
		Project proj = thR.getThread().getForum().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(thR.getAuthor().equals(getCurUser()))
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.EDIT_REPLY_THREAD )){
			return true;
		}
		return false;
	}
	public boolean canDeleteThreadReply(ThreadReply thR){
		Project proj = thR.getThread().getForum().getProject();
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		if(thR.getAuthor().equals(getCurUser()) && thR.getChildren().size()==0)
			return true;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.DELETE_REPLY_THREAD )){
			return true;
		}
		return false;
	}
	public boolean canCreateThreadReply(Project proj){
		if(canAdminAccess(proj))
			return true;
		if(proj.isReference())
			return false;
		ProjUser pu = proj.getMember(getCurUser());
		if(pu !=null && pu.hasPrivilege(PrivilegeForum.REPLY_THREAD)){
			return true;
		}
		return false;
	}
	public boolean canDeleteRef(LogEntry reflection){
		if(reflection.getCreator().equals(getCurUser()))
			return true;
		return false;
	}
	public boolean canEditRef(LogEntry reflection){
		if(reflection.getCreator().equals(getCurUser())
			&& hasLearningLogMod(reflection.getProject())
		)
			return true;
		return false;
	}
	public boolean hasLearningLogMod(Project proj){
		return proj.hasModule(PredefinedNames.MODULE_LEARNING_LOG);
	}
}
