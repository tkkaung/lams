package ntu.celt.eUreka2.modules.backuprestore.migrateEnities;

import java.util.Date;


public class MUser {
	private String external_person_key;
	private String userId;
	private String userName;
	private String password;
	private Date createDate;
	private String email;
	private String firstName;
	private String lastName;
	private String title;
	private String department;
	private String job_title;
	private boolean externalUser;
	private boolean disabled;
	private String contactNumber;
	private String institution_role;
	private Date lastModifiedDate;
	
	public String getExternal_person_key() {
		return external_person_key;
	}
	public void setExternal_person_key(String externalPersonKey) {
		external_person_key = externalPersonKey;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getJob_title() {
		return job_title;
	}
	public void setJob_title(String jobTitle) {
		job_title = jobTitle;
	}
	public boolean isExternalUser() {
		return externalUser;
	}
	public void setExternalUser(boolean externalUser) {
		this.externalUser = externalUser;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	public String getInstitution_role() {
		return institution_role;
	}
	public void setInstitution_role(String institutionRole) {
		institution_role = institutionRole;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	
}
