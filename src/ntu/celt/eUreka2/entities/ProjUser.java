package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class ProjUser implements Serializable {
	
	private static final long serialVersionUID = 4276891224185386940L;
	@NonVisual
	private int id;
	@Validate("Required")
	private Project project;
	@Validate("Required")
	private User user;
	@Validate("Required")
	private ProjRole role;
	private String customRoleName;
	

	public ProjUser() {
		super();
	}
	public ProjUser(Project project, User user, ProjRole role) {
		super();
		this.user = user;
		this.project = project;
		this.role = role;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public ProjRole getRole() {
		return role;
	}
	public void setRole(ProjRole role) {
		this.role = role;
	}
	public void setCustomRoleName(String cusRoleName) {
		this.customRoleName = cusRoleName;
	}
	public String getCustomRoleName() {
		return customRoleName;
	}
	public String getDisplayRoleName(){
		if(customRoleName!=null)
			return customRoleName;
		return role.getDisplayName();
	}
	
	/**
     * Check if the user have the given privilege
     * 
     * @param privId - the privilege to check
     * @return 
     */
	public boolean hasPrivilege(String privId){
    	return role.getPrivileges().contains(new Privilege(privId));
    }
}
