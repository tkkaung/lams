package ntu.celt.eUreka2.modules.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Blog implements JSONable, Cloneable {
	@NonVisual
	private String id;
	private Project project;
	private User author;
	private String subject;
	private String content;
	private Date cdate;
	private Date mdate;
	private boolean shared;
	private BlogStatus status;
	private String ip;
	private String remarks;
	private List<BlogComment> comments = new ArrayList<BlogComment>();
	private List<BlogFile> attaches = new ArrayList<BlogFile>();
	private Set<String> tags = new HashSet<String>();
	
	public Blog clone(){
		Blog b = new Blog();
		//b.setId(id);
		b.setProject(project);
		b.setAuthor(author);
		b.setSubject(subject);
		b.setContent(content);
		b.setCdate(cdate);
		b.setMdate(mdate);
		b.setShared(shared);
		b.setStatus(status);
		b.setIp(ip);
		b.setRemarks(remarks);
		for(BlogComment bc : comments){
			b.addComment(bc.clone());
		}
		for(BlogFile bf : attaches){
			b.addFile(bf.clone());
		}
		for(String t : tags){
			b.addTags(t);
		}
		return b;
	}
	
	
	public Blog(){
		super();
		id = Util.generateUUID();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
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
	public boolean isShared() {
		return shared;
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public BlogStatus getStatus() {
		return status;
	}
	public void setStatus(BlogStatus status) {
		this.status = status;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public void setComments(List<BlogComment> comments) {
		this.comments = comments;
	}
	public List<BlogComment> getComments() {
		return comments;
	}
	public BlogComment getCommentById(int id) {
		BlogComment rtn = null;
		List<BlogComment> items = getComments();
		for (BlogComment c : items) {
			if (c.getId() == id) {
				rtn = c;
				break;
			}
		}
		return rtn;
	}
	public void addComment(BlogComment c) {
		comments.add(c);
	}
	public void removeComment(BlogComment c) {
		getComments().remove(c);
	}
	public int getCnum() {
		return comments.size();
	}
	public boolean getHasComment() {
		if (comments.size() == 0) {
			return false;
		}
		else {
			return true;
		}
	}
	public void setAttaches(List<BlogFile> attaches) {
		this.attaches = attaches;
	}
	public List<BlogFile> getAttaches() {
		return attaches;
	}
	public void addFile(BlogFile f) {
		attaches.add(f);
	}
	public void removeFile(BlogFile f) {
		getAttaches().remove(f);
	}
	
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	public Set<String> getTags() {
		return tags;
	}
	public void addTags(String tag){
		tags.add(tag);
	}
	public void removeTags(String tag){
		tags.remove(tag);
	}
	
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public boolean hasEdited(){
		if(cdate.equals(mdate))
			return false;
		return true;
	}
	
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("project", project.getId());
		j.put("author", author.getId());
		j.put("subject", subject);
		j.put("content", content);
		j.put("cdate", cdate.getTime());
		j.put("mdate", mdate==null? null : mdate.getTime());
		j.put("shared", shared);
		j.put("status", status.toString());
		j.put("ip", ip);
		j.put("remarks", remarks);
		JSONArray cmmts = new JSONArray();
		for(BlogComment cmmt : comments){
			cmmts.put(cmmt.toJSONObject());
		}
		j.put("comments", cmmts);
		JSONArray attchs = new JSONArray();
		for(BlogFile bf: attaches){
			attchs.put(bf.toJSONObject());
		}
		j.put("attaches", attchs);
		
		return j;
	}
}
