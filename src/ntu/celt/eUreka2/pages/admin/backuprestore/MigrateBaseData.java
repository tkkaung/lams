package ntu.celt.eUreka2.pages.admin.backuprestore;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.modules.backuprestore.MigrateDAO;
import ntu.celt.eUreka2.modules.backuprestore.ResultSummary;

public class MigrateBaseData {
	
	@Property
	private boolean sysRole;
	@Property
	private boolean projRole;
	@Property
	private boolean projStatus;
	@Property
	private boolean projType;
	@Property
	private boolean school;
	@Property
	private boolean user;
	@Property
	private String actionIfExist;
	
	@Inject
	private MigrateDAO migrateDAO;// = new MigrateDAO();
	
	
	@InjectPage
	private ResultPage resultPage;
	@Inject
	private Messages messages;
	
	
	void setupRender(){
		if(actionIfExist==null)
			actionIfExist = "IGNORE";
	}
	
	Object onSuccessFromSelectProjForm(){
		boolean replaceIfExist = false;
		if("REPLACE".equalsIgnoreCase(actionIfExist)){
			replaceIfExist = true;
		}
		List<ResultSummary> results = new ArrayList<ResultSummary>();
		
		if(sysRole){
			ResultSummary result = migrateDAO.importAllSysRoles(replaceIfExist);
			result.setMessage("SysRole");
			results.add(result);
		}
		if(projRole){
			ResultSummary result = migrateDAO.importAllProjRoles(replaceIfExist);
			result.setMessage("ProjRole");
			results.add(result);		
		}
		if(projStatus){
			ResultSummary result = migrateDAO.importAllProjStatus(replaceIfExist);
			result.setMessage("ProjStatus");
			results.add(result);
		}
		if(projType){
			ResultSummary result = migrateDAO.importAllProjTypes(replaceIfExist);
			result.setMessage("ProjType");
			results.add(result);
		}
		if(school){
			ResultSummary result = migrateDAO.importAllSchools(replaceIfExist);
			result.setMessage("School");
			results.add(result);
		}
		if(user){
			ResultSummary result = migrateDAO.importAllUsers(replaceIfExist);
			result.setMessage("User");
			results.add(result);
		}
	
		resultPage.setResults(results);
		resultPage.setCallBackPageName(messages.get("page-title"));
		resultPage.setCallBackPageLink("admin/backuprestore/migratebasedata"); //this page
		
		return resultPage;
	}
	
	
	public Long getTotalSysRole(){
		return migrateDAO.getCountSysRoles();
	}
	public Long getTotalProjRole(){
		return migrateDAO.getCountProjRoles();
	}
	public Long getTotalProjStatus(){
		return migrateDAO.getCountProjStatuss();
	}
	public Long getTotalProjType(){
		return migrateDAO.getCountProjTypes();
	}
	public Long getTotalSchool(){
		return migrateDAO.getCountSchools();
	}
	public Long getTotalUser(){
		return migrateDAO.getCountUsers();
	}
	
}
