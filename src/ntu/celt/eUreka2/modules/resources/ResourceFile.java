package ntu.celt.eUreka2.modules.resources;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.entities.User;

public class ResourceFile extends Resource{
	
	private List<ResourceFileVersion> fileVersions = new ArrayList<ResourceFileVersion>();
	private User lockedBy; //checked out by, null if no body check out
	
	public ResourceType getType() {
		return ResourceType.File;
	}
	public ResourceFile clone(){
		ResourceFile rf = (ResourceFile) super.clone(ResourceFile.class);
		rf.setLockedBy(lockedBy);
		for(ResourceFileVersion rfv : fileVersions){
			rf.addFileVersion(rfv.clone());
		}
		return rf;
	}
	
	public void setFileVersions(List<ResourceFileVersion> fileVersions) {
		this.fileVersions = fileVersions;
	}
	public List<ResourceFileVersion> getFileVersions() {
		return fileVersions;
	}
	public void addFileVersion(ResourceFileVersion fileVersion){
		this.fileVersions.add(fileVersion);
		fileVersion.setRfile(this);
	}
	public void removeFileVersion(ResourceFileVersion fileVersion){
		this.fileVersions.remove(fileVersion);
		fileVersion.setRfile(null);
	}
	public ResourceFileVersion getLatestFileVersion(){
		if(fileVersions.isEmpty())
			return null;
		return fileVersions.get(fileVersions.size()-1);
	}
	public boolean hasMultiVersion(){
		if(fileVersions.size()>1)
			return true;
		return false;
	}
	public int getLatestVersion(){
		if(fileVersions.isEmpty())
			return 0;
		return fileVersions.get(fileVersions.size()-1).getVersion();
	}
	

	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
	}

	public User getLockedBy() {
		return lockedBy;
	}
	public boolean isLocked(){
		if(lockedBy==null)
			return false;
		return true;
	}
	public int getTotalNumDownload(){
		int count=0;
		for(ResourceFileVersion rfv : fileVersions){
			count += rfv.getNumDownload();
		}
		return count;
	}
}
