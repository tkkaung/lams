package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

@Entity
public class ProjModule implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3910157810333805052L;
	@NonVisual
	private int id;
	@Validate("Required")
	private Project project;
	@Validate("Required")
	private Module module;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public Module getModule() {
		return module;
	}
	public void setModule(Module module) {
		this.module = module;
	}
	public ProjModule() {
		super();
	}
	public ProjModule(Project project, Module module) {
		super();
		this.project = project;
		this.module = module;
	}
	
	
}
