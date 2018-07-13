package ntu.celt.eUreka2.pages;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.GenericModuleDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.EvenOdd;
import ntu.celt.eUreka2.data.LastUpdateItem;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ProjTypeSetting;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.Privilege;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

@Import(library="Index.js", stylesheet="Index.css")
public class Index {
	
	@Property
	private Project project;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	@Property
	private GridDataSource projects;
	@SuppressWarnings("unused")
	@Property
	private ProjRole projRole;
	@SuppressWarnings("unused")
	@Property
	private int roleIndex;
	@SuppressWarnings("unused")
	@Property
	private int userIndex;
	
	@Property
	private List<Module> tempMods = new ArrayList<Module>();
	@SuppressWarnings("unused")
	@Property
	private Module tempMod;
	@SuppressWarnings("unused")
	@Property
	private EvenOdd evenOdd = new EvenOdd();
	
	@Inject
    private WebSessionDAO webSessionDAO;
    
	@SessionState
	private AppState appState;
	
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
	@Inject
	private Request request;
	@Inject
	private Messages messages;
	
	@Inject
	private ProjectDAO projDAO;
	@Inject
	private ProjRoleDAO projRoleDAO;
	@Inject
	private SchlTypeAnnouncementDAO schlTypeAnnmtDAO;
	
	@Inject
	private AnnouncementDAO annmtDAO;
	@Inject
	private AssessmentDAO assmtDAO;
	@Inject
	private BlogDAO blogDAO;
	@Inject
	private BudgetDAO budgetDAO;
	@Inject
	private ForumDAO forumDAO;
	@Inject
	private ElogDAO elogDAO;
	@Inject
	private LearningLogDAO llogDAO;
	@Inject
	private MessageDAO msgDAO;
	@Inject
	private ResourceDAO resourceDAO;
	@Inject
	private SchedulingDAO schdlDAO;
	@Inject
	private EvaluationDAO evalDAO;
	
	private GenericModuleDAO[] genericModuleDAO = {annmtDAO, assmtDAO, blogDAO, budgetDAO
			, forumDAO, elogDAO, llogDAO, msgDAO, resourceDAO, schdlDAO, evalDAO};
	
	@InjectComponent
	private Grid grid;
	private int firstResult;
	private int maxResult ;

	
	private final int NUM_PAST_DAY = Config.getInt("NUM_PAST_DAY");
	private final int NUM_USER_DISPLAY = Config.getInt("NUM_USER_DISPLAY");
	
	
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	public String truncateString(String str){
		return Util.truncateString(str, 300);
	}
	
	void setupRender(){
		maxResult = appState.getRowsPerPage();
		firstResult = (grid.getCurrentPage()-1)*grid.getRowsPerPage();
		
	
		if (grid.getSortModel().getSortConstraints().isEmpty()) {
			grid.getSortModel().updateSort("name");
			grid.getSortModel().updateSort("sdate");
			grid.getSortModel().updateSort("sdate"); //do twice to DESC sort
		}
		
		projects = projDAO.getProjectsForHomeByMemberAsDataSource(getCurUser(),
				firstResult, maxResult,
				grid.getSortModel().getSortConstraints());	
		
		
	}
	
	@Inject
	private BeanModelSource _source;
	@Inject
	private Messages _message;
	public BeanModel<Project> getModel() {
		BeanModel<Project> model = _source.createEditModel(Project.class, _message);
		//model.exclude("name","description","sdate","edate","courseId","shared","remarks");
		model.include("name", "sdate");
		
		model.add("No", null);
		model.get("name").label(_message.get("name-and-desc"));
		model.get("sdate").label(_message.get("date-label"));
		//model.add("date", propertyConduitSource.create(Project.class, "sdate"));
		model.add("type", propertyConduitSource.create(Project.class, "type.displayName"));
		model.add("members", null).label(_message.get("members"));
//		model.add("past7day", null).label(msg.get("past7day"));
		
		model.reorder("No",  "name");
		return model;
	}
	
	@Cached
	public int getTotalSize() {
		return (int) projects.getAvailableRows();
	}
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	public String getSpace(){
		return "&nbsp;";
	}
	public Object[] getSrchProjParams(){
		return new Object[]{null, "PROJECTS", true};
	}
	
	public boolean canCreateProject(){
		return getCurUser().hasPrivilege(PrivilegeSystem.CREATE_PROJECT);			
	}
	
	
	public String getContextPath(){
		return request.getContextPath();
	}
	
	public List<Module> getModulesToDisplay(Project proj){
		ProjUser pu = proj.getMember(getCurUser());
		List<Module> mList = new ArrayList<Module>();
		for(ProjModule pm : proj.getProjmodules()){
			if(canHaveModule(pm.getModule(), pu, proj)){
				mList.add(pm.getModule());
				
				if(!tempMods.contains(pm.getModule())){
					tempMods.add(pm.getModule()); //save to display Legend
				}
			}
		}
		
		return mList;
	}
	private boolean canHaveModule(Module m, ProjUser pu, Project proj){
		if(proj.isReference())
			return true;
		if(pu==null)
			return false;
		if(m.getName().equals(PredefinedNames.MODULE_ASSESSMENT)){
			/*if(!pu.hasPrivilege(PrivilegeAssessment.ACCESS_MODULE))
				return false;
		
			if(pu.hasPrivilege(PrivilegeAssessment.IS_ASSESSEE)){
				if(assmtDAO.getAssessments(proj, true).isEmpty()){ //no visible assessment
					return false;
				}
			}
			*/
			return false;
		}
		if(m.getName().equals(PredefinedNames.MODULE_USAGE)){
			return false;
		}
/*		if(m.getName().equals(PredefinedNames.MODULE_PEER_EVALUATION)){
			return false;
		}
*/		
		return true;
	}
	@Inject
	private Logger logger;
	
	@Property
	private List<LastUpdateItem> lastUpdateList;
	@SuppressWarnings("unused")
	@Property
	private LastUpdateItem tempLastUpdate;
	public List<LastUpdateItem> loadModuleLastUpdate(Module mod){
		for(GenericModuleDAO modDAO : genericModuleDAO){
			if(modDAO.getModuleName().equals(mod.getName())){
				lastUpdateList = modDAO.getLastUpdates(project, getCurUser(), NUM_PAST_DAY);
				break;
			}
		}
		
		return lastUpdateList;
	}
	public List<LastUpdateItem> getSchlTypeAnnmtLastUpdate(){
		lastUpdateList = schlTypeAnnmtDAO.getLastUpdates(project, getCurUser(), NUM_PAST_DAY);
		return lastUpdateList;
	}
	public boolean hasSettingHideMemberList(Project project){
		if(project.getType().getSettings().contains(ProjTypeSetting.HIDE_MEMBER_LIST))
			return true;
		return false;
	}
	public List<ProjRole> getProjRoles(){
		List<ProjRole> projRoles = projRoleDAO.getAllRole();
		projRoles = filterProjRoles(projRoles);
		return projRoles;
	}
	private List<ProjRole> filterProjRoles(List<ProjRole> projRoles){
		if(hasSettingHideMemberList(project)){
			for(int i= projRoles.size()-1; i>=0; i--){
				ProjRole prole = projRoles.get(i);
				if(!prole.getPrivileges().contains(new Privilege(PrivilegeProject.IS_LEADER))){
					projRoles.remove(i);
				}
			}
		}
		return projRoles;
	}
	
	public boolean isLast(int index, List<Object> list){
		if(index == list.size()-1)
			return true;
		return false;
	}
	private List<User> membersByRole;
	public List<User> getMembersByRole(Project proj, ProjRole pRole){
		membersByRole = proj.getMembersByRole(pRole);
		return membersByRole;
	}
	public List<User> getMembersByRole(){
		return membersByRole;
	}
	
	private final String overListUserClass = "oUser";
	public String getOverListUserClass(int userIndex){
		if(userIndex >= NUM_USER_DISPLAY)
			return overListUserClass;
		return "";
	}
	public boolean hasOverListUser(int lastUserIndex){
		if(lastUserIndex >= NUM_USER_DISPLAY)
			return true;
		return false;
	}
	public String getNumMoreText(List<Object> list){
		int num = list.size()- NUM_USER_DISPLAY;
		return messages.format("x-more", num);
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
}
