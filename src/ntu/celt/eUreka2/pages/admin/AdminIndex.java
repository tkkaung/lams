package ntu.celt.eUreka2.pages.admin;

import java.util.Date;

import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.PrivilegeHelper;
import ntu.celt.eUreka2.pages.admin.project.BatchCreateProject;
import ntu.celt.eUreka2.pages.admin.report.SchoolAssessments;
import ntu.celt.eUreka2.pages.admin.report.SchoolEvaluations;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

public class AdminIndex {

	@Inject
    private WebSessionDAO webSessionDAO;
    @SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
    private String ssid;
    @Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    private String buildNumber ; 
    
	@SuppressWarnings("unused")
	@Property
	private PrivilegeSystem privSys = new PrivilegeSystem();
	@SuppressWarnings("unused")
	@Property
	private PrivilegeHelper privHelper = new PrivilegeHelper();

	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	@Property
	private String strYear;
	@SuppressWarnings("deprecation")
	void setupRender(){
		strYear = Integer.toString((new Date()).getYear() + 1900);
	}
	
	public boolean showSystemTools(){
		if(//wsData.getUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJECT) 
			//|| wsData.getUser().hasPrivilege(PrivilegeSystem.MANAGE_USER) 
			 getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_EMAIL_TEMPLATE) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_ROLE)
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_TYPE))
			return true;
		return false;
	}
	public boolean showAnnouncements(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.SYSTEM_ANNOUNCEMENT) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.PROJ_SCHOOL_ANNOUNCEMENT) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.PROJ_TYPE_ANNOUNCEMENT))
			return true;
		return false;
	}
	
	public boolean canManageRubric(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_RUBRIC) 
				|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SCHOOL_RUBRIC))
			return true;
		return false;
	}
	@InjectPage
	private SchoolAssessments schoolAssessmentsPage; 
	public boolean canViewAssessmentReport(){
		return schoolAssessmentsPage.canViewAssessmentReport();
	}
	@InjectPage
	private SchoolEvaluations schoolEvaluationsPage; 
	public boolean canViewEvaluationReport(){
		return schoolEvaluationsPage.canViewEvaluationReport();
	}
	@InjectPage
	private BatchCreateProject batchCreateProjectPage; 
	public boolean canBatchCreateProject(){
		return batchCreateProjectPage.canBatchCreateProject();
	}
	
	public String getSpace(){
		return "&nbsp;";
	}
	
	public String getBuildNumber(){
    	return buildNumber;
    }
}
