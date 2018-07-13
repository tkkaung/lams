package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import ntu.celt.eUreka2.data.PrivilegeType;

import org.hibernate.annotations.Entity;

@Entity
public class Privilege implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1122392875890273576L;
	private String id;
	private String name;
	private String description;
	private PrivilegeType type;
	
	public String toString() {
		final String DIVIDER = ", ";
		
		StringBuffer buf = new StringBuffer();
		buf.append(this.getClass().getSimpleName() + ": ");
		buf.append("[");
		buf.append("id=" + id + DIVIDER);
		buf.append("name=" + name + DIVIDER);
		buf.append("description=" + description + DIVIDER);
		buf.append("type=" + type );
		
		buf.append("]");
		return buf.toString();
	}
	
	
	public Privilege(){
	}
	public Privilege(String id){
		this.id = id;
	}
	public Privilege(PrivilegeType type, String id, String name, String description){
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setType(PrivilegeType type) {
		this.type = type;
	}
	public PrivilegeType getType() {
		return type;
	}

	/**
	 * @return check the equal by 'id'
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof Privilege) && id != null 
				&& id.equals(((Privilege) obj).getId());
	}
	/**
	 * override method, hashCode is calculate by the field 'id'
	 */
	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}
}
