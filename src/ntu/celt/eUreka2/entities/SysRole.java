package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class SysRole implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -379089968539436018L;
	@NonVisual
	private int id;
	@Validate("required")
	private String name;
	private String alias;
	private String description;
	private Set<Privilege> privileges  = new HashSet<Privilege>();
	private boolean system;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getAlias() {
		return alias;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isSystem() {
		return system;
	}
	public void setSystem(boolean system) {
		this.system = system;
	}
	public void setPrivileges(Set<Privilege> privileges) {
		this.privileges = privileges;
	}
	public Set<Privilege> getPrivileges() {
		return privileges;
	}
	public List<String> getPrivilegeIDList() {
		List<String> privs = new ArrayList<String>();
		for(Privilege priv : privileges){
			privs.add(priv.getId());
		}
		return privs;
	}
	public String getDisplayName(){
		if(alias!=null)
			return alias;
		return name;
	}
    // The need for a equals() method is discussed at http://www.hibernate.org/109.html
    @Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof SysRole) && name != null 
				&& name.equals(((SysRole) obj).getName());
	}
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return getName() == null ? super.hashCode() : getName().hashCode();
	}
}
