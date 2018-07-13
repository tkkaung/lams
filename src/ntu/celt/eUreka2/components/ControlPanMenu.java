package ntu.celt.eUreka2.components;

import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.data.PrivilegeSystem;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.PrivilegeHelper;
import ntu.celt.eUreka2.pages.admin.report.SchoolAssessments;
import ntu.celt.eUreka2.pages.admin.report.SchoolEvaluations;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(library="dropdowntabs/dropdowntabfiles/dropdowntabs.js", 
		stylesheet="dropdowntabs/dropdowntabfiles/bluetabs.css")
public class ControlPanMenu
{
	@Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String initTab ;
	@Inject
    private ComponentResources resources;
	@SuppressWarnings("unused")
	@Property
	private PrivilegeSystem privSys = new PrivilegeSystem();
	@SuppressWarnings("unused")
	@Property
	private PrivilegeHelper privHelper = new PrivilegeHelper();

	@SessionAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME)
	private String ssid;
	@Inject
    private WebSessionDAO webSessionDAO;
	@SessionAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME)
    private String hideHeaderForIframe;

	@Cached
	public User getCurUser(){
		return webSessionDAO.getCurrentUser(ssid);
	}
	
	void setupRender(){

	}
	
	public boolean showSystemTools(){
		if(//getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJECT) 
		//	|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_USER) 
			 getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_EMAIL_TEMPLATE) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_DATA) 
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_SYSTEM_ROLE)
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_ROLE)
			|| getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJ_TYPE)
			)
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
	public boolean showStatistics(){
		if(getCurUser().hasPrivilege(PrivilegeSystem.MANAGE_PROJECT) 
				|| canViewEvaluationReport())
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
	
	
	public String getSpace(){
		return "&nbsp;";
	}
	
	public String getActivePageClass(String pageName){
		
		if(resources.getPageName().equalsIgnoreCase(pageName) || resources.getPageName().equalsIgnoreCase(pageName+"/index")){
			return "active";
		}
		return null;
	}
	
	public int getInitTabNum(){
		if(initTab==null)
			return -1;
		else if(initTab.equalsIgnoreCase("project"))
			return 0;
		else if(initTab.equalsIgnoreCase("user"))
			return 1;
		else if(initTab.equalsIgnoreCase("rubric"))
			return 2;
		else if(initTab.equalsIgnoreCase("systemtools"))
			return 3;
		else if(initTab.equalsIgnoreCase("announcement"))
			return 4;
		else if(initTab.equalsIgnoreCase("statistic"))
			return 5;
		
		return -1;
	}
	
	public int getHideHeaderIDForIframe(){
		if(hideHeaderForIframe == null)
			return 0;
		if(hideHeaderForIframe.equals("1"))
			return 1;
		if(hideHeaderForIframe.equals("2"))
			return 2;
		return 0;
	}
	public boolean isEqual(int a, int b){
		return (a == b) ;
	}
	public boolean isMoreThan(int a, int b){
		return (a > b) ;
	}
	
	/*
	<!-- 		<t:pagelink page="admin/config" >${message:configuration}</t:pagelink>
	 -->
	<!-- 	<t:pagelink page="admin/backuprestore/MigrateBaseData" >${message:migrate-base-data}</t:pagelink>
			<t:pagelink page="admin/backuprestore/migrateproject" >${message:migrate-project}</t:pagelink>
			<t:pagelink page="admin/backuprestore" >${message:backup-restore} ${message:project}</t:pagelink>
	-->
	<!--		
	<div class="lmenubox">
		<div class="title" id="lmt_statistics" onclick="toggleElm('lmc_statistics')"><div class="box">Statistics</div></div>
		<div class="content" id="lmc_statistics">
			<ul>
				<li>...</li>
				<li>...</li>
			</ul>
		</div>
	</div>
-->
	*/
}
