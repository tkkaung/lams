package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;

public class WebSessionData implements Serializable{
	
	private static final long serialVersionUID = 6756731266496606724L;
	public static final String EUREKA2_SESSION_ID_NAME = "celt.eureka2.ssid";
	
	
	private String id;
	private String username;
	private Date loginTime;
	private Date lastActiveTime;
	private String ip;
	
	
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public Date getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
	public Date getLastActiveTime() {
		return lastActiveTime;
	}
	public void setLastActiveTime(Date lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
}
