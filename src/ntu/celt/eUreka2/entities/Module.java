package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class Module implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2593092619584182748L;
	@NonVisual
	private int id;
	@Validate("Required")
	private String name;
	private String alias;
	private String description;
	@Validate("Required")
	private String rooturl;
	private String iconUrl = "";
	@Validate("Required")
	private boolean available = true;
	private String version;
	@NonVisual
	private Date cdate = new Date();
	private String remarks;
	
	
	
	public Module() {
		super();
	}
	
	
	public Module(String name, String alias, String description, String rooturl, String iconUrl) {
		super();
		this.name = name;
		this.alias = alias;
		this.description = description;
		this.rooturl = rooturl;
		this.iconUrl = iconUrl;
	}


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
	public String getRooturl() {
		return rooturl;
	}
	public void setRooturl(String rooturl) {
		this.rooturl = rooturl.trim();
	}
	public void setIconUrl(String iconUrl) {
		if(iconUrl==null)
			this.iconUrl = iconUrl;
		else
			this.iconUrl = iconUrl.trim();
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public String getDisplayName(){
		if(alias!=null)
			return alias;
		return name;
	}
	
	// The need for a equals() method is discussed at http://www.hibernate.org/109.html
    @Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof Module) && name != null 
				&& name.equals(((Module) obj).getName());
	}
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return getName() == null ? super.hashCode() : getName().hashCode();
	}
}
