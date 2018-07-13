package ntu.celt.eUreka2.modules.announcement;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Announcement implements Cloneable, JSONable {
	@NonVisual
	private Long id;
	@Validate("required")
	private String subject;
	@Validate("required")
	private String content;
	private Date startDate;
	private Date endDate;
	private boolean urgent;
	private Date createDate;
	private Date modifyDate;
	private User creator;
	private Project project;
	private boolean enabled;
	private Set<User> hasReadUsers = new HashSet<User>();
	
	public Announcement(){
		super();
		id = Util.generateLongID();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
	public boolean isUrgent() {
		return urgent;
	}
	public void setUrgent(boolean urgent) {
		this.urgent = urgent;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
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
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public void setHasReadUsers(Set<User> hasReadUsers) {
		this.hasReadUsers = hasReadUsers;
	}
	public Set<User> getHasReadUsers() {
		return hasReadUsers;
	}
	public void addHasReadUsers(User user){
		hasReadUsers.add(user);
	}
	public void removeHasReadUsers(User user){
		hasReadUsers.remove(user);
	}
	public boolean hasRead(User user){
		if(hasReadUsers.contains(user))
			return true;
		return false;
	}
	
	
	public Announcement clone(){
    	Announcement a = new Announcement();
    	//a.setId(id);
    	a.setContent(content);
		a.setSubject(subject);
		a.setStartDate(startDate);
		a.setEndDate(endDate);
		a.setUrgent(urgent);
		a.setCreateDate(createDate);
		a.setModifyDate(modifyDate);
		a.setCreator(creator);
    	a.setProject(project);
    	a.setEnabled(enabled);
    	return a;
    }
	
    public JSONObject toJSONObject(){
    	JSONObject j = new JSONObject();
    	j.put("id", id);
    	j.put("subject", subject);
    	j.put("content", content);
    	j.put("startDate", startDate==null? null: startDate.getTime());
    	j.put("endDate", endDate==null? null: endDate.getTime());
    	j.put("urgent", urgent);
    	j.put("createDate", createDate.getTime());
    	j.put("modifyDate", modifyDate.getTime());
    	j.put("creator", creator.getId());
    	j.put("creatorUsername", creator.getUsername());
    	j.put("project", project.getId());
    	j.put("enabled", enabled);
    	return j;
    }
    
    /**
     * check if this announcement should be displayed, (check the startDate and endDate)
     */
    public boolean isDisplayed(){
		Date today = DateUtils.truncate(new Date(), Calendar.DATE);
		if(endDate!=null && today.after(endDate))
			return false;
		if(today.before(startDate))
			return false;
		return true;
    }
    public String getCreateDateDisplay(){
    	return Util.formatDateTime(createDate);
    }
    public String getModifyDateDisplay(){
    	return Util.formatDateTime(modifyDate);
    }
    public String getStartDateDisplay(){
    	return Util.formatDateTime(startDate, Config.getString(Config.FORMAT_TIME_PATTERN));
    }
    public String getEndDateDisplay(){
    	return Util.formatDateTime(endDate, Config.getString(Config.FORMAT_TIME_PATTERN));
    }
}
