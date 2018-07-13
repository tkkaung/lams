package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;

import ntu.celt.eUreka2.data.ApproveItemStatus;
import ntu.celt.eUreka2.internal.Util;

import org.hibernate.annotations.Entity;

@Entity
public class ProjCAOExtraInfo implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3247944482921762263L;
	private int id;
	private String originalScope = "-";
	private String originalPrerequisite = "-";
	private String approvedScope; 
	private String approvedPrerequisite; 
	private String rejectedScope; 
	private String rejectedPrerequisite; 
	private String scope;
	private String prerequisite;
	private Date cdate;
	private Date mdate;
	private Project project;
	private ApproveItemStatus prevStatus = ApproveItemStatus.DRAFT;
	private ApproveItemStatus status = ApproveItemStatus.DRAFT;
	private User user;
	private User approver;
	private Date approveTime;
	private String logs;
	private String caoID; //e.g: NTU/2009/1/222/1504
	private String caoType; //IA, IO...
	//private Integer numOfAttach; //number of week for the attachment
	//private Boolean recordStatus;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOriginalScope() {
		return originalScope;
	}
	public void setOriginalScope(String originalScope) {
		this.originalScope = originalScope;
	}
	public String getOriginalPrerequisite() {
		return originalPrerequisite;
	}
	public void setOriginalPrerequisite(String originalPrerequisite) {
		this.originalPrerequisite = originalPrerequisite;
	}
	
	public String getApprovedScope() {
		return approvedScope;
	}
	public void setApprovedScope(String approvedScope) {
		this.approvedScope = approvedScope;
	}
	public String getApprovedPrerequisite() {
		return approvedPrerequisite;
	}
	public void setApprovedPrerequisite(String approvedPrerequisite) {
		this.approvedPrerequisite = approvedPrerequisite;
	}
	public String getRejectedScope() {
		return rejectedScope;
	}
	public void setRejectedScope(String rejectedScope) {
		this.rejectedScope = rejectedScope;
	}
	public String getRejectedPrerequisite() {
		return rejectedPrerequisite;
	}
	public void setRejectedPrerequisite(String rejectedPrerequisite) {
		this.rejectedPrerequisite = rejectedPrerequisite;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getPrerequisite() {
		return prerequisite;
	}
	public void setPrerequisite(String prerequisite) {
		this.prerequisite = prerequisite;
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
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public void setPrevStatus(ApproveItemStatus prevStatus) {
		this.prevStatus = prevStatus;
	}
	public ApproveItemStatus getPrevStatus() {
		return prevStatus;
	}
	public ApproveItemStatus getStatus() {
		return status;
	}
	public void setStatus(ApproveItemStatus status) {
		this.status = status;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public User getApprover() {
		return approver;
	}
	public void setApprover(User approver) {
		this.approver = approver;
	}
	public Date getApproveTime() {
		return approveTime;
	}
	public void setApproveTime(Date approveTime) {
		this.approveTime = approveTime;
	}
	public String getLogs() {
		return logs;
	}
	public void setLogs(String logs) {
		this.logs = logs;
	}
	public void appendlnHtmlLogs(String newLineString){
		if(logs==null)
			logs = newLineString;
		else
			logs = logs + "<br/>" + newLineString;
	}
	
	public boolean hasStatusChanged(){
		if(ApproveItemStatus.DRAFT.equals(status)
			&& ApproveItemStatus.DRAFT.equals(prevStatus))
			return false;
		return true;
	}
	public boolean isDraft(){
		if(ApproveItemStatus.DRAFT.equals(status))
			return true;
		return false;
	}
	public boolean isPending(){
		if(ApproveItemStatus.PENDING.equals(status))
			return true;
		return false;
	}
	public boolean isPrevRejected(){
		if(ApproveItemStatus.REJECTED.equals(prevStatus))
			return true;
		return false;
	}
	public String getScopeDisplay(){
		if(ApproveItemStatus.APPROVED.equals(status))
			return scope;
		if(approvedScope!=null)
			return approvedScope;
		return Util.nvl(originalScope);
	}
	public String getPrerequisiteDisplay(){
		if(ApproveItemStatus.APPROVED.equals(status))
			return prerequisite;
		if(approvedPrerequisite!=null)
			return approvedPrerequisite;
		return Util.nvl(originalPrerequisite);
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public String getApproveTimeDisplay(){
		return Util.formatDateTime(approveTime);
	}
	public void setCaoID(String caoID) {
		this.caoID = caoID;
	}
	public String getCaoID() {
		return caoID;
	}
	public void setCaoType(String caoType) {
		this.caoType = caoType;
	}
	public String getCaoType() {
		return caoType;
	}
	
	public ProjCAOExtraInfo clone(){
		ProjCAOExtraInfo c = new ProjCAOExtraInfo();
		c.setId(id);
		c.setOriginalScope(originalScope);
		c.setOriginalPrerequisite(originalPrerequisite);
		c.setApprovedScope(approvedScope);
		c.setApprovedPrerequisite(approvedPrerequisite);
		c.setRejectedScope(rejectedScope);
		c.setRejectedPrerequisite(rejectedPrerequisite);
		c.setScope(scope);
		c.setPrerequisite(prerequisite);
		c.setCdate(cdate);
		c.setMdate(mdate);
		c.setProject(project);
		c.setPrevStatus(prevStatus);
		c.setStatus(status);
		c.setUser(user);
		c.setApprover(approver);
		c.setApproveTime(approveTime);
		c.setLogs(logs);
		c.setCaoID(caoID);
		c.setCaoType(caoType);
		
		return c;
	}
	
}
