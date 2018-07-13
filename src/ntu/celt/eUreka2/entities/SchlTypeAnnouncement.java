package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.json.JSONObject;
import org.hibernate.annotations.Entity;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

@Entity
public class SchlTypeAnnouncement implements Serializable, JSONable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7499869438639522323L;
	@NonVisual
	private int id;
	@Validate("required")
	private String subject;
	@Validate("required")
	private String content;
	private Date sdate;
	private Date edate;
	private Date cdate;
	private Date mdate;
	private boolean urgent;
	private User creator;
	private boolean enabled;
	private School school;
	private ProjType projType;
	private Set<User> hasReadUsers = new HashSet<User>();
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
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
	public Date getSdate() {
		return sdate;
	}
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getEdate() {
		return edate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public boolean isUrgent() {
		return urgent;
	}
	public void setUrgent(boolean urgent) {
		this.urgent = urgent;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getCdateDisplay(){
    	return Util.formatDateTime(cdate);
    }
    public String getMdateDisplay(){
    	return Util.formatDateTime(mdate);
    }
    public String getSdateDisplay(){
    	return Util.formatDateTime(sdate, Config.getString(Config.FORMAT_TIME_PATTERN));
    }
    public String getEdateDisplay(){
    	return Util.formatDateTime(edate, Config.getString(Config.FORMAT_TIME_PATTERN));
    }
	public void setSchool(School school) {
		this.school = school;
	}
	public School getSchool() {
		return school;
	}
	public void setProjType(ProjType projType) {
		this.projType = projType;
	}
	public ProjType getProjType() {
		return projType;
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
	@Override
	public JSONObject toJSONObject(){
    	JSONObject j = new JSONObject();
    	j.put("id", id);
    	j.put("subject", subject);
    	j.put("content", content);
    	j.put("startDate", sdate==null? null: sdate.getTime());
    	j.put("endDate", edate==null? null: edate.getTime());
    	j.put("urgent", urgent);
    	j.put("createDate", cdate.getTime());
    	j.put("modifyDate", mdate.getTime());
    	j.put("creator", creator.getId());
    	j.put("creatorUsername", creator.getUsername());
    	j.put("enabled", enabled);
    	j.put("school", school==null? null:school.getName());
    	j.put("type", projType==null? null:projType.getName());
    	return j;
    }
}
