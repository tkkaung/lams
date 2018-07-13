package ntu.celt.eUreka2.modules.search;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;

public class SearchResult {
	private SearchResultType type;  
	private String title;
	private String content;
	private Map<String, String> extraInfo = new LinkedHashMap<String, String>();
	private String byUser;
	private Project proj;
	private Date modifyTime;
	private String link;
	private String remarks;
	//private double priority;  //eg: how many time keywords appear in Title and/or Content, Title has higher weight than Content
	
	public SearchResultType getType() {
		return type;
	}
	public void setType(SearchResultType type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getByUser() {
		return byUser;
	}
	public void setByUser(String byUser) {
		this.byUser = byUser;
	}
	public Project getProj() {
		return proj;
	}
	public void setProj(Project proj) {
		this.proj = proj;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getModifyTimeDisplay(){
		return Util.formatDateTime(modifyTime);
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}
	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}
	
	
	
}
