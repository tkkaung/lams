package ntu.celt.eUreka2.entities;

import java.util.Date;

import ntu.celt.eUreka2.internal.Util;

public class AuditTrail {
	private String id;
	private Project proj;
	private String moduleName;
	private String objID;
	private String actionName;
	private Date actionTime;
	private User actionUser;
	private String prevValue;
	private String newValue;
	
	
	//default actions
	public static final String ACTION_ADD = "Add";
	public static final String ACTION_EDIT = "Edit";
	public static final String ACTION_DELETE = "Delete";
	
	
	
	public AuditTrail(){
		super();
		id = Util.generateUUID();
	}
	public AuditTrail(Project proj, String moduleName, String objID, String actionName, User user){
		super();
		id = Util.generateUUID();
		this.proj = proj;
		this.moduleName = moduleName;
		this.objID = objID;
		this.actionName = actionName;
		this.prevValue = "";
		this.newValue = "";
		this.actionTime = new Date();
		this.actionUser = user;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getObjID() {
		return objID;
	}
	public void setObjID(String objID) {
		this.objID = objID;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getPrevValue() {
		return prevValue;
	}
	public void setPrevValue(String prevValue) {
		this.prevValue = prevValue;
	}
	public void appendPrevValue(String prevValue) {
		this.prevValue += prevValue + System.lineSeparator();
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public void appendNewValue(String newValue) {
		this.newValue += newValue + System.lineSeparator();
	}
	
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}
	public Date getActionTime() {
		return actionTime;
	}
	public String getActionTimeDisplay(){
		return Util.formatDateTime2(actionTime);
	}
	public void setActionUser(User actionUser) {
		this.actionUser = actionUser;
	}
	public User getActionUser() {
		return actionUser;
	}
	public void setProj(Project proj) {
		this.proj = proj;
	}
	public Project getProj() {
		return proj;
	}
	
	
	
}
