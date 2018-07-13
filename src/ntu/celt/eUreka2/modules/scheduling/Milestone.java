package ntu.celt.eUreka2.modules.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Milestone implements Cloneable, JSONable{
	@NonVisual
	private Long id;
	private String name;
	private UrgencyLevel urgency = UrgencyLevel.MEDIUM;
	private String comment;
	private Date deadline;
	private User manager;  //creator
	private Date createDate;
	private Date modifyDate;
	private Schedule schedule;
	private List<Task> tasks = new ArrayList<Task>(); //all tasks including those under Phases.
	private List<Phase> phases = new ArrayList<Phase>();
	private String identifier;  //to identify when clone, export/import, compare
	
	public Milestone() {
		super();
		identifier = Util.generateUUID();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UrgencyLevel getUrgency() {
		return urgency;
	}
	public void setUrgency(UrgencyLevel urgency) {
		this.urgency = urgency;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public User getManager() {
		return manager;
	}
	public void setManager(User manager) {
		this.manager = manager;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getCreateDateDisplay(){
    	return Util.formatDateTime(createDate);
    }
    public String getModifyDateDisplay(){
    	return Util.formatDateTime(modifyDate);
    }
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	public Schedule getSchedule() {
		return schedule;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public void setPhases(List<Phase> phases) {
		this.phases = phases;
	}
	public List<Phase> getPhases() {
		return phases;
	}
	
	public List<Task> getNoPhaseTasks(){
		List<Task> tList = new ArrayList<Task>();
		for(Task t : tasks){
			if(t.getPhase()==null)
				tList.add(t);
		}
		return tList;
	}
	public void addTask(Task task){
		tasks.add(task);
		task.setMilestone(this);
		Collections.sort(tasks, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				if(o1.getStartDate()==null || o2.getStartDate()==null)
					return -1;
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
	}
	public void removeTask(Task task){
		tasks.remove(task);
		task.setMilestone(null);
	}
	public void addPhase(Phase phase){
		phases.add(phase);
		phase.setMilestone(this);
		Collections.sort(phases, new Comparator<Phase>() {
			@Override
			public int compare(Phase o1, Phase o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
	}
	public void removePhase(Phase phase){
		phases.remove(phase);
		phase.setMilestone(null);
	}
	public List<Activity> getSortedTasksAndPhases(){
		List<Activity> list = new ArrayList<Activity>();
		for(Phase p : phases){
			list.add(p);
		}
		for(Task t : tasks){
			if(t.getPhase()==null)   //get the top-level only
				list.add(t);
		}
		
		//Collections.sort(list);
		return list;
	}
	public boolean isEmpty(){
		if(phases.size()==0 && tasks.size()==0)
			return true;
		return false;
	}
	public String getDeadlineDisplay(){
		return Util.formatDateTime(deadline, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	
	@Override
	public Milestone clone(){
		Milestone ms = new Milestone();
		ms.setName(name);
		ms.setUrgency(urgency);
		ms.setComment(comment);
		ms.setDeadline(deadline);
		ms.setManager(manager);
		ms.setCreateDate(createDate);
		ms.setModifyDate(modifyDate);
		ms.setSchedule(schedule);
		ms.setIdentifier(identifier);
		for(Phase p : phases){
			Phase p2 = p.clone();
			ms.addPhase(p2);
			for(Task t : p2.getTasks()){
				ms.addTask(t);
			}
		}
		for(Task t : getNoPhaseTasks()){
			Task t2 = t.clone();
			ms.addTask(t2);
		}
		
		return ms;
	}
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("urgency", urgency.toString());
		j.put("comment", comment);
		j.put("deadline", deadline.getTime());
		j.put("manager", manager.getId());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate.getTime());
		j.put("schedule", schedule.getId());
		j.put("identifier", identifier);
		JSONArray jPhases = new JSONArray();
		for(Phase p : phases){
			jPhases.put(p.toJSONObject());
		}
		j.put("phases", jPhases);
		JSONArray jTasks = new JSONArray();
		for(Task t: getNoPhaseTasks()){
			jTasks.put(t.toJSONObject());
		}
		j.put("tasks", jTasks);
		
		return j;
	}
	
	/**
	 * compute and return the average of PercentageDone of tasks
	 * @return 
	 */
	public int getAveragePercentDone(){
		if(tasks.size()==0)
			return 0;
		int result = 0;
		for(Task t: tasks){
			result += t.getPercentageDone();
		}
		result = Math.round(result/tasks.size());
		
		return result;
	}
	public void setIdentifier(String identifier) {
		if(identifier==null)
			identifier = Util.generateUUID();
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	
	
}
