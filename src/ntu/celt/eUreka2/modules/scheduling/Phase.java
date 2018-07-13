package ntu.celt.eUreka2.modules.scheduling;

import java.util.ArrayList;
import java.util.Calendar;
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

public class Phase implements Activity, Cloneable, JSONable{
	@NonVisual
	private Long id;
	private String name;
	private String comment;
	private User manager;
	private Date startDate;
	private Date endDate;
	private List<Task> tasks = new ArrayList<Task>();
	private Milestone milestone;
	private Date createDate;
	private Date modifyDate;
	private String identifier;  //to identify when clone, export/import, compare
	
	public Phase() {
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
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public User getManager() {
		return manager;
	}
	public void setManager(User manager) {
		this.manager = manager;
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
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public Milestone getMilestone() {
		return milestone;
	}
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
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
	public void addTask(Task task){
		tasks.add(task);
		Collections.sort(tasks, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
	}
	public void removeTask(Task task){
		tasks.remove(task);
		task.setPhase(null);
	}
	@Override
	public int compareTo(Activity o) {
		return getStartDate().compareTo(o.getStartDate());
	}
	public String getStartDateDisplay(){
		return Util.formatDateTime(startDate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getEndDateDisplay(){
		return Util.formatDateTime(endDate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	
	@Override
	public Phase clone(){
		Phase p = new Phase();
		p.setName(name);
		p.setComment(comment);
		p.setManager(manager);
		p.setStartDate(startDate);
		p.setEndDate(endDate);
		for(Task t : tasks){
			p.addTask(t.clone());
		}
		p.setMilestone(milestone);
		p.setCreateDate(createDate);
		p.setModifyDate(modifyDate);
		p.setIdentifier(identifier);
		
		return p;
	}
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("comment", comment);
		j.put("manager", manager.getId());
		j.put("startDate", startDate.getTime());
		j.put("endDate", endDate.getTime());
		j.put("milestone", milestone.getId());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate.getTime());
		j.put("identifier", identifier);
		JSONArray jTasks = new JSONArray();
		for(Task t : tasks){
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
	/**
	 * 
	 * @return number of days from startDate to endDate
	 */
	public int getDuration(){
		Calendar sDate = Calendar.getInstance();
		sDate.setTime(startDate);
		Calendar eDate = Calendar.getInstance();
		eDate.setTime(endDate);
		
		return Util.getNumDaysBetween(sDate, eDate);
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
