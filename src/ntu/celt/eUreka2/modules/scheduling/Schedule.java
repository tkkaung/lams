package ntu.celt.eUreka2.modules.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Schedule implements Cloneable, JSONable {
	private Long id ;
	private Project project;
	private Date createDate;
	private User user;  //creator
	private List<Milestone> milestones = new ArrayList<Milestone>();
	private boolean active;
	private String remarks;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setMilestones(List<Milestone> milestones) {
		this.milestones = milestones;
	}
	public List<Milestone> getMilestones() {
		return milestones;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getRemarks() {
		return remarks;
	}
	
	
	public void addMilestone(Milestone milestone){
		this.milestones.add(milestone);
		Collections.sort(milestones, new Comparator<Milestone>() {
			@Override
			public int compare(Milestone o1, Milestone o2) {
				return o1.getDeadline().compareTo(o2.getDeadline());
			}
		});
		milestone.setSchedule(this);
	}
	public void removeMilestone(Milestone milestone){
		this.milestones.remove(milestone);
		milestone.setSchedule(null);
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime2(createDate);
	}
	@Override
	public Schedule clone(){
		Schedule schd = new Schedule();
		schd.setProject(project);
		schd.setCreateDate(createDate);
		schd.setUser(user);
		for(Milestone ms : milestones){
			schd.addMilestone(ms.clone());
		}
		schd.setActive(active);
		schd.setRemarks(remarks);
		
		return schd;
	}
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("project", project.getId());
		j.put("createDate", createDate.getTime());
		j.put("user", user.getId());
		j.put("active", active);
		j.put("remarks", remarks);
		JSONArray jMilestones = new JSONArray();
		for(Milestone m : milestones){
			jMilestones.put(m.toJSONObject());
		}
		j.put("milestones", jMilestones);
		
		
		
		return j;
	}
}