package ntu.celt.eUreka2.entities;

import java.util.Date;

public class SessionVisitStatistic {
	
	
	private Long id;
	private String username;
	private String url;
	private String pageName;
	private Date accessTime;
	private String ip;
	private String sessionID;
	private Project proj;
	private ProjRole projRole;
	private String moduleName;
	
	public SessionVisitStatistic() {
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
	 * @param proj
	 * @param projRole
	 * @param module
	 */
	public SessionVisitStatistic(String username, String url, String pageName
			,Date accessTime, String ip, String sessionID, Project proj
			, ProjRole projRole, String moduleName) {
		super();
		this.username = username;
		this.url = url;
		this.pageName = pageName;
		this.accessTime = accessTime;
		this.ip = ip;
		this.sessionID = sessionID;
		this.proj = proj;
		this.projRole = projRole;
		this.moduleName = moduleName;
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


	public Project getProj() {
		return proj;
	}


	public void setProj(Project proj) {
		this.proj = proj;
	}


	public ProjRole getProjRole() {
		return projRole;
	}


	public void setProjRole(ProjRole projRole) {
		this.projRole = projRole;
	}


	public String getModuleName() {
		return moduleName;
	}


	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

		
	
}
