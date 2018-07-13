package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.Date;


import org.hibernate.annotations.Entity;

@Entity
public class ProjFYPExtraInfo implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5887750823432922369L;
	private int id;
	private Project project;
	private String fypID;  
	private String fypNo; 
	private String acadYear;
	private String acadSem;
	private Date examDateTime ;
	private String examVenue ;
	
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	
	public String getFypID() {
		return fypID;
	}
	public void setFypID(String fypID) {
		this.fypID = fypID;
	}
	public String getFypNo() {
		return fypNo;
	}
	public void setFypNo(String fypNo) {
		this.fypNo = fypNo;
	}
	public String getAcadYear() {
		return acadYear;
	}
	public void setAcadYear(String acadYear) {
		this.acadYear = acadYear;
	}
	public String getAcadSem() {
		return acadSem;
	}
	public void setAcadSem(String acadSem) {
		this.acadSem = acadSem;
	}
	public Date getExamDateTime() {
		return examDateTime;
	}
	public void setExamDateTime(Date examDateTime) {
		this.examDateTime = examDateTime;
	}
	public String getExamVenue() {
		return examVenue;
	}
	public void setExamVenue(String examVenue) {
		this.examVenue = examVenue;
	}
	
	public ProjFYPExtraInfo clone(){
		ProjFYPExtraInfo f = new ProjFYPExtraInfo();
		f.setId(id);
		f.setProject(project);
		f.setFypID(fypID);
		f.setFypNo(fypNo);
		f.setAcadYear(acadYear);
		f.setAcadSem(acadSem);
		f.setExamDateTime(examDateTime);
		f.setExamVenue(examVenue);
		return f;
	}
	
}
