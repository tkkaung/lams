package ntu.celt.eUreka2.modules.backuprestore.migrateEnities;

import java.util.Date;
import java.util.List;


public class MThread {
	private String message_id; 
	private String forum_id;
	private String parent_id;
	private String creatorUsername;
	private String creatorId;
	private String subject;
	private String body;
	private int views;
	private Date lastModifiedDate;
	private Date createDate;
	private Date lastReplyDate;
	private String category;
	private List<MThread> children;
	
	public String getMessage_id() {
		return message_id;
	}
	public void setMessage_id(String messageId) {
		message_id = messageId;
	}
	public String getForum_id() {
		return forum_id;
	}
	public void setForum_id(String forumId) {
		forum_id = forumId;
	}
	public String getParent_id() {
		return parent_id;
	}
	public void setParent_id(String parentId) {
		parent_id = parentId;
	}
	public String getCreatorUsername() {
		return creatorUsername;
	}
	public void setCreatorUsername(String creatorUsername) {
		this.creatorUsername = creatorUsername;
	}
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getLastReplyDate() {
		return lastReplyDate;
	}
	public void setLastReplyDate(Date lastReplyDate) {
		this.lastReplyDate = lastReplyDate;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setChildren(List<MThread> children) {
		this.children = children;
	}
	public List<MThread> getChildren() {
		return children;
	}
	
}
