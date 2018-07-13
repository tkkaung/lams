package ntu.celt.eUreka2.data;

import java.util.Date;

import ntu.celt.eUreka2.internal.Util;

public class LastUpdateItem implements Comparable<LastUpdateItem>{
	
	private Date time;
	private String title;
	private String url; //relative to webapp root
	private String type;  //can be null
	
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getTimeDisplay(){
		return Util.formatDateTime2(time);
	}
	
	@Override
	public int compareTo(LastUpdateItem o) {
		return o.getTime().compareTo(time);  //time descending
	}
	
	
	/*private User user;
	private String content;
	private String iconURL; //relative to webapp root
	private String remarks;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getIconURL() {
		return iconURL;
	}
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
	*/
}
