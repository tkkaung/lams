package ntu.celt.eUreka2.modules.scheduling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Task implements Activity, Cloneable, JSONable {
	//@NonVisual
	private Long id;
	private String name;
	private UrgencyLevel urgency;
	private String comment;
	private Set<User> assignedPersons = new HashSet<User>();
	private Integer percentageDone;
	private Date startDate;
	private Date endDate;
	private User manager;
	private User editor;
	private Date createDate; 
	private Date modifyDate;
	private Date completeDate;
	private Phase phase; //if phase is null, then directly link to Milestone
	private Milestone milestone;
	private Set<Integer> daysToRemind = new HashSet<Integer>();
	private ReminderType reminderType;
	private String identifier;  //to identify when clone, export/import, compare
	
	
	public Task() {
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
	public Set<User> getAssignedPersons() {
		return assignedPersons;
	}
	public void setAssignedPersons(Set<User> assignedPersons) {
		this.assignedPersons = assignedPersons;
	}
	public Integer getPercentageDone() {
		return percentageDone;
	}
	public void setPercentageDone(Integer percentageDone) {
		this.percentageDone = percentageDone;
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
	public void setManager(User manager) {
		this.manager = manager;
	}
	public User getManager() {
		return manager;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	public User getEditor() {
		return editor;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}
	public Date getCompleteDate() {
		return completeDate;
	}
	public Phase getPhase() {
		return phase;
	}
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	public Milestone getMilestone() {
		return milestone;
	}
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}
	public void setDaysToRemind(Set<Integer> daysEarlierToRemind) {
		this.daysToRemind = daysEarlierToRemind;
	}
	public Set<Integer> getDaysToRemind() {
		return daysToRemind;
	}
	
	public void setReminderType(ReminderType reminderType) {
		this.reminderType = reminderType;
	}
	public ReminderType getReminderType() {
		return reminderType;
	}
	public void addAssignedPerson(User user){
		assignedPersons.add(user);
	}
	public void removeAssignedPerson(User user){
		assignedPersons.remove(user);
	}
	public void addDaysToRemind(Integer numDay){
		daysToRemind.add(numDay);
	}
	public void removeDaysToRemind(Integer numDay){
		daysToRemind.remove(numDay);
	}
	public String getAssignedPersonsDisplay(){
		String result = "";
		for(User u : assignedPersons){
			result += u.getDisplayName() + ", ";
		}
		if(!result.isEmpty()){
			return Util.removeLastSeparator(result, ", "); 
		}
		else{
			return "-";
		}
	}
	public String getAssignedPersonUsernames(){
		String result = "";
		for(User u : assignedPersons){
			result += u.getUsername() + ", ";
		}
		if(!result.isEmpty()){
			return Util.removeLastSeparator(result, ", "); 
		}
		else{
			return result = "-";
		}
	}
	@Override
	public int compareTo(Activity o) {
		return getStartDate().compareTo(o.getStartDate());
	}
	public List<String> getAssignedPersonsAsStringIds(){
		List<String> list = new ArrayList<String>();
		for(User u: assignedPersons){
			list.add(Integer.toString(u.getId()));
		}
		return list;
	}
	public String getStartDateDisplay(){
		return Util.formatDateTime(startDate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getEndDateDisplay(){
		return Util.formatDateTime(endDate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getModifyDateDisplay(){
		if(modifyDate==null)
			return null;
		return Util.formatDateTime(modifyDate);
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public String getCompleteDateDisplay(){
		if(completeDate==null)
			return null;
		return Util.formatDateTime2(completeDate);
	}
	@Override
	public Task clone(){
		Task t = new Task();
		t.setName(name);
		t.setUrgency(urgency);
		t.setComment(comment);
		for(User u : assignedPersons){
			t.addAssignedPerson(u);
		}
		t.setPercentageDone(percentageDone);
		t.setStartDate(startDate);
		t.setEndDate(endDate);
		t.setManager(manager);
		t.setEditor(editor);
		t.setCreateDate(createDate);
		t.setModifyDate(modifyDate);
		t.setCompleteDate(completeDate);
		t.setPhase(phase);
		t.setMilestone(milestone);
		t.setIdentifier(identifier);
		for(Integer i : daysToRemind){
			t.addDaysToRemind(i);
		}
		t.setReminderType(reminderType);
		
		return t;
	}
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("urgency", urgency.toString());
		j.put("comment", comment);
		JSONArray jAssignedPersons = new JSONArray();
		for(User u : assignedPersons){
			jAssignedPersons.put(u.getId());
		}
		j.put("assignedPersons", jAssignedPersons);
		j.put("percentageDone", percentageDone);
		j.put("startDate", startDate.getTime());
		j.put("endDate", endDate.getTime());
		j.put("manager", manager.getId());
		j.put("editor", editor.getId());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate==null? null:modifyDate.getTime());
		j.put("completeDate", completeDate==null? null:completeDate.getTime());
		j.put("phase", phase==null? null:phase.getId());
		j.put("milestone", milestone.getId());
		j.put("reminderType", reminderType.toString());
		j.put("identifier", identifier);
		JSONArray jDaysToRemind = new JSONArray();
		for(Integer d : daysToRemind){
			jDaysToRemind.put(d);
		}
		j.put("daysToRemind", jDaysToRemind);
		
		return j;
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
	public boolean hasAssignedPerson(User u){
		if(assignedPersons.contains(u)){
			return true;
		}
		return false;
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
