package ntu.celt.eUreka2.modules.elog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class Elog implements Cloneable{
	@NonVisual
	private String id;
	private Project project;
	private User author;
	private User editor;
	private String subject;
	private String content;
	private Date cdate;
	private Date mdate;
	private ElogStatus status;
	private String ip;
	private String remarks;
	private List<ElogComment> comments = new ArrayList<ElogComment>();
	private List<ElogFile> files = new ArrayList<ElogFile>();
	private boolean published;
	
	public Elog clone(){
		Elog e = new Elog();
		//e.setId(id);
		e.setProject(project);
		e.setAuthor(author);
		e.setEditor(editor);
		e.setSubject(subject);
		e.setContent(content);
		e.setCdate(cdate);
		e.setMdate(mdate);
		e.setStatus(status);
		e.setIp(ip);
		e.setRemarks(remarks);
		e.setPublished(published);
		for(ElogComment c : comments){
			e.addComment(c.clone());
		}
		for(ElogFile f : files){
			e.addFile(f.clone());
		}
		
		return e;
	}
	
	public Elog(){
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
	public ElogStatus getStatus() {
		return status;
	}
	public void setStatus(ElogStatus status) {
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
	public void appendlnHtmlRemarks(String newLineString){
		if(remarks==null)
			remarks = newLineString;
		else
			remarks = remarks + "<br/>" + newLineString;
	}
	public void setComments(List<ElogComment> comments) {
		this.comments = comments;
	}
	public List<ElogComment> getComments() {
		return comments;
	}
	public ElogComment getCommentById(int id) {
		ElogComment rtn = null;
		List<ElogComment> items = getComments();
		for (ElogComment c : items) {
			if (c.getId() == id) {
				rtn = c;
				break;
			}
		}
		return rtn;
	}
	public void addComment(ElogComment c) {
		comments.add(c);
	}
	public void removeComment(ElogComment c) {
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
	public void setFiles(List<ElogFile> files) {
		this.files = files;
	}
	public List<ElogFile> getFiles() {
		return files;
	}
	public void addFile(ElogFile f) {
		files.add(f);
	}
	public void removeFile(ElogFile f) {
		files.remove(f);
	}
	
	public boolean isPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
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
	public boolean hasEditor(){
		if(editor==null)
			return false;
		if(editor.equals(author))
			return false;
		return true;
	}
	public User getEditor() {
		return editor;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	
}
