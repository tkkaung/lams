package ntu.celt.eUreka2.modules.blog;

import java.util.Date;

import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class BlogComment implements JSONable , Cloneable{
	private int id;
	private Blog blog;
	private User author;
	private String subject;
	private String content;
	private Date cdate;
	private Date mdate;
	private boolean disabled;
	private String ip;
	private String remarks;
	
	public BlogComment clone(){
		BlogComment c = new BlogComment();
	//	c.setId(id);
		c.setBlog(blog);
		c.setAuthor(author);
		c.setSubject(subject);
		c.setContent(content);
		c.setCdate(cdate);
		c.setMdate(mdate);
		c.setDisabled(disabled);
		c.setIp(ip);
		c.setRemarks(remarks);
		return c;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Blog getBlog() {
		return blog;
	}
	public void setBlog(Blog blog) {
		this.blog = blog;
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
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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
	
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	
	@Override
	public JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("blog", blog.getId() );
		j.put("author", author.getId() );
		j.put("subject",  subject);
		j.put("content", content );
		j.put("cdate",  cdate.getTime());
		j.put("mdate",  mdate==null? null:mdate.getTime());
		j.put("disabled",  disabled);
		j.put("ip", ip );
		j.put("remarks",  remarks);
		return j;
	}
}
