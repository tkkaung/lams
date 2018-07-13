package ntu.celt.eUreka2.modules.backuprestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class BackupEntry {
	private String id;
	private User creator;
	private Project project;
	private Date createDate;
	private BackupAttachedFile attachedFile;
	private String remarks;
	private List<String> succeededBackups = new ArrayList<String>();  
	private List<String> failedBackups = new ArrayList<String>();  //just temp values, not save to database
	
	public BackupEntry() {
		super();
		id = Util.generateUUID();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public BackupAttachedFile getAttachedFile() {
		return attachedFile;
	}
	public void setAttachedFile(BackupAttachedFile attachedFile) {
		this.attachedFile = attachedFile;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public void setSucceededBackups(List<String> succeededBackups) {
		this.succeededBackups = succeededBackups;
	}
	public List<String> getSucceededBackups() {
		return succeededBackups;
	}
	public void addSucceededBackups(String moduleName){
		succeededBackups.add(moduleName);
	}
	public void setFailedBackups(List<String> failedBackups) {
		this.failedBackups = failedBackups;
	}
	public List<String> getFailedBackups() {
		return failedBackups;
	}
	public void addFailedBackups(String moduleName){
		failedBackups.add(moduleName);
	}
	
}
