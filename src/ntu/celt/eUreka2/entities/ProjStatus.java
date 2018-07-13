package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class ProjStatus implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5751646680943096869L;
	@NonVisual
	private int id;
	private String name;
	private String alias;
	private String description;
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
	public String getDisplayName(){
		if(alias!=null)
			return alias;
		return name;
	}
	// The need for a equals() method is discussed at http://www.hibernate.org/109.html
    @Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof ProjStatus) && name != null 
				&& name.equals(((ProjStatus) obj).getName());
	}
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return getName() == null ? super.hashCode() : getName().hashCode();
	}
}
