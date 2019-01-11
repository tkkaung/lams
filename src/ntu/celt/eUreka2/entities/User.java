package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class User implements Serializable{
	
	private static final long serialVersionUID = 7226902377491307717L;
	@NonVisual
	private int id;
	private String externalKey;
	@Validate("required")
	private String username;
	private String password;
	private String title;
	@Validate("required")
	private String firstName;
	@Validate("required")
	private String lastName;
	private String jobTitle;
	private School school;
	@Validate("required")
	private SysRole sysRole;
	private String organization;
	private String phone;
	private String mphone;
	@Validate("required")
	private String email;
	private boolean enabled = true;
	@Validate("required")
	private Date createDate;
	private Date modifyDate;
	private String ip;
	private String remarks;
	private Set<ProjUser> projects = new HashSet<ProjUser>();
	private Set<SysroleUser> extraRoles = new HashSet<SysroleUser>();
	
	/*
	public String toString() {
		final String DIVIDER = ", ";
		
		StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + ": ");
		buf.append("[");
		buf.append("id=" + id + DIVIDER);
		buf.append("username=" + username + DIVIDER);
		buf.append("password=" + password.replaceAll(".", "*") + DIVIDER);
		buf.append("firstName=" + firstName + DIVIDER);
		buf.append("lastName=" + lastName + DIVIDER);
		buf.append("jobTitle=" + jobTitle + DIVIDER);
		buf.append("school=" + school.toString() + DIVIDER);
		buf.append("sysRole=" + sysRole.toString() + DIVIDER);
		buf.append("organization=" + organization + DIVIDER);
		buf.append("phone=" + phone + DIVIDER);
		buf.append("mphone=" + mphone + DIVIDER);
		buf.append("email=" + email + DIVIDER);
		buf.append("enabled=" + enabled + DIVIDER);
		buf.append("createDate=" + createDate + DIVIDER);
		buf.append("modifyDate=" + modifyDate + DIVIDER);
		buf.append("ip=" + ip + DIVIDER);
		buf.append("remarks=" + remarks );
		buf.append("]");
		return buf.toString();
	}
	*/
	
	public User() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getExternalKey() {
		return externalKey;
	}
	public void setExternalKey(String externalKey) {
		this.externalKey = externalKey;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = null; //firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public School getSchool() {
		return school;
	}
	public void setSchool(School school) {
		this.school = school;
	}
	public SysRole getSysRole() {
		return sysRole;
	}
	public void setSysRole(SysRole sysRole) {
		this.sysRole = sysRole;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMphone() {
		return mphone;
	}
	public void setMphone(String mphone) {
		this.mphone = mphone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public void setProjects(Set<ProjUser> projects) {
		this.projects = projects;
	}
	public Set<ProjUser> getProjects() {
		return projects;
	}
	public void addProject(ProjUser projUser){
		projects.add(projUser);
		//projUser.getProject().addMember(projUser);
	}
	public void removeProject(ProjUser projUser){
		projects.remove(projUser);
		//projUser.getProject().removeMember(projUser);
	}
	public List<Project> getProjectList(){
		List<Project> pList = new ArrayList<Project>();
		for(ProjUser pu : projects){
			pList.add(pu.getProject());
		}
		return pList;
	}
	
	// The need for a equal() method is discussed at http://www.hibernate.org/109.html
    @Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof User) && username != null 
    			&& username.equals(((User) obj).getUsername());
    	
    }
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return username == null ? super.hashCode() : getUsername().hashCode();
	}
    
    /**
     * Check if the user have the given privilege
     * 
     * @param privId - the privilege to check
     * @return 
     */
    public boolean hasPrivilege(String privId){
    	return hasPrivilege(privId, null);
    }
    public boolean hasPrivilege(String privId, String param){
    	Privilege priv = new Privilege(privId);
    	if(param==null){
    		if( sysRole.getPrivileges().contains(priv)){
        		return true;
        	}
    		for(SysroleUser eRole : extraRoles){
    			if(eRole.getSysRole().getPrivileges().contains(priv))
    				return true;
    		}
    	}
    	else{
    		for(SysroleUser eRole : extraRoles){
    			if(eRole.getSysRole().getPrivileges().contains(priv)
    				&& param.equals(eRole.getParam())){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    public boolean hasSysRoleID(int roleId){
    	if(sysRole.getId()== roleId){
			return true;
		}
    	for(SysroleUser su : extraRoles){
			if(su.getSysRole().getId() == roleId)
				return true;
		}
    	return false;
    }
    
    
    /**
     * Get a customized name for displaying
     * 
     * @return	customized display name
     */
    public String getDisplayName(){
    	//if(".".equals(0))
    	//	return Util.capitalize(lastName);
    	//String str = Util.capitalize(lastName) + " " + Util.capitalize(firstName);
    	//return str;
    	return Util.capitalize(lastName);
    }
    
    public String getDisplayContactNo(){
    	if(mphone!=null && !mphone.isEmpty())
    		return mphone;
    	if(phone!=null)
    		return phone;
    	return "";
    }
    
    public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public String getModifyDateDisplay(){
		return Util.formatDateTime(modifyDate);
	}
	public void setExtraRoles(Set<SysroleUser> extraRoles) {
		this.extraRoles = extraRoles;
	}
	public Set<SysroleUser> getExtraRoles() {
		return extraRoles;
	}
	public void addExtraRole(SysroleUser extraRole){
		extraRoles.add(extraRole);
	}
	public void removeExtraRole(SysroleUser extraRole){
		extraRoles.remove(extraRole);
	}
	
}