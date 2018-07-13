package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;

public class AnalyticUserBean implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7871443329316343085L;
	private Long id;
	private String username;
	private String url;
	private String pageName;
	private Date accessTime;
	private String ip;
	private String sessionID;
	
	public AnalyticUserBean() {
		super();
	}
	
	
	/**
	 * 
	 * @param username
	 * @param url
	 * @param pageName
	 * @param accessTime
	 * @param ip
	 * @param sessionID
	 */
	public AnalyticUserBean(String username, String url, String pageName,
			Date accessTime, String ip, String sessionID) {
		super();
		this.username = username;
		this.url = url;
		this.pageName = pageName;
		this.accessTime = accessTime;
		this.ip = ip;
		this.sessionID = sessionID;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPageName() {
		return pageName;
	}
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	public Date getAccessTime() {
		return accessTime;
	}
	public void setAccessTime(Date accessTime) {
		this.accessTime = accessTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	
	
	
	
}
