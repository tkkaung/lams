package ntu.celt.eUreka2.entities;

import org.hibernate.annotations.Entity;

import ntu.celt.eUreka2.services.attachFiles.AttachedFile;

@Entity
public class ProjectAttachedFile extends AttachedFile  {
	
	private static final long serialVersionUID = -1988239822509499380L;
	private Project proj;

	public void setProj(Project proj) {
		this.proj = proj;
	}

	public Project getProj() {
		return proj;
	}
	
	public ProjectAttachedFile clone(){
		ProjectAttachedFile at = new ProjectAttachedFile();
		at.setId(id);
		at.setFileName(fileName);
		at.setAliasName(aliasName);
		at.setContentType(contentType);
		at.setPath(path);
		at.setSize(size);
		at.setCreator(creator);
		at.setProj(proj);
		
		return at;
	}
}
