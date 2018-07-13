package ntu.celt.eUreka2.modules.backuprestore.migrateEnities;

import java.util.Date;

public class MProject {
	/*
	 * fields in database table mbcampaign: 
	campaign_id, name, description, startDate, endDate, creator_id, 
	createDate, privateCampaign, logo, logoThumbnail, logoThumbnailWidth, 
	logoThumbnailHeight, link_id, closeDateMillis, assetTreeId, status, statusSetDateMillis,
	school_id, course_id, project_no, academic_year, isp, exam_date, exam_venue, semester_off, 
	campaign_type, modifier_id, lastModifiedDate, contentLastModifiedDate, new_converted_campaign_id,
	mark, cao
	*/
	
	
	private String id;
	private String name;
	private String desc;
	private Date startDate; 
	private Date endDate;
	private String creatorId;
	private Date createDate;
	private boolean privateProj;  //shared
	private String status;
	private Date statusChangedDate;
	private String SchoolId;
	private String courseId;
	private String projNo;
	private String academicYear;
	private String projType;
	private String modifierId;
	private Date lastModifiedDate;
	private boolean cao;
	
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
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public boolean isPrivateProj() {
		return privateProj;
	}
	public void setPrivateProj(boolean privateProj) {
		this.privateProj = privateProj;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getStatusChangedDate() {
		return statusChangedDate;
	}
	public void setStatusChangedDate(Date statusChangedDate) {
		this.statusChangedDate = statusChangedDate;
	}
	public String getSchoolId() {
		return SchoolId;
	}
	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getProjNo() {
		return projNo;
	}
	public void setProjNo(String projNo) {
		this.projNo = projNo;
	}
	public String getAcademicYear() {
		return academicYear;
	}
	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}
	public String getProjType() {
		return projType;
	}
	public void setProjType(String projType) {
		this.projType = projType;
	}
	public String getModifierId() {
		return modifierId;
	}
	public void setModifierId(String modifierId) {
		this.modifierId = modifierId;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public boolean isCao() {
		return cao;
	}
	public void setCao(boolean cao) {
		this.cao = cao;
	}
	public String getDisplayName(){
		return id + " - " + name;
	}
	
	
}
