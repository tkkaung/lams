package ntu.celt.eUreka2.pages.admin.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.data.ProjectSearchableField;
import ntu.celt.eUreka2.entities.ProjModule;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjStatus;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.ProjectAttachedFile;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.apache.tapestry5.services.Request;

public class ManageAdminProjects extends AbstractPageAdminProject {
	@SuppressWarnings("unused")
	@Property
	private Project _proj;
	@Property
	private GridDataSource _projs;
	@SuppressWarnings("unused")
	@Property
	private User _tempUser;
	@SuppressWarnings("unused")
	@Property
	private ProjModule _tempPModule;
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
	@SuppressWarnings("unused")
	@Property
	private ProjectAttachedFile tempAttFile;
	
	
	@SessionState
	private AppState appState;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private ProjectSearchableField filterIn;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String filterUsername;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private Date filterSDate;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private Date filterEDate;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private ProjStatus filterStatus;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private ProjType filterType;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private School filterSchool;

	private enum SubmitType { DELETE, ARCHIVE , ACTIVE};
	private SubmitType submitType;
	@Property
	private ProjStatus _projStatus;
	
	
	@Inject
	private ProjectDAO _projDAO;
	@Inject
	private ProjStatusDAO _projStatusDAO;
	@Inject
	private ProjTypeDAO _projTypeDAO;
	@Inject
	private SchoolDAO _schoolDAO;
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private Request _rqust;
	@Inject
	private BeanModelSource _source;
	@Inject
	private Messages _messages;
	@Inject
    private PropertyConduitSource _propertyConduitSource; 
	
	@InjectComponent
	private Grid grid;
	private int firstResult;
	private int maxResult ;

	
	void setupRender(){
		if(!canManageProjects()){
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		maxResult = appState.getRowsPerPage();
		firstResult = (grid.getCurrentPage()-1)*grid.getRowsPerPage();
		
		if(filterSchool==null && !canManageSystemData())
			filterSchool = getDefaultSchool();
			
		_projs = _projDAO.searchProjectsAsDataSource(filterText, filterIn, filterUsername,
				filterSDate, filterEDate, filterStatus, filterType,filterSchool
				,firstResult,maxResult, grid.getSortModel().getSortConstraints(), true);
		
		
		projRoles = _projRoleDAO.getAllRole();
		
		if(_projStatus==null)
			_projStatus = _projStatusDAO.getStatusByName(PredefinedNames.PROJSTATUS_ARCHIVED);

	}
	@Property
	private int rowIndex;
	public int getNo(){
		return (grid.getCurrentPage()-1)*grid.getRowsPerPage()+ rowIndex + 1;
	}
	@Cached
	public int getTotalSize() {
		return (int) _projs.getAvailableRows();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	public BeanModel<Project> getModel() {
		BeanModel<Project> model = _source.createEditModel(Project.class, _messages);
		model.include("name","sdate","remarks","mdate");
		
		model.add("No", null);
		model.add("chkBox", null);
		model.get("name").label(_messages.get("name_desc-label"));
		model.get("sdate").label(_messages.get("date-label"));
		model.add("school", _propertyConduitSource.create(Project.class, "school.displayname"));
		model.add("type", _propertyConduitSource.create(Project.class, "type.displayname"));
		model.add("status", _propertyConduitSource.create(Project.class, "status.displayName"));
		model.add("members", null).label(_messages.get("members"));
		model.add("modules", null).label(_messages.get("modules"));
		model.add("lastAccess", _propertyConduitSource.create(Project.class, "lastAccess"));
		
		model.reorder("No","chkBox", "name", "sdate", "status", "type", "school"
				,"members","modules","lastAccess","remarks","mdate");
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
	public SelectModel getProjTypeModel() {
		List<OptionModel> optionList = new ArrayList<OptionModel>();
		List<ProjType> typeList = _projTypeDAO.getAllTypes();
		for (ProjType type: typeList) {
			OptionModel option = new OptionModelImpl(type.getDisplayName(), type);
			optionList.add(option);
		}
		SelectModel selModel = new SelectModelImpl(null, optionList);
		return selModel;
	}
	
	@Component(id = "filterForm")
	private Form filterForm;
	@Component(id="filterEDate")
	private DateField filterEDateField;
	public void onValidateFormFromFilterForm(){
		if (filterEDate!=null && filterSDate!=null
			&&	filterEDate.before(filterSDate)) {
			filterForm.recordError(filterEDateField, _messages.get("endDate-must-be-after-startDate"));
		}
	}
	public void onPrepareForSubmitFromFilterForm(){
		//do nothing, just reload
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
				projForm.recordError(_messages.get("select-at-least-one-item"));
			}
		}
	}
	
	@InjectPage
	private ProjectAdminDelete projectAdminDelete;
	@CommitAfter
	public Object onSuccessFromProjForm(){
		String[] selProjId = _rqust.getParameters("gridChkBox");
		switch(submitType){
			case DELETE:
				projectAdminDelete.setContext(selProjId);
				return projectAdminDelete;
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
		return _messages.format("x-more", num);
	}
	
}
