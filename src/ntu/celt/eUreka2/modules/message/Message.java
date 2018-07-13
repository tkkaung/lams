package ntu.celt.eUreka2.modules.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Message implements Cloneable, JSONable {
	private Long id;
	private String subject;
	private String content;
	private Date createDate;
	private Date sendDate;
	private User sender;
	private User owner;
	private Set<User> recipients = new HashSet<User>();
	private boolean flag;
	private boolean hasRead;
	private MessageType type;
	private Set<String> externalEmails = new HashSet<String>();
	private Date deleteDate;
	private Project proj;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getSendDate() {
		return sendDate;
	}
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}
	
	public User getSender() {
		return sender;
	}
	public void setSender(User sender) {
		this.sender = sender;
	}
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public Set<User> getRecipients() {
		return recipients;
	}
	public void setRecipients(Set<User> recipients) {
		this.recipients = recipients;
	}
	public void addRecipients(User u){
		recipients.add(u);
	}
	public void removeRecipients(User u){
		recipients.remove(u);
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	
	public void setHasRead(boolean hasRead) {
		this.hasRead = hasRead;
	}
	public boolean isHasRead() {
		return hasRead;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public MessageType getType() {
		return type;
	}
	public void setExternalEmails(Set<String> externalEmails) {
		this.externalEmails = externalEmails;
	}
	public Set<String> getExternalEmails() {
		return externalEmails;
	}
	
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setProj(Project proj) {
		this.proj = proj;
	}
	public Project getProj() {
		return proj;
	}
	public void addExternalEmail(String email){
		this.externalEmails.add(email);
	}
	public void removeExternalEmail(String email){
		this.externalEmails.remove(email);
	}
	
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public String getSendDateDisplay(){
		return Util.formatDateTime(sendDate);
	}
	public String getDeleteDateDisplay(){
		return Util.formatDateTime(deleteDate);
	}
	
	@Override
	public Message clone(){
		Message m = new Message();
		m.setSubject(subject);
		m.setContent(content);
		m.setCreateDate(createDate);
		m.setSendDate(sendDate);
		m.setSender(sender);
		m.setOwner(owner);
		for(User u: recipients){
			m.addRecipients(u);
		}
		for(String email: externalEmails){
			m.addExternalEmail(email);
		}
		m.setFlag(flag);
		m.setHasRead(hasRead);
		m.setType(type);
		m.setDeleteDate(deleteDate);
		m.setProj(proj);
		
		return m;
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("subject", subject);
		j.put("content", content);
		j.put("createDate", createDate.getTime());
		j.put("sendDate", sendDate.getTime());
		j.put("sender", sender.getId());
		j.put("owner", owner.getId());
		JSONArray jRecipts = new JSONArray();
		for(User u: recipients){
			jRecipts.put(u.getId());
		}
		j.put("recipients", jRecipts);
		JSONArray jExtEmails = new JSONArray();
		for(String email: externalEmails){
			jExtEmails.put(email);
		}
		j.put("externalEmails", jExtEmails);
		j.put("flag", flag);
		j.put("hasRead", hasRead);
		j.put("type", type.toString());
		j.put("deleteDate", deleteDate==null? null:deleteDate.getTime());
		j.put("proj", proj.getId());
		
		return j;
	}
	
	
	public List<String> getToOtherUsersAsStringIds(){
		List<String> list = new ArrayList<String>();
		for(User u: recipients){
			list.add(Integer.toString(u.getId()));
		}
		return list;
	}
	public String getToExternalEmailAsString(){
		String SEPARATE_STRING = ", ";
		StringBuffer extEmails = new StringBuffer();
		for(String email : externalEmails){
			extEmails.append(email + SEPARATE_STRING);
		}
		if(extEmails.length()>0)
			return Util.removeLastSeparator(extEmails.toString() , SEPARATE_STRING);
		
		return null;
	}
	public String getRecipientsDisplay(){
		String SEPARATE_STRING = ", ";
		StringBuffer str = new StringBuffer();
		for(User u: recipients){
			str.append(u.getDisplayName()+ SEPARATE_STRING);
		}
		for(String email : externalEmails){
			str.append(email + SEPARATE_STRING);
		}
		
		if(str.length()>0)
			return Util.removeLastSeparator(str.toString(), SEPARATE_STRING);
		return null;
	}
	
	 
}