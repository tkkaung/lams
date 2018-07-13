package ntu.celt.eUreka2.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.PrivilegeAssessment;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageType;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stylesheet="projModuleTabs/ProjModuleTabs.css")
public class ProjModuleTabs
{
	@Property
	private Module module;
	
	@Parameter(required = true)
	private Project curProj ;
	@Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
	private String curModule;
	
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@SessionState
	private AppState appState;
	@Inject
    private ComponentResources resources;
	@Inject
	private Messages messages;
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private SysRoleDAO sysRoleDAO;
	
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
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
	
	
	void setupRender(){
		ProjUser projU = curProj.getMember(getCurUser());
		if(!(projU!=null && projU.hasPrivilege(PrivilegeProject.READ_PROJECT)
				|| canSpecialAccess() || canAdminAccess(curProj))){
		
			String leaderEmails = "";
			for(ProjUser pu : curProj.getMembers()){
				if(pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER)){
					leaderEmails += ", " + pu.getUser().getEmail();
				}
			}
			if(!leaderEmails.equals(""))
				leaderEmails = leaderEmails.substring(2);
			
			throw new NotAuthorizedAccessException(messages.format("not-member-request-enroll-member-to-x", leaderEmails));
			
		}
		if(!canSpecialAccess() && !canAdminAccess(curProj)){
			updateLastAccess(curProj);
		}
	}
	
	
	
	@Cached
	private boolean canSpecialAccess(){
		if(curProj.isReference())
			return true;	
		
		return false;
	}
	
	
	public List<Module> getModulesToDisplay(){
		Project proj = curProj;
		ProjUser pu = proj.getMember(getCurUser());
		List<Module> mList = new ArrayList<Module>();
		for(ProjModule pm : proj.getProjmodules()){
			if(canHaveModule(pm.getModule(), pu, proj)){
				mList.add(pm.getModule());
			}
		}
		
		return mList;
	}
	
	public boolean canHaveModule(Module m, ProjUser pu, Project proj){
		if(canAdminAccess(curProj))
			return true;
		if(canSpecialAccess())
			return true;
		
		if(pu==null)
			return false;
		if(m.getName().equals(PredefinedNames.MODULE_ASSESSMENT)){
			if(!pu.hasPrivilege(PrivilegeAssessment.ACCESS_MODULE))
				return false;
		
			if(pu.hasPrivilege(PrivilegeAssessment.IS_ASSESSEE)){
				if(assmtDAO.getVisibleAssessments(proj).isEmpty()){ 
					return false;
				}
			}
		}
		
		return true;
	}
	
	@CommitAfter
	public void updateLastAccess(Project p){
		p.setLastAccess(new Date());
		if(!appState.hasVisitedProj(p.getId())){
			p.setNumVisit(p.getNumVisit()+1);
			appState.addVisitedProj(p.getId());
		}
		projDAO.updateProject(p);
	}
	
	public String getProjId(){
		return curProj.getId();
	}
	
	public String getClassForTabName(){
		if(module == null )
			return null;
		if(module.getName().equalsIgnoreCase(curModule) 
				|| resources.getPageName().equalsIgnoreCase(module.getRooturl()) 
				|| resources.getPageName().equalsIgnoreCase(module.getRooturl()+"/index"))
			return "selected";
		else
			return null;
	}
	public String getClassForTabName2(){
		if(curModule.equalsIgnoreCase(PredefinedNames.PROJECT_INFO))
			return "selected";
		else
			return null;
	}
	
	public String getNavClass(String modName){
		if(modName.equals(PredefinedNames.MODULE_ANNOUNCEMENT))
			return "nav2";
		if(modName.equals(PredefinedNames.MODULE_SCHEDULING))
			return "nav3";
		if(modName.equals(PredefinedNames.MODULE_RESOURCE))
			return "nav4";
		if(modName.equals(PredefinedNames.MODULE_FORUM))
			return "nav5";
		if(modName.equals(PredefinedNames.MODULE_BLOG))
			return "nav6";
		if(modName.equals(PredefinedNames.MODULE_BUDGET))
			return "nav7";
		if(modName.equals(PredefinedNames.MODULE_MESSAGE))
			return "nav8";
		if(modName.equals(PredefinedNames.MODULE_ASSESSMENT))
			return "nav9";
		if(modName.equals(PredefinedNames.MODULE_LEARNING_LOG))
			return "nav10";
		if(modName.equals(PredefinedNames.MODULE_ELOG))
			return "nav11";
		if(modName.equals(PredefinedNames.MODULE_USAGE))
			return "nav12";
		if(modName.equals(PredefinedNames.MODULE_PEER_EVALUATION))
			return "nav13";
		
		return modName;
	}
	
	public String getSpace(){
    	return "&nbsp;";
    }
	
	/*
	@Inject
	@Path("projModuleTabs/images/btninf.png")
	private  Asset info_png;
	@Inject
	@Path("projModuleTabs/images/btnann.png")
	private  Asset annmt_png;
	@Inject
	@Path("projModuleTabs/images/btnsch.png")
	private  Asset schdl_png;
	@Inject
	@Path("projModuleTabs/images/btnres.png")
	private  Asset resrc_png;
	@Inject
	@Path("projModuleTabs/images/btnfor.png")
	private  Asset forum_png;
	@Inject
	@Path("projModuleTabs/images/btnblo.png")
	private  Asset blog_png;
	@Inject
	@Path("projModuleTabs/images/btnbud.png")
	private  Asset budgt_png;
	@Inject
	@Path("projModuleTabs/images/btnmes.png")
	private  Asset msg_png;
	@Inject
	@Path("projModuleTabs/images/btnass.png")
	private  Asset assmt_png;
	@Inject
	@Path("projModuleTabs/images/btntes.png")
	private  Asset llog_png;
	@Inject
	@Path("projModuleTabs/images/btnnot.png")
	private  Asset elog_png;
	*/
	
	@Inject
	private MessageDAO msgDAO;
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private SchlTypeAnnouncementDAO schlTypeAnnmtDAO;
	public boolean hasNotification(String modName){
		if(modName.equals(PredefinedNames.MODULE_ANNOUNCEMENT)){
			if(annmtDAO.getActiveAnnouncements(curProj, getCurUser()).size()>0
				|| schlTypeAnnmtDAO.getActiveSchlTypeAnnouncements(curProj, getCurUser()).size()>0
				)
				return true;
		}
		else if(modName.equals(PredefinedNames.MODULE_MESSAGE)){
			if(msgDAO.getCountUnreadMessage(getCurUser(), curProj, MessageType.INBOX)>0)
				return true;
		}
		
		return false;
	}
	public long getNotificationCount(String modName){
		if(modName.equals(PredefinedNames.MODULE_ANNOUNCEMENT)){
			return annmtDAO.getActiveAnnouncements(curProj, getCurUser()).size()
				+ schlTypeAnnmtDAO.getActiveSchlTypeAnnouncements(curProj, getCurUser()).size();
		}
		else if(modName.equals(PredefinedNames.MODULE_MESSAGE)){
			return msgDAO.getCountUnreadMessage(getCurUser(), curProj, MessageType.INBOX);
		}
		
		return 0;
	}
	public String getModuleDisplayName(){
		if(PredefinedNames.PROJTYPE_SENATE.equalsIgnoreCase(curProj.getType().getName())){
			if(PredefinedNames.MODULE_FORUM.equalsIgnoreCase(module.getName()))
				return messages.get("senate-module-forum-name");
			if(PredefinedNames.MODULE_RESOURCE.equalsIgnoreCase(module.getName()))
				return messages.get("senate-module-resources-name");
		}
		
		return module.getDisplayName();
	}
}
