package ntu.celt.eUreka2.modules.backuprestore.migrateEnities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MMilestone {
	private String projId;
	private String milestone_id;
	private String owner;
	private String name;
	private String comments;
	private Date deadline; 
	private String urgency;
	private List<MPhase> phases = new ArrayList<MPhase>();
	private List<MTask> tasks = new ArrayList<MTask>();
	
	public String getProjId() {
		return projId;
	}
	public void setProjId(String projId) {
		this.projId = projId;
	}
	public String getMilestone_id() {
		return milestone_id;
	}
	public void setMilestone_id(String milestoneId) {
		milestone_id = milestoneId;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public String getUrgency() {
		return urgency;
	}
	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}
	public List<MPhase> getPhases() {
		return phases;
	}
	public void setPhases(List<MPhase> phases) {
		this.phases = phases;
	}
	public List<MTask> getTasks() {
		return tasks;
	}
	public void setTasks(List<MTask> tasks) {
		this.tasks = tasks;
	}
	
	public void addPhase(MPhase phase){
		phases.add(phase);
	}
	public void addTask(MTask task){
		tasks.add(task);
	}
}
