package ntu.celt.eUreka2.pages.admin.user;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.ProjType;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.SysroleUser;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AbstractPageAdminUser {
	@Inject
	private SysRoleDAO sysRoleDAO;
	@Inject
	private SchoolDAO schoolDAO;
	@Inject
	private ProjTypeDAO projTypeDAO;
	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	public String getSpace(){
		return "&nbsp;";
	}
	
	public int getTypeAdminID(){
		SysRole r = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_PROJTYPE_ADMIN);
		if(r==null)
			return 0;
		return r.getId();
	}
	public int getSchoolAdminID(){
		SysRole r = sysRoleDAO.getSysRoleByName(PredefinedNames.SYSROLE_SCHOOL_ADMIN);
		if(r==null)
			return 0;
		return r.getId();
	}
	public String getExtraRoleParamDisplay(SysroleUser sRoleUser){
		String str = "";
		if(sRoleUser.getParam()==null)
			return str;
		if(sRoleUser.getSysRole().getName().equals(PredefinedNames.SYSROLE_PROJTYPE_ADMIN)){
			ProjType pt = projTypeDAO.getTypeById(sRoleUser.getParam());
			if(pt!=null)
				str = pt.getDisplayName();
		}
		else if(sRoleUser.getSysRole().getName().equals(PredefinedNames.SYSROLE_SCHOOL_ADMIN)){
			School schl = schoolDAO.getSchoolById(sRoleUser.getParam());
			if(schl!=null)
				str = schl.getDisplayName();
		}
		if(!str.isEmpty())
			return "("+str+")";
		
		return "";
	}
	public SelectModel getSchoolModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = schoolDAO.getAllSchools();
		for (School s : schoolList) {
			OptionModel optModel = new OptionModelImpl(s.getDisplayNameLong(), s);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getSchoolCompactModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<School> schoolList = schoolDAO.getAllSchools();
		for (School s : schoolList) {
			OptionModel optModel = new OptionModelImpl(s.getDisplayName(), s);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getProjTypeModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<ProjType> tList = projTypeDAO.getAllTypes();
		for (ProjType t : tList) {
			OptionModel optModel = new OptionModelImpl(t.getDisplayName(), t);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
	public SelectModel getSysRoleModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<SysRole> sysRoleList = sysRoleDAO.getAllSysRoles();
		for (SysRole s : sysRoleList) {
			if(!Util.isContains(Config.paramSysRoles, s.getName())){
				OptionModel optModel = new OptionModelImpl(s.getDisplayName(), s);
				optModelList.add(optModel);
			}
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	public SelectModel getAllSysRoleModel() {
		List<OptionModel> optModelList = new ArrayList<OptionModel>();
		List<SysRole> sysRoleList = sysRoleDAO.getAllSysRoles();
		for (SysRole s : sysRoleList) {
			OptionModel optModel = new OptionModelImpl(s.getDisplayName(), s);
			optModelList.add(optModel);
		}
		SelectModel selModel = new SelectModelImpl(null, optModelList);
		return selModel;
	}
	
}
