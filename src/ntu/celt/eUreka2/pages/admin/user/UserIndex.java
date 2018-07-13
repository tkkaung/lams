package ntu.celt.eUreka2.pages.admin.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.data.FilterType;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.UserSearchableField;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.BeanModel;
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
import org.hibernate.exception.ConstraintViolationException;

public class UserIndex extends AbstractPageAdminUser {
	@Property
	private User user;
	@Property
	private GridDataSource users;
	@SessionState
	private AppState appState;
	@SuppressWarnings("unused")
	@Property
	private SysroleUser sRoleUser;
	
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchText;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private UserSearchableField searchIn;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private String searchEnabled;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private School searchSchool;
	@Property
	@Persist(PersistenceConstants.CLIENT)
	private SysRole searchSysRole;

	enum SubmitType {DELETE};
	private SubmitType submitType ;
	
	@Inject
	private UserDAO userDAO;
	@Inject
	private Request request;
	@Inject
    private BeanModelSource beanModelSource;
    @Inject
    private Messages messages;
    @Inject
    private PropertyConduitSource propertyConduitSource; 
    
    @InjectComponent
	private Grid grid;
	private int firstResult;
	private int maxResult ;

    
    
	void setupRender() {
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		maxResult = appState.getRowsPerPage();
		firstResult = (grid.getCurrentPage()-1)*grid.getRowsPerPage();
		
		users = userDAO.searchUsersAsDataSource(FilterType.CONTAIN, searchText, searchIn
				,(searchEnabled==null? null : Boolean.parseBoolean(searchEnabled))
				, searchSchool, searchSysRole
				, firstResult, maxResult, grid.getSortModel().getSortConstraints());
	//	users = filterUsers(users);
	}
	
/*	private List<User> filterUsers(List<User> users){
		if(searchSysRole!=null){
			for(int i = users.size()-1; i>=0; i--){
				User u = users.get(i);
				if(!u.hasSysRoleID(searchSysRole.getId())){
					users.remove(i);
				}
			}
		}
		
		return users;
	}
	*/
	
	public int getTotalSize() {
		return users.getAvailableRows();
	}
	public int getRowsPerPage(){
		return appState.getRowsPerPage();
	}
	
	
	void onSelectedFromDeleteBtn() {
		submitType = SubmitType.DELETE;
		
	}
	
	
	void onSuccessFromUserForm(){
		String[] selUserId = request.getParameters("gridChkBox");
		if(selUserId!=null){
			switch(submitType){
			case DELETE:
				for(String userId : selUserId){
					User u = userDAO.getUserById(Integer.parseInt(userId));
					if(getCurUser().equals(u)){
						appState.recordErrorMsg(messages.get("cant-delete-own-account"));
					}
					else{
						try{
							deleteUser(u);
							appState.recordInfoMsg(messages.format("successfully-delete-x", u.getUsername()));
						}
						catch(ConstraintViolationException e){
							appState.recordErrorMsg(messages.format("cant-delete-x-used-by-other", u.getUsername()));
						}
					}
					
				}
				break;
			}
		}
	}
	
	@CommitAfter
	void deleteUser(User user){
		userDAO.delete(user);
	}
	void onSuccessFromSearchForm(){
		//do nothing, only need to reload
	}
	
	@SuppressWarnings("unchecked")
	public BeanModel getModel() {
		BeanModel model = beanModelSource.createEditModel(User.class, messages);
		model.include("username","firstname","lastname","email","enabled","modifyDate");
		 model.add("chkBox",null);
        model.add("id", propertyConduitSource.create(User.class, "id")); 
        model.add("school", propertyConduitSource.create(User.class, "school.displayname")); 
        model.add("sysRole", propertyConduitSource.create(User.class, "sysRole.displayName")); 
        model.add("projectCount", null); 
        model.add("action",null);
        model.reorder("chkBox","id");
        return model;
    }
	
	public SelectModel getStatusModel(){
    	List<OptionModel> optModelList = new ArrayList<OptionModel>();
    	OptionModel optModel = new OptionModelImpl(messages.get("enabled"), "true"); 
    	optModelList.add(optModel);
    	optModel = new OptionModelImpl(messages.get("disabled"), "false"); 
    	optModelList.add(optModel);
    	
    	SelectModel selModel = new SelectModelImpl(null, optModelList);
    	return selModel;
    }
	
	@CommitAfter
	void onActionFromToggleEnabled(int id) {
		user = userDAO.getUserById(id);
		if(user==null){
			throw new RecordNotFoundException(messages.format("entity-not-exists", "UserID", id));
		}
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER)) {
			throw new NotAuthorizedAccessException(messages.get("not-authorized-access"));
		}
		
		user.setEnabled(!user.isEnabled());
		user.setModifyDate(new Date());
		userDAO.save(user);
	}
	
	
	
}
