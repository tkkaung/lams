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

public class Thread implements JSONable, Cloneable{
	@NonVisual
	private Long id;
	private String name;
	private String message;
	private User author;
	private Date createDate;
	private Date modifyDate;
	private Set<ThreadReply> replies = new HashSet<ThreadReply>();  //all replies, not just 1 level down
	private Set<ThreadUser> threadUsers = new HashSet<ThreadUser>();
	private Forum forum; 
	private ThreadType type = ThreadType.ASK_A_QUESTION; //default type vaule
	private List<ForumAttachedFile> attachedFiles = new ArrayList<ForumAttachedFile>();
	@NonVisual
	private boolean anonymous = false;   //whether it should not display real author name
	private boolean pinned = false; //whether it should always be listed first, (i.e: important Thread)
	
	
	
	public Thread() {
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
	public Set<ThreadReply> getReplies() {
		return replies;
	}
	public void setReplies(Set<ThreadReply> replies) {
		this.replies = replies;
	}
	public void addReplies(ThreadReply reply){
		this.replies.add(reply);
	}
	public void removeReplies(ThreadReply reply){
		this.replies.remove(reply);
	}
	public List<ThreadReply> getRepliesOrdered(){
		List<ThreadReply> list = new ArrayList<ThreadReply>();
		for(ThreadReply thR : replies){
			if(thR.getParent()==null){ //top level, i.e: directly under this thread
				list.add(thR);
				thR.getDescendants(list); //recursively get the descendants
			}
		}
		return list;
	}
	
	public void setForum(Forum forum) {
		this.forum = forum;
	}
	public Forum getForum() {
		return forum;
	}
	public void setType(ThreadType type) {
		this.type = type;
	}
	public ThreadType getType() {
		return type;
	}
	public Set<ThreadUser> getThreadUsers() {
		return threadUsers;
	}
	public void setThreadUsers(Set<ThreadUser> threadUsers) {
		this.threadUsers = threadUsers;
	}
	public void addThreadUsers(ThreadUser tu){
		threadUsers.add(tu);
	}
	public void removeThreadUsers(ThreadUser tu){
		threadUsers.remove(tu);
	}
	public ThreadUser getThreadUser(User user){
		for(ThreadUser tu : threadUsers){
			if(tu.getUser().equals(user))
				return tu;
		}
		return null;
	}
	
	/*
	//it is faster to query these from database
	public int getTotalView() {
		int total = 0;
		for(ThreadUser tu : threadUsers){
			total += tu.getNumView();
		}
		return total;
	}
	public int getTotalRateByType(RateType rate){
		return getUsersByRateType(rate).size();
	}
	public List<User> getUsersByRateType(RateType rate){
		List<User> uList = new ArrayList<User>();
		for(ThreadUser tu : threadUsers){
			if(rate.equals(tu.getRate()))
				uList.add(tu.getUser());
		}
		return uList;
	}
	*/
	
	
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
	
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
	public boolean isPinned() {
		return pinned;
	}
	
	
	@Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof Thread) && id != null 
    			&& id.equals(((Thread) obj).getId());
    }
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("message", message);
		j.put("author", author.getId());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate.getTime());
		JSONArray thRs = new JSONArray();
		for(ThreadReply thR : replies){
			if(thR.getParent()==null){
				thRs.put(thR.toJSONObject());
			}
		}
		j.put("replies", thRs);
		JSONArray tus = new JSONArray();
		for(ThreadUser tu : threadUsers){
			JSONObject jtu = new JSONObject();
			jtu.put("numView", tu.getNumView());
			jtu.put("rate", (tu.getRate()==null? null : tu.getRate().toString()));
			jtu.put("user", tu.getUser().getId());
			jtu.put("flag", tu.isFlagged());
			jtu.put("read", tu.isHasRead());
			tus.put(jtu);
		}
		j.put("threadUsers", tus);
		j.put("forum", forum.getId());
		j.put("type", type.toString());
		JSONArray atts = new JSONArray();
		for(ForumAttachedFile att : attachedFiles){
			atts.put(att.toJSONObject());
		}
		j.put("attachedFiles", atts);
		j.put("anonymous", anonymous);
		
		//TODO j.put("reflections", value);
		
		return j;
	}
	
	public Thread clone(){
		Thread t = new Thread();
		//t.setId(id);
		t.setName(name);
		t.setMessage(message);
		t.setAuthor(author);
		t.setCreateDate(createDate);
		t.setModifyDate(modifyDate);
		for(ThreadReply tr: replies){
			if(tr.getParent()==null){ //top level, i.e: directly under this thread
				t.addReplies(tr.clone());
			}
		}
		for(ThreadUser tu : threadUsers){
			t.addThreadUsers(tu.clone());
		}
		t.setForum(forum);
		t.setType(type);
		for(ForumAttachedFile a : attachedFiles){
			t.addAttachFile(a.clone());
		}
		t.setAnonymous(anonymous);
		t.setPinned(pinned);
		return t;
	}
	
}
