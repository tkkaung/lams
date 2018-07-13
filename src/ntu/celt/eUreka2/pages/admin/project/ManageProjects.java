package ntu.celt.eUreka2.pages.admin.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.PrivilegeProject;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjUser;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class ManageProjects  extends AbstractPageAdminProject{
	@SuppressWarnings("unused")
	@Property
	private Project _proj;
	@Property
	private List<Project> projs;
	@SuppressWarnings("unused")
	@Property
	private User tempUser;
	@SuppressWarnings("unused")
	@Property
	private ProjModule tempPModule;
	@SuppressWarnings("unused")
	@Property
	private List<ProjRole> projRoles;
	@SuppressWarnings("unused")
	@Property
	private ProjRole projRole;
	@SuppressWarnings("unused")
	@Property
	private int userIndex;
	private final int NUM_USER_DISPLAY = Config.getInt("NUM_USER_DISPLAY");
	
	@SessionState
	private AppState appState;
	
	@SuppressWarnings("unused")
	@Property
	private ProjectAttachedFile tempAttFile;
	
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private ProjStatus filterStatus;
	
	private enum SubmitType { DELETE, ARCHIVE, ACTIVE };
	private SubmitType submitType;
	
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private ProjStatusDAO _projStatusDAO;
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private BeanModelSource source;
	@Inject
    private PropertyConduitSource propertyConduitSource; 
	@Inject
	private Messages messages;
	@Inject
	private Request _rqust;
	
	@InjectComponent
	private Grid grid;

	void setupRender(){
		projs  = _projDAO.getProjectsByMember(getCurUser());
		projs = filterOutProjects(projs);
		
		projRoles = _projRoleDAO.getAllRole();
	}
	
	private List<Project> filterOutProjects(List<Project> projs){
		// only projs that user is in-charge, contain filterText, are filterStatus
		for(int i=projs.size()-1; i>=0; i--){
			Project proj = projs.get(i);
			if( (filterText!=null && !(proj.getName().toLowerCase().contains(filterText.toLowerCase()) 
					|| (proj.getDescription()!=null && proj.getDescription().toLowerCase().contains(filterText.toLowerCase()))
					|| proj.getId().toLowerCase().contains(filterText.toLowerCase())
					|| proj.hasMember(filterText)
					)
				)
				|| (filterStatus!=null && !(proj.getStatus().equals(filterStatus)))
				|| (!canManage(proj))
			){
				projs.remove(i);
			}
		}
		return projs;
	}
	
	
	private boolean canManage(Project proj){
		ProjUser pu = proj.getMember(getCurUser());
		if(pu==null)
			return false;
		if(pu.hasPrivilege(PrivilegeProject.ASSIGN_MODULE)
			|| pu.hasPrivilege(PrivilegeProject.CHANGE_STATUS)
			|| pu.hasPrivilege(PrivilegeProject.ENROLL_MEMBER)
			|| pu.hasPrivilege(PrivilegeProject.UPDATE_PROJECT)
			|| pu.hasPrivilege(PrivilegeProject.IS_LEADER)
			){
			return true;
		}
		return false;
	}
	
	
	
	
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	
	public BeanModel<Project> getModel() {
		BeanModel<Project> model = source.createEditModel(Project.class, messages);
		model.include("name","sdate","mdate");
		
		model.add("No", null);
		model.add("chkBox", null);
		model.get("name").label(messages.get("name_desc-label"));
		model.get("sdate").label(messages.get("date-label"));
		model.add("School", propertyConduitSource.create(Project.class, "school.displayname"));
		model.add("type", propertyConduitSource.create(Project.class, "type.displayname"));
		model.add("status", propertyConduitSource.create(Project.class, "status.displayName"));
		model.add("members", null).label(messages.get("members"));
		model.add("modules", null).label(messages.get("modules"));
		model.add("lastAccess", propertyConduitSource.create(Project.class, "lastAccess"));
		
	 	
		model.reorder("No","chkBox", "name", "sdate", "status", "type","school");
		return model;
	}
	
	public SelectModel getProjStatusModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjStatus> projStatusList = _projStatusDAO.getAllStatus();
		for (ProjStatus ps : projStatusList) {
			OptionModel optModel = new OptionModelImpl(ps.getDisplayName(), ps);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	
	public int getTotalSize() {
		if (projs == null)
			return 0;
		return projs.size();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	void onSelectedFromDelete(){
		submitType = SubmitType.DELETE;
	}
	void onSelectedFromArchive(){
		submitType = SubmitType.ARCHIVE;
	}
	void onSelectedFromActive(){
		submitType = SubmitType.ACTIVE;
	}
	
	@Component(id = "projForm")
	private Form projForm;
	public void onValidateFormFromProjForm(){
		if( SubmitType.DELETE.equals(submitType)){
			String[] selModId = _rqust.getParameters("gridChkBox");
			if(selModId==null || selModId.length==0){
				projForm.recordError(messages.get("select-at-least-one-item"));
			}
		}
	}
	@InjectPage
	private ProjectDelete projectDelete;
	@CommitAfter
	public Object onSuccessFromProjForm(){
		String[] selProjId = _rqust.getParameters("gridChkBox");
		switch(submitType){
			case DELETE:
				projectDelete.setContext(selProjId);
				return projectDelete;
			case ARCHIVE:
				int count = 0;
				ProjStatus pArchive = _projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ARCHIVED);
				if(selProjId!=null){
					for(String id : selProjId){
						_proj = _projDAO.getProjectById(id);
						if(_proj != null){
							_proj.setLastStatusChange(new Date());
							_proj.setStatus(pArchive);
							_projDAO.saveProject(_proj);
							count++;
						}
					}
				}
				if(count<=1){
					appState.recordInfoMsg(count + " project has been set to " + PredefinedNames.PROJSTATUS_ARCHIVED);
				}
				else{
					appState.recordInfoMsg(count + " projects have been set to " + PredefinedNames.PROJSTATUS_ARCHIVED);
				}
				break;
			case ACTIVE:
				count = 0;
				ProjStatus pActive = _projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ACTIVE);
				if(selProjId!=null){
					for(String id : selProjId){
						_proj = _projDAO.getProjectById(id);
						if(_proj != null){
							_proj.setLastStatusChange(new Date());
							_proj.setStatus(pActive);
							_projDAO.saveProject(_proj);
							count++;
						}
					}
				}
				if(count<=1){
					appState.recordInfoMsg(count + " project has been set to " + PredefinedNames.PROJSTATUS_ACTIVE);
				}
				else{
					appState.recordInfoMsg(count + " projects have been set to " + PredefinedNames.PROJSTATUS_ACTIVE);
				}
				break;

		}
		return null;
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
}
