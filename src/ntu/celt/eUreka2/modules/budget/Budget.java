package ntu.celt.eUreka2.modules.budget;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Budget implements JSONable , Cloneable{
	private Long id ;
	private Project project;
	private Date createDate;
	private Set<Transaction> transactions = new HashSet<Transaction>();
	private boolean active;
	private String remarks;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Set<Transaction> getTransactions() {
		return transactions;
	}
	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getRemarks() {
		return remarks;
	}
	public void addTransaction(Transaction transaction){
		this.transactions.add(transaction);
		transaction.setBudget(this);
	}
	public void removeTransaction(Transaction transaction){
		this.transactions.remove(transaction);
		transaction.setBudget(null);
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("project", project.getId());
		j.put("createDate", createDate.getTime());
		j.put("active", active);
		j.put("remarks", remarks);
		JSONArray jTrans = new JSONArray();
		for(Transaction t : transactions){
			jTrans.put(t.toJSONObject());
		}
		j.put("transactions", jTrans);
		return j;
	}
	
	public Budget clone(){
		Budget b = new Budget();
		//b.setId()
		b.setProject(project);
		b.setCreateDate(createDate);
		b.setActive(active);
		b.setRemarks(remarks);
		for(Transaction t : transactions){
			b.addTransaction(t.clone());
		}
		return b;
	}
}
