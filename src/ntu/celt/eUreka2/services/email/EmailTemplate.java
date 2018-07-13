package ntu.celt.eUreka2.services.email;

import java.util.Date;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class EmailTemplate {
	@NonVisual
	private Long id;
	private String type;
	private String language;
	private String description;
	private String subject;
	private String content;
	private Date modifyDate;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
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
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public String getModifyDateDisplay(){
		return Util.formatDateTime(modifyDate);
	}
	public String getContentAsText(){
		return Util.stripTags(content);
	}
	
}
