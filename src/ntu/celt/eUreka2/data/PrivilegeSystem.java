package ntu.celt.eUreka2.data;

public class PrivilegeSystem extends PrivilegeConstant{
	public static final String CREATE_PROJECT = "s01";
	public static final String SYSTEM_ANNOUNCEMENT = "s02";
	public static final String PROJ_SCHOOL_ANNOUNCEMENT = "s02a";
	public static final String PROJ_TYPE_ANNOUNCEMENT = "s02b";
	public static final String MANAGE_PROJECT = "s03";
	public static final String MANAGE_USER = "s04";
	public static final String MANAGE_EMAIL_TEMPLATE = "s06";
	public static final String MANAGE_SYSTEM_DATA = "s07";	//privilege, projStatus, module,school
	public static final String MANAGE_SYSTEM_ROLE = "s08";
	public static final String MANAGE_PROJ_ROLE = "s08a";
	public static final String MANAGE_PROJ_TYPE = "s08b";
	public static final String MANAGE_OWN_RUBRIC = "s09";
	public static final String MANAGE_RUBRIC = "s10";
	public static final String MANAGE_SCHOOL_RUBRIC = "s10a";
	public static final String CHANGE_EMAIL = "s11";
	public static final String DIRECT_LOGIN = "s12";
	public static final String EVAL_WITHOUT_GROUP = "s13";
	public static final String LEADSHIP_PROFILING = "s14";
	
	
	public String getCreateProject() {
		return CREATE_PROJECT;
	}
	public String getSystemAnnouncement() {
		return SYSTEM_ANNOUNCEMENT;
	}
	public String getManageProject() {
		return MANAGE_PROJECT;
	}
	public String getManageUser() {
		return MANAGE_USER;
	}
	public String getManageEmailTemplate() {
		return MANAGE_EMAIL_TEMPLATE;
	}
	public String getManageSystemData() {
		return MANAGE_SYSTEM_DATA;
	}
	public String getManageSystemRole() {
		return MANAGE_SYSTEM_ROLE;
	}
	public String getManageProjRole() {
		return MANAGE_PROJ_ROLE;
	}
	public String getManageProjType() {
		return MANAGE_PROJ_TYPE;
	}
	public String getProjSchoolAnnouncement() {
		return PROJ_SCHOOL_ANNOUNCEMENT;
	}
	public String getProjTypeAnnouncement() {
		return PROJ_TYPE_ANNOUNCEMENT;
	}
	public String getManageOwnRubric() {
		return MANAGE_OWN_RUBRIC;
	}
	public String getManageRubric() {
		return MANAGE_RUBRIC;
	}
	public String getManageSchoolRubric() {
		return MANAGE_SCHOOL_RUBRIC;
	}
	public static String getChangeEmail() {
		return CHANGE_EMAIL;
	}
	public static String getDirectLogin() {
		return DIRECT_LOGIN;
	}
	
	
}
