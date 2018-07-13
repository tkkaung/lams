package ntu.celt.eUreka2.data;

public class ProjectStat {
	private String id;
	private String name;
	private String status;
	private String school;
	private String type;
//	private String lastaccess;
	private int visit;
	private int member;
	private long announcement;
	private long assessment;
	private long budget;
	private long blog;
	private long elog;
	private long resource;
	private long forum;
	private long timeline;
	private long note;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getVisit() {
		return visit;
	}
	public void setVisit(int visit) {
		this.visit = visit;
	}
	public int getMember() {
		return member;
	}
	public void setMember(int member) {
		this.member = member;
	}
	public long getAnnouncement() {
		return announcement;
	}
	public void setAnnouncement(long announcement) {
		this.announcement = announcement;
	}
	public long getAssessment() {
		return assessment;
	}
	public void setAssessment(long assessment) {
		this.assessment = assessment;
	}
	public long getBudget() {
		return budget;
	}
	public void setBudget(long budget) {
		this.budget = budget;
	}
	public long getBlog() {
		return blog;
	}
	public void setBlog(long blog) {
		this.blog = blog;
	}
	public long getElog() {
		return elog;
	}
	public void setElog(long elog) {
		this.elog = elog;
	}
	public long getResource() {
		return resource;
	}
	public void setResource(long resource) {
		this.resource = resource;
	}
	public long getForum() {
		return forum;
	}
	public void setForum(long forum) {
		this.forum = forum;
	}
	public long getTimeline() {
		return timeline;
	}
	public void setTimeline(long timeline) {
		this.timeline = timeline;
	}
	public long getNote() {
		return note;
	}
	public void setNote(long note) {
		this.note = note;
	}
}
