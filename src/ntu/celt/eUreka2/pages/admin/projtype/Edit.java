package ntu.celt.eUreka2.pages.admin.projtype;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.NotAuthorizedAccessException;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.RecordNotFoundException;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.data.ProjTypeSetting;
import ntu.celt.eUreka2.entities.Module;
import ntu.celt.eUreka2.entities.ProjRole;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.pages.admin.projtype.Manage;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

@Import(library="Edit.js")
public class Edit {
	@Property
	private ProjType _type;
	private int _id = 0;
	
	@SuppressWarnings("unused")
	@Property
	private Module _module;
	@SuppressWarnings("unused")
	@Property
	private List<Module> _modules;
	@SuppressWarnings("unused")
	@Property
	private ProjRole _pRole;
	@SuppressWarnings("unused")
	@Property
	private List<ProjRole> _pRoles;
	@SuppressWarnings("unused")
	@Property
	private ProjTypeSetting _pSetting;
	@SuppressWarnings("unused")
	@Property
	private int i;
	
	@Inject
	private ProjTypeDAO _typeDAO;
	@Inject
	private ModuleDAO _moduleDAO;
	@Inject
	private ProjRoleDAO _projRoleDAO;
	@Inject
	private Request _rqst;
	@Inject
	private Messages _messages;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	
	void onActivate(int id) {
		_id = id;
	}
	int onPassivate() {
		return _id;
	}
	public boolean isCreateMode(){
		if(_id == 0)
			return true;
		return false;
	}
	
	void setupRender(){
		if(!getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_TYPE)) {
			throw new NotAuthorizedAccessException(_messages.get("not-authorized-access"));
		}
		
		_modules = _moduleDAO.getAllModules();
		_pRoles = _projRoleDAO.getAllRole();
	}
	void onPrepareFromForm(){
		if(isCreateMode()){
			_type = new ProjType();
			_type.setModules(new ArrayList<Module>());
			for(String moduleName : Config.mandatoryModules){
				Module m = _moduleDAO.getModuleByName(moduleName);
				_type.addModule(m);
			}
		}
		else{
			_type = _typeDAO.getTypeById(_id);
			if(_type==null){
				throw new RecordNotFoundException(_messages.format("entity-not-exists", "TypeId", _id));
			}
		}
	}
	
	@Component
	private Form form;
	@Component(id="name")
	private TextField nameField;
	
	void onValidateFormFromForm() {
		if(isCreateMode() && _typeDAO.isTypeNameExist(_type.getName())){
			form.recordError(nameField, _messages.format("duplicate-key-exception", 
					_messages.get("name-label"), _type.getName()));
		}
	}
	
	@CommitAfter
	public Object onSuccessFromForm() {
		_type.setModules(new ArrayList<Module>());
		String[] sValue = _rqst.getParameters("objModule");
		if(sValue!=null){
			for (int i = 0; i < sValue.length; i++) {
				int moduleId = Integer.parseInt(sValue[i]);
				Module newModule = _moduleDAO.getModuleById(moduleId);
				_type.addModule(newModule);
			}
		}
		_type.setNonModules(new ArrayList<Module>());
		sValue = _rqst.getParameters("noModule");
		if(sValue!=null){
			for (int i = 0; i < sValue.length; i++) {
				int moduleId = Integer.parseInt(sValue[i]);
				Module newModule = _moduleDAO.getModuleById(moduleId);
				_type.addNonModule(newModule);
			}
		}
		_type.setDefaultModules(new ArrayList<Module>());
		sValue = _rqst.getParameters("defaultModule");
		if(sValue!=null){
			for (int i = 0; i < sValue.length; i++) {
				int moduleId = Integer.parseInt(sValue[i]);
				Module newModule = _moduleDAO.getModuleById(moduleId);
				_type.addDefaultModule(newModule);
			}
		}
		_type.setRoles(new ArrayList<ProjRole>());
		sValue = _rqst.getParameters("pRole");
		if(sValue!=null){
			for (int i = 0; i < sValue.length; i++) {
				int roleId = Integer.parseInt(sValue[i]);
				ProjRole role = _projRoleDAO.getRoleById(roleId);
				_type.addRole(role);
			}
		}
		_type.setSettings(new ArrayList<ProjTypeSetting>());
		sValue = _rqst.getParameters("pSetting");
		if(sValue!=null){
			for (int i = 0; i < sValue.length; i++) {
				_type.addSetting(ProjTypeSetting.valueOf(sValue[i]));
			}
		}
		
		_typeDAO.updateType(_type);
		
		return Manage.class;
	}
	
	public String getCheckHasDefaultModules(Module m){
		if(_type.getDefaultModules().contains(m))
			return "checked";
		return null;
	}
	public String getCheckHasModules(Module m){
		if(_type.getModules().contains(m))
			return "checked";
		return null;
	}
	public String getCheckHasNonModules(Module m){
		if(_type.getNonModules().contains(m))
			return "checked";
		return null;
	}
	public String getCheckHasRoles(ProjRole r){
		if(_type.getRoles().contains(r))
			return "checked";
		return null;
	}
	public String getCheckHasSettings(ProjTypeSetting s){
		if(_type.getSettings().contains(s))
			return "checked";
		return null;
	}
	
	public ProjTypeSetting[] getAllSettings(){
		return ProjTypeSetting.values();
	}
	public String getSettingDisplayName(ProjTypeSetting s){
		return _messages.get(s.name()+"-label");
	}
}
