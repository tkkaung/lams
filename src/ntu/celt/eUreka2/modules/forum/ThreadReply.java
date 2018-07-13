package ntu.celt.eUreka2.modules.forum;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class ThreadReply implements JSONable, Cloneable{
	@NonVisual
	private Long id;
	private String name;
	private String message;
	private User author;
	private Date createDate;
	private Date modifyDate;
	private ThreadReply parent;  
	private Thread thread;
	private Set<ThreadReply> children = new HashSet<ThreadReply>();
	private Set<ThreadReplyUser> threadReplyUsers = new HashSet<ThreadReplyUser>();
	private ThreadType type = ThreadType.DISCUSS_TOPIC; //default vaule
	private List<ForumAttachedFile> attachedFiles = new ArrayList<ForumAttachedFile>();
	@NonVisual
	private boolean anonymous = false;   //whether it should not display real author name
	
	public ThreadReply() {
		super();
		id = Util.generateLongID();
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
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
	public ThreadReply getParent() {
		return parent;
	}
	public void setParent(ThreadReply parent) {
		this.parent = parent;
	}
	public Thread getThread(){
		return thread;
	}
	public void setThread(Thread thread) {
		this.thread = thread;
	}
	public Set<ThreadReply> getChildren() {
		return children;
	}
	public void setChildren(Set<ThreadReply> children) {
		this.children = children;
	}
	public void addChildren(ThreadReply child){
		this.children.add(child);
	}
	public void removeChildren(ThreadReply child){
		this.children.remove(child);
	}
	public List<ThreadReply> getDescendants(List<ThreadReply> list){
		for(ThreadReply child : children){
			list.add(child);
			list = child.getDescendants(list);
		}
		return list;
	}
	
	public void setType(ThreadType type) {
		this.type = type;
	}
	public ThreadType getType() {
		return type;
	}
	public Set<ThreadReplyUser> getThreadReplyUsers() {
		return threadReplyUsers;
	}
	public void setThreadReplyUsers(Set<ThreadReplyUser> threadReplyUsers) {
		this.threadReplyUsers = threadReplyUsers;
	}
	public void addThreadReplyUsers(ThreadReplyUser tu){
		threadReplyUsers.add(tu);
	}
	public void removeThreadReplyUsers(ThreadReplyUser tu){
		threadReplyUsers.remove(tu);
	}
	public ThreadReplyUser getThreadReplyUser(User user){
		for(ThreadReplyUser tu : threadReplyUsers){
			if(tu.getUser().equals(user))
				return tu;
		}
		return null;
	}
	
	public String getAuthorDisplayName(){
		if(anonymous)
			return PredefinedNames.ANONYMOUS;
		return author.getDisplayName();
	}
	public void setAttachedFiles(List<ForumAttachedFile> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}
	public List<ForumAttachedFile> getAttachedFiles() {
		return attachedFiles;
	}
	public void addAttachFile(ForumAttachedFile attachedFile){
		attachedFiles.add(attachedFile);
	}
	public void removeAttachFile(ForumAttachedFile attachedFile){
		attachedFiles.remove(attachedFile);
	}
	
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public String getModifyDateDisplay(){
		return Util.formatDateTime(modifyDate);
	}
	public boolean hasEdited(){
		if(createDate.equals(modifyDate))
			return false;
		return true;
	}
	public boolean isAnonymous() {
		return anonymous;
	}
	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	
	@Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof ThreadReply) && id != null 
    			&& id.equals(((ThreadReply) obj).getId());
    }
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("message", message);
		j.put("author", author.getId());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate.getTime());
		j.put("parent", parent==null? null : parent.getId());  //not parent.toJSONObject because it will cause cyclic loop
		j.put("thread", thread.getId());
		JSONArray chs = new JSONArray();
		for(ThreadReply ch : children){
			chs.put(ch.toJSONObject());
		}
		j.put("children", chs);
		JSONArray tus = new JSONArray();
		for(ThreadReplyUser tu : threadReplyUsers){
			JSONObject jtu = new JSONObject();
			jtu.put("rate", (tu.getRate()==null? null : tu.getRate().toString()));
			jtu.put("user", tu.getUser().getId());
			tus.put(jtu);
		}
		j.put("threadReplyUsers", tus);
		j.put("type", type.toString());
		JSONArray atts = new JSONArray();
		for(ForumAttachedFile att : attachedFiles){
			atts.put(att.toJSONObject());
		}
		j.put("attachedFiles", atts);
		j.put("anonymous", anonymous);
		
		return j;
	}
	
	public ThreadReply clone(){
		ThreadReply tr = new ThreadReply();
		//t.setId(id);
		tr.setName(name);
		tr.setMessage(message);
		tr.setAuthor(author);
		tr.setCreateDate(createDate);
		tr.setModifyDate(modifyDate);
		tr.setParent(parent);
		for(ThreadReply trC: children){
			tr.addChildren(trC.clone());
		}
		for(ThreadReplyUser tu : threadReplyUsers){
			tr.addThreadReplyUsers(tu.clone());
		}
		tr.setType(type);
		for(ForumAttachedFile a : attachedFiles){
			tr.addAttachFile(a.clone());
		}
		tr.setAnonymous(anonymous);
		
		return tr;
	}
	
}
