package ntu.celt.eUreka2.modules.teameffectiveness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;

import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class TESurvey implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6018334449607865799L;
	@NonVisual
	private long id;
	private String name; 
	private Project project;
	private User creator;
	private User editor;
	private Date cdate;
	private Date mdate;
	private Date sdate;
	private Date edate;
	private int orderNumber;
	private Boolean released;
	
	private TEQuestionSet questionSet;		//just for reference, because criterias is loaded from this
	private String questionSetName;
	private List<TEQuestion> questions = new ArrayList<TEQuestion>();
	private Set<TESurveyUser> TEUsers = new HashSet<TESurveyUser>();
	private Set<Integer> daysToRemind = new HashSet<Integer>();
	
	
	public TESurvey(){
		super();
		id =  Util.generateLongID();
	}
	public TESurvey(String name, Project proj, User creator, int orderNumber,Date cdate){
		super();
		id =  Util.generateLongID();
		this.name = name;
		this.project = proj;
		this.creator = creator;
		this.orderNumber = orderNumber;
		this.cdate = cdate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	public User getEditor() {
		return editor;
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
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getSdate() {
		return sdate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public Date getEdate() {
		return edate;
	}
	

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	
	
	
	
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public String getSdateDisplay(){
		return Util.formatDateTime(sdate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getEdateDisplay(){
		return Util.formatDateTime(edate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	
	public boolean isDiffEditor(){
		if(editor==null || editor.equals(creator))
			return false;
		return true;
	}



	public TEQuestionSet getQuestionSet() {
		return questionSet;
	}
	public void setQuestionSet(TEQuestionSet questionSet) {
		this.questionSet = questionSet;
	}
	public String getQuestionSetName() {
		return questionSetName;
	}
	public void setQuestionSetName(String questionSetName) {
		this.questionSetName = questionSetName;
	}
	public List<TEQuestion> getQuestions() {
		return questions;
	}
	public void setQuestions(List<TEQuestion> questions) {
		this.questions = questions;
	}
	public void addQuestions(TEQuestion question){
		questions.add(question);
	}
	public void removeQuestions(TEQuestion question){
		questions.remove(question);
	}
	public int getTotalQuestions(){
		return questions.size();
	}
	
	
	public Set<TESurveyUser> getTEUsers() {
		return TEUsers;
	}
	public void setTEUsers(Set<TESurveyUser> TEUsers) {
		this.TEUsers = TEUsers;
	}
	
	public void addTEUsers(TESurveyUser TEUser){
		TEUsers.add(TEUser);
	}
	public void removeTEUsers(TESurveyUser TEUser){
		TEUsers.remove(TEUser);
	}
	
	
	public void setDaysToRemind(Set<Integer> daysEarlierToRemind) {
		this.daysToRemind = daysEarlierToRemind;
	}
	public Set<Integer> getDaysToRemind() {
		return daysToRemind;
	}
	public void addDaysToRemind(Integer numDay){
		daysToRemind.add(numDay);
	}
	public void removeDaysToRemind(Integer numDay){
		daysToRemind.remove(numDay);
	}
	
	
	// The need for a equal() method is discussed at http://www.hibernate.org/109.html
    @Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof Project)  
    			&& (id == ((TESurvey) obj).getId());
    	
    }
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return String.valueOf(id).hashCode();
	}
	public void setReleased(Boolean released) {
		this.released = released;
	}
	public Boolean getReleased() {
		if(released == null)
			return false;
		return released;
	}
}
