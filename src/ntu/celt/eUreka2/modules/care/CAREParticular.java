package ntu.celt.eUreka2.modules.care;

import java.io.Serializable;
import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class CAREParticular implements Serializable{
	private static final long serialVersionUID = 3339750244461930297L;
	
	private long id;
	private User user; 
	private Date cdate;
	private Date mdate;
	private String matricNumber;
	private int age;
	private String gender;
	private String highestEducation;
	private String maritalStatus;
	private String lastLeadershipApppointRank;
	private int yearsInLeadershipAppointment;
	private boolean hasExpInCrisisLeadership;
	private String briefDescriptionOfTheCrisis;
	
	
	public CAREParticular(){
		super();
		this.id = Util.generateLongID();
		this.setCdate(new Date());
	}
	public CAREParticular( User user, String matricNumber,
			int age, String gender, String highestEducation,
			String maritalStatus, String lastLeadershipApppointRank,
			int yearsInLeadershipAppointment, boolean hasExpInCrisisLeadership,
			String briefDescriptionOfTheCrisis) {
		super();
		this.id = Util.generateLongID();
		this.user = user;
		this.mdate = new Date();
		this.setCdate(new Date());
		this.matricNumber = matricNumber;
		this.age = age;
		this.gender = gender;
		this.highestEducation = highestEducation;
		this.maritalStatus = maritalStatus;
		this.lastLeadershipApppointRank = lastLeadershipApppointRank;
		this.yearsInLeadershipAppointment = yearsInLeadershipAppointment;
		this.hasExpInCrisisLeadership = hasExpInCrisisLeadership;
		this.briefDescriptionOfTheCrisis = briefDescriptionOfTheCrisis;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public Date getCdate() {
		return cdate;
	}
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public String getMatricNumber() {
		return matricNumber;
	}
	public void setMatricNumber(String matricNumber) {
		this.matricNumber = matricNumber;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getHighestEducation() {
		return this.highestEducation;
	}
	public void setHighestEducation(String highestEducation) {
		this.highestEducation = highestEducation;
	}
	public String getMaritalStatus() {
		return this.maritalStatus;
	}
	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getLastLeadershipApppointRank() {
		return lastLeadershipApppointRank;
	}
	public void setLastLeadershipApppointRank(String lastLeadershipApppointRank) {
		this.lastLeadershipApppointRank = lastLeadershipApppointRank;
	}
	public int getYearsInLeadershipAppointment() {
		return yearsInLeadershipAppointment;
	}
	public void setYearsInLeadershipAppointment(int yearsInLeadershipAppointment) {
		this.yearsInLeadershipAppointment = yearsInLeadershipAppointment;
	}
	public boolean isHasExpInCrisisLeadership() {
		return hasExpInCrisisLeadership;
	}
	public void setHasExpInCrisisLeadership(boolean hasExpInCrisisLeadership) {
		this.hasExpInCrisisLeadership = hasExpInCrisisLeadership;
	}
	public String getBriefDescriptionOfTheCrisis() {
		return briefDescriptionOfTheCrisis;
	}
	public void setBriefDescriptionOfTheCrisis(String briefDescriptionOfTheCrisis) {
		this.briefDescriptionOfTheCrisis = briefDescriptionOfTheCrisis;
	}
	
	
	
}
