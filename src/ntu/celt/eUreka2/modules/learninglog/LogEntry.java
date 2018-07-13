package ntu.celt.eUreka2.modules.learninglog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

/**
 * 
 * @author Somsak
 * 
 * this class is used as "Personal Note", and "Reflection"
 */
@Entity
public class LogEntry implements Cloneable {
	@NonVisual
	private long id;
	private String title;
	private String content;
	private String type;
	private User creator;
	private Project project;
	private Date cdate;
	private Date mdate;
	private List<LLogFile> files = new ArrayList<LLogFile>();
	
	private boolean shared;
	private Long forumThreadId;
	private String blogId;
	private String elogId;
	
	public static final String TYPE_FORUM_REFLECTION = "Forum Reflection";
	public static final String TYPE_BLOG_REFLECTION = "Blog Reflection";
	public static final String TYPE_ELOG_REFLECTION = "eLog Reflection";
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public Project getProject() {
		return project;
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
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	
	public void setFiles(List<LLogFile> files) {
		this.files = files;
	}
	public List<LLogFile> getFiles() {
		return files;
	}
	public void addFile(LLogFile file) {
		files.add(file);
	}
	public void removeFile(LLogFile file) {
		files.remove(file);
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public boolean isShared() {
		return shared;
	}
	public void setForumThreadId(Long forumThreadId) {
		this.forumThreadId = forumThreadId;
	}
	public Long getForumThreadId() {
		return forumThreadId;
	}
	public void setBlogId(String blogId) {
		this.blogId = blogId;
	}
	public String getBlogId() {
		return blogId;
	}
	public void setElogId(String elogId) {
		this.elogId = elogId;
	}
	public String getElogId() {
		return elogId;
	}
	
	public LogEntry clone(){
		LogEntry l = new LogEntry();
		//l.setId(id);
		l.setTitle(title);
		l.setContent(content);
		l.setType(type);
		l.setCreator(creator);
		l.setProject(project);
		l.setCdate(cdate);
		l.setMdate(mdate);
		for(LLogFile f : files){
			l.addFile(f.clone());
		}
		l.setShared(shared);
		l.setForumThreadId(forumThreadId);
		l.setBlogId(blogId);
		l.setElogId(elogId);
		
		return l;
	}
	
}
