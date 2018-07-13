package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class ProjRole implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1708333488521863311L;
	@NonVisual
	private int id;
	private String name;
	private String alias;
	private String des;
	private boolean system; //if it is defined by the system
	private Set<Privilege> privileges  = new HashSet<Privilege>();
	
	
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public void setPrivileges(Set<Privilege> privileges) {
		this.privileges = privileges;
	}
	public Set<Privilege> getPrivileges() {
		return privileges;
	}
	public boolean isSystem() {
		return system;
	}
	public void setSystem(boolean system) {
		this.system = system;
	}
	public String getDisplayName(){
		if(alias!=null)
			return alias;
		return name;
	}
	// The need for a equals() method is discussed at http://www.hibernate.org/109.html
    @Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof ProjRole) && name != null 
				&& name.equals(((ProjRole) obj).getName());
	}
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return getName() == null ? super.hashCode() : getName().hashCode();
	}
}
