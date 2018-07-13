package ntu.celt.eUreka2.modules.backuprestore.migrateEnities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MTask {
	private String task_id;
	private String milestone_id;
	private String owner_id;
	private String name;
	private String comments;
	private Date startDate;
	private Date endDate;
	private Date dateCompleted;
	private String urgency;
	private boolean notifyOwnerOfEndDate;
	private String phase_link_id;
	private int percentage;
	private List<String> assignedMembers = new ArrayList<String>();
	
	public String getTask_id() {
		return task_id;
	}
	public void setTask_id(String taskId) {
		task_id = taskId;
	}
	public String getMilestone_id() {
		return milestone_id;
	}
	public void setMilestone_id(String milestoneId) {
		milestone_id = milestoneId;
	}
	public String getOwner_id() {
		return owner_id;
	}
	public void setOwner_id(String ownerId) {
		owner_id = ownerId;
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
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getDateCompleted() {
		return dateCompleted;
	}
	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	public String getUrgency() {
		return urgency;
	}
	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}
	public boolean isNotifyOwnerOfEndDate() {
		return notifyOwnerOfEndDate;
	}
	public void setNotifyOwnerOfEndDate(boolean notifyOwnerOfEndDate) {
		this.notifyOwnerOfEndDate = notifyOwnerOfEndDate;
	}
	public String getPhase_link_id() {
		return phase_link_id;
	}
	public void setPhase_link_id(String phaseLinkId) {
		phase_link_id = phaseLinkId;
	}
	public int getPercentage() {
		return percentage;
	}
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}
	public void setAssignedMembers(List<String> assignedMembers) {
		this.assignedMembers = assignedMembers;
	}
	public List<String> getAssignedMembers() {
		return assignedMembers;
	}
	public void addAssignedMember(String username){
		this.assignedMembers.add(username);
	}
	
}
