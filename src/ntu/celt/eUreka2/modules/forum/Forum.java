package ntu.celt.eUreka2.modules.forum;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Forum implements JSONable, Cloneable {
	@NonVisual
	private Long id;
	private String name;
	private String description;
	private Project project;
	private Set<Thread> threads = new HashSet<Thread>();  //only root Threads
	private User creator;
	private Date createDate;
	private Date modifyDate;
	private boolean anonymousPost = false;

	
	public Forum() {
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public void setThreads(Set<Thread> threads) {
		this.threads = threads;
	}
	public Set<Thread> getThreads() {
		return threads;
	}
	public void addThread(Thread thread){
		threads.add(thread);
	}
	public void removeThread(Thread thread){
		threads.remove(thread);
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getModifyDateDisplay(){
		return Util.formatDateTime(modifyDate);
	}
	public void setAnonymousPost(boolean anonymousPost) {
		this.anonymousPost = anonymousPost;
	}
	public boolean isAnonymousPost() {
		return anonymousPost;
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("description", description);
		j.put("project", project.getId());
		JSONArray ths = new JSONArray();
		for(Thread t : this.getThreads()){
			ths.put(t.toJSONObject());
		}
		j.put("threads", ths);
		j.put("creator", creator.getId());
		j.put("createDate", createDate.getTime());
		
		return j;
	}
	public Forum clone(){
		Forum f = new Forum();
		//f.setId(id)
		f.setName(name);
		f.setDescription(description);
		f.setProject(project);
		for(Thread th : threads){
			f.addThread(th.clone());
		}
		f.setCreator(creator);
		f.setCreateDate(createDate);
		f.setModifyDate(modifyDate);
		f.setAnonymousPost(anonymousPost);
		
		return f;
	}
}
