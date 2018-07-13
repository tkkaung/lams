package ntu.celt.eUreka2.modules.budget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Transaction implements JSONable, Cloneable{
	private Long id;
	private String description;
	private String category;
	private Date transactDate;
	private Date createDate;
	private Date modifyDate;
	private User creator;
	private User editor;
	private double amount;
	private TransactionType type;
	private TransactionStatus status;
	private Budget budget;
	private String remarks;
	private List<TransactionAttachedFile> attachedFiles = new ArrayList<TransactionAttachedFile>();
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Date getTransactDate() {
		return transactDate;
	}
	public void setTransactDate(Date transactDate) {
		this.transactDate = transactDate;
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
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getAmount() {
		return amount;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public User getEditor() {
		return editor;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	public TransactionType getType() {
		return type;
	}
	public void setType(TransactionType type) {
		this.type = type;
	}
	public TransactionStatus getStatus() {
		return status;
	}
	public void setStatus(TransactionStatus status) {
		this.status = status;
	}
	public void setBudget(Budget budget) {
		this.budget = budget;
	}
	public Budget getBudget() {
		return budget;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getTransactDateDisplay(){
		return Util.formatDateTime(transactDate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public String getModifyDateDisplay(){
		return Util.formatDateTime(modifyDate);
	}
	public String getInFlowAmount(){
		if(TransactionType.IN_FLOW.equals(type))
			return Util.formatCurrency(amount);
		return null;
	}
	public String getOutFlowAmount(){
		if(TransactionType.OUT_FLOW.equals(type))
			return Util.formatCurrency(amount);
		return null;
	}
	/**
	 * positive value if type is IN_FLOW, negative value if type is OUT_FLOW
	 * @return
	 */
	public Double getFlowAmount(){
		if(TransactionType.IN_FLOW.equals(type))
			return amount;
		if(TransactionType.OUT_FLOW.equals(type))
			return -1 * amount;
		return null;
	}
	
	public boolean isDiffEditor(){
		if(editor==null || editor.equals(creator))
			return false;
		return true;
	}
	public void setAttachedFiles(List<TransactionAttachedFile> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}
	public List<TransactionAttachedFile> getAttachedFiles() {
		return attachedFiles;
	}
	public void addAttachFile(TransactionAttachedFile attachedFile){
		attachedFiles.add(attachedFile);
	}
	public void removeAttachFile(TransactionAttachedFile attachedFile){
		attachedFiles.remove(attachedFile);
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("description", description);
		j.put("transactDate", transactDate.getTime());
		j.put("createDate", createDate.getTime());
		j.put("modifyDate", modifyDate==null? null:modifyDate.getTime());
		j.put("creator", creator.getId());
		j.put("amount", amount);
		j.put("type", type.toString());
		j.put("status", status.toString());
		j.put("budget", budget.getId());
		j.put("remarks", remarks);
		j.put("editor", editor.getId());
		JSONArray atts = new JSONArray();
		for(TransactionAttachedFile att : attachedFiles){
			atts.put(att.toJSONObject());
		}
		j.put("attachedFiles", atts);
		
		return j;
	}
	
	public Transaction clone(){
		Transaction t = new Transaction();
		//t.setId(id)
		t.setDescription(description);
		t.setTransactDate(transactDate);
		t.setCreateDate(createDate);
		t.setModifyDate(modifyDate);
		t.setCreator(creator);
		t.setEditor(editor);
		t.setAmount(amount);
		t.setType(type);
		t.setStatus(status);
		t.setBudget(budget);
		t.setRemarks(remarks);
		for(TransactionAttachedFile a : attachedFiles){
			t.addAttachFile(a.clone());
		}
		
		return t;
	}
}
