package ntu.celt.eUreka2.data;

public class PredefinedNames {
	/* entities */
	public static final String SYSROLE_SUPER_ADMIN = "Super_admin";
	public static final String SYSROLE_USER = "User";
	public static final String SYSROLE_EXTERNAL_USER = "External-User";
	public static final String SYSROLE_ADMIN = "Admin";
	public static final String SYSROLE_STUDENT = "Student";
	public static final String SYSROLE_FACULTY = "Faculty";
	public static final String SYSROLE_SCHOOL_ADMIN = "School-Admin";
	public static final String SYSROLE_PROJTYPE_ADMIN = "Project-Type-Admin";
	public static final String ANONYMOUS = "Anonymous";
	public static final String PROJROLE_LEADER = "Leader";
	public static final String PROJROLE_MEMBER = "Member";
	public static final String PROJROLE_STUDENT = "Student";
	public static final String PROJROLE_SUPERVISOR = "Supervisor";
	public static final String PROJROLE_ORG_SUPERVISOR = "Org-Supervisor";
	public static final String PROJROLE_SCHOOL_TUTOR = "School-Tutor";
	public static final String PROJROLE_EXAMINER = "Examiner";
	public static final String PROJROLE_SECRETARY = "Secretary";
	public static final String PROJROLE_SENATOR = "Senator";
	public static final String PROJSTATUS_ACTIVE = "Active";
	public static final String PROJSTATUS_INACTIVE = "Inactive"; //no view for 90 days, then send email to leader. if Inactive for 30, it change to Archived
	public static final String PROJSTATUS_REFERENCE = "Reference"; //view only, no more edit allowed, none-members can access
	public static final String PROJSTATUS_ARCHIVED = "Archived"; //mark for archive, separate process to move it out
	public static final String PROJSTATUS_DELETED = "Deleted";
	public static final String PROJTYPE_ADHOC = "ADH";
	public static final String PROJTYPE_FYP = "FYP";
	public static final String PROJTYPE_CAO = "CAO";
	public static final String PROJTYPE_SENATE = "SEN";
	public static final String PROJTYPE_COURSE = "COU";
	public static final String SCHOOL_OTHERS = "Others";
	
	
	/* modules */ /*tip: avoid using space or punctuation marks in the names, because we may use these to name folder */
	public static final String MODULE_ANNOUNCEMENT = "Announcement";
	public static final String MODULE_FORUM = "Forum";
	public static final String MODULE_SCHEDULING = "Scheduling";
	public static final String MODULE_TIMELINE = "Timeline";
	public static final String MODULE_BUDGET = "Budget";
	public static final String MODULE_MESSAGE = "Message";
	public static final String MODULE_BLOG = "Blog";
	public static final String MODULE_ELOG = "eLog";
	public static final String MODULE_LEARNING_LOG = "LearningLog";
	public static final String MODULE_RESOURCE = "Resource";
	public static final String MODULE_ASSESSMENT = "Assessment";
	public static final String MODULE_USAGE = "Usage";
	public static final String MODULE_PEER_EVALUATION = "PeerEvaluation";
	public static final String MODULE_LEADERSHIP_PROFILING = "LeadershipProfiling";
	public static final String MODULE_LEADERSHIP_COMPETENCY = "LCDP";
	public static final String MODULE_CARE_PSYCHOMETRIC_SURVEY = "CARE";
	public static final String PROJECT_INFO = "ProjectInfo";
	
	
}
