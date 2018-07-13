package ntu.celt.eUreka2.modules.profiling;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class ProfileUser {
	public static final int VALID_MINUTE_PER_QUESTION = 180;
	
	private long id;
	private User assessor;
	private User assessee;
	private Profiling profile;
	private Date startAssessTime;
	private Date lastAssessTime;   
	private boolean finished;
	private int lastQuestionNum; // the last question index number that has been answer, equal to QuestionSet size when finished
	private Set<QuestionScore> questionScores = new HashSet<QuestionScore>();
	private int resetCount = 0;  //number of time that has been reset the answers
	
	
	public ProfileUser(){
		super();
		id =  Util.generateLongID();
		finished = false;
		lastQuestionNum = 0;
		setResetCount(0);
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public User getAssessor() {
		return assessor;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public User getAssessee() {
		return assessee;
	}
	public void setAssessee(User assessee) {
		this.assessee = assessee;
	}
	
	
	
	public Date getStartAssessTime() {
		return startAssessTime;
	}
	public void setStartAssessTime(Date startAssessTime) {
		this.startAssessTime = startAssessTime;
	}
	public Date getLastAssessTime() {
		return lastAssessTime;
	}
	public void setLastAssessTime(Date lastAssessTime) {
		this.lastAssessTime = lastAssessTime;
	}
	
	public String getStartAssessTimeDisplay(){
		return Util.formatDateTime(startAssessTime, Config.getString(Config.FORMAT_TIME_PATTERN));
	}
	public String getLastAssessTimeDisplay(){
		return Util.formatDateTime(lastAssessTime, Config.getString(Config.FORMAT_TIME_PATTERN));
	}
	
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public int getLastQuestionNum() {
		return lastQuestionNum;
	}
	public void setLastQuestionNum(int lastQuestionNum) {
		this.lastQuestionNum = lastQuestionNum;
	}
	public Profiling getProfile() {
		return profile;
	}
	public void setProfile(Profiling profile) {
		this.profile = profile;
	}
	public void setQuestionScores(Set<QuestionScore> questionScores) {
		this.questionScores = questionScores;
	}
	public Set<QuestionScore> getQuestionScores() {
		return questionScores;
	}
	public void addQuestionScore(QuestionScore qs){
		this.questionScores.add(qs);
	}
	public void removeQuestionScore(QuestionScore qs){
		this.questionScores.remove(qs);
	}
	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}
	public int getResetCount() {
		return resetCount;
	}
	public void increaseResetCount(){
		this.resetCount++;
	}
	public void increaseLastQuestionNum(){
		this.lastQuestionNum++;
	}
	
	
	
}
