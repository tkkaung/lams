package ntu.celt.eUreka2.data;

public class PrivilegeProject extends PrivilegeConstant{
	public static final String READ_PROJECT = "p01";
	public static final String UPDATE_PROJECT = "p02";
	public static final String ENROLL_MEMBER = "p03";
	public static final String ENROLL_ORG_SUPERVISOR = "p03a";
	public static final String ASSIGN_MODULE = "p04";
	public static final String CHANGE_STATUS = "p05";
	public static final String UPDATE_PROJECT_SCOPE = "p06";
	public static final String APPROVE_PROJECT_SCOPE = "p07";
	public static final String IS_LEADER = "p08";
	public static final String MANAGE_GROUP = "p09";
	
	
	public String getReadProject() {
		return READ_PROJECT;
	}
	public String getUpdateProject() {
		return UPDATE_PROJECT;
	}
	public String getEnrollMember() {
		return ENROLL_MEMBER;
	}
	public String getEnrollOrgSupervisor() {
		return ENROLL_MEMBER;
	}
	public String getAssignModule() {
		return ASSIGN_MODULE;
	}
	public String getChangeStatus() {
		return CHANGE_STATUS;
	}
	public String getUpdateProjectScope() {
		return UPDATE_PROJECT_SCOPE;
	}
	public String getApproveProjectScope() {
		return APPROVE_PROJECT_SCOPE;
	}
	
	
	
}
