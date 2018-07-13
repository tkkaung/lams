package ntu.celt.eUreka2.entities;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

@Entity
public class SysAnnouncement {
	
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
}
