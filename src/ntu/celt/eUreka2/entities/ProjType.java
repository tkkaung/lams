package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.data.ProjTypeSetting;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class ProjType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8858638642405432319L;
	@NonVisual
	private int id;
	@Validate("required")
	private String name;
	private String alias;
	private String des;
	private boolean system;
	private List<Module> defaultModules = new ArrayList<Module>(); //compulsory modules
	private List<Module> modules = new ArrayList<Module>(); //compulsory modules
	private List<Module> nonModules = new ArrayList<Module>(); //must not include these modules
	private List<ProjRole> roles = new ArrayList<ProjRole>(); //possible roles
	private List<ProjTypeSetting> settings = new ArrayList<ProjTypeSetting>(); //additional functions
	
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public void setDefaultModules(List<Module> defaultModules) {
		this.defaultModules = defaultModules;
	}
	public void addDefaultModule(Module module) {
		this.defaultModules.add(module);
	}
	public void removeDefaultModule(Module module) {
		this.defaultModules.remove(module);
	}
	public List<Module> getDefaultModules() {
		return defaultModules;
	}
	public List<Module> getModules() {
		return modules;
	}
	public void setModules(List<Module> modules) {
		this.modules = modules;
	}
	public void addModule(Module module) {
		this.modules.add(module);
	}
	public void removeModule(Module module) {
		this.modules.remove(module);
	}
	public List<Module> getNonModules() {
		return nonModules;
	}
	public void setNonModules(List<Module> nonModules) {
		this.nonModules = nonModules;
	}
	public void addNonModule(Module nonModule) {
		this.nonModules.add(nonModule);
	}
	public void removeNonModule(Module nonModule) {
		this.nonModules.remove(nonModule);
	}
	public List<ProjRole> getRoles() {
		return roles;
	}
	public void setRoles(List<ProjRole> roles) {
		this.roles = roles;
	}
	public void addRole(ProjRole role){
		this.roles.add(role);
	}
	public void removeRole(ProjRole role){
		this.roles.remove(role);
	}
	
	public void setSettings(List<ProjTypeSetting> settings) {
		this.settings = settings;
	}
	public List<ProjTypeSetting> getSettings() {
		return settings;
	}
	public void addSetting(ProjTypeSetting setting){
		this.settings.add(setting);
	}
	public void removeSetting(ProjTypeSetting setting){
		this.settings.remove(setting);
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
		return (obj == this) || (obj instanceof ProjType) && name != null 
				&& name.equals(((ProjType) obj).getName());
	}
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return getName() == null ? super.hashCode() : getName().hashCode();
	}
}
