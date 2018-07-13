package ntu.celt.eUreka2.entities;


import java.io.Serializable;

import ntu.celt.eUreka2.modules.backuprestore.JSONable;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.json.JSONObject;
import org.hibernate.annotations.Entity;

@Entity
public class School implements Serializable, JSONable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8275906627450727640L;
	@NonVisual
	private int id;
	@Validate("required")
	private String name;
	private String alias;
	@Validate("required")
	private String des;
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
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
		else 
			return name;
	}
	public String getDisplayNameLong(){
		return getDisplayName()+" - "+ getDes();
	}
	
	@Override
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("description", des);
		j.put("alias", alias);
	//	j.put("isSystem", system);
		return j;
	}
	
	
	
	// The need for a equals() method is discussed at http://www.hibernate.org/109.html
    @Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof School) && name != null 
				&& name.equals(((School) obj).getName());
	}
    
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return name == null ? super.hashCode() : name.hashCode();
	}
}
