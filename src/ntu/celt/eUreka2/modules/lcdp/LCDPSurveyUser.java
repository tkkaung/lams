package ntu.celt.eUreka2.modules.lcdp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class LCDPSurveyUser {
	public static final int VALID_MINUTE_PER_QUESTION = 180;
	
	private long id;
	private User assessor;
	private User assessee;
	private LCDPSurvey survey;
	private Date startAssessTime;
	private Date lastAssessTime;   
	private boolean finished;
	private int lastQuestionNum; // the last question index number that has been answer, equal to QuestionSet size when finished
	private Set<PQuestionScore> questionScores = new HashSet<PQuestionScore>();
	private int resetCount = 0;  //number of time that has been reset the answers
	
	
	public LCDPSurveyUser(){
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
	public LCDPSurvey getSurvey() {
		return survey;
	}
	public void setSurvey(LCDPSurvey survey) {
		this.survey = survey;
	}
	public void setQuestionScores(Set<PQuestionScore> questionScores) {
		this.questionScores = questionScores;
	}
	public Set<PQuestionScore> getQuestionScores() {
		return questionScores;
	}
	public void addQuestionScore(PQuestionScore qs){
		this.questionScores.add(qs);
	}
	public void removeQuestionScore(PQuestionScore qs){
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
