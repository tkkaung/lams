package ntu.celt.eUreka2.modules.ipsp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class IPSPSurveyUser {
	public static final int VALID_MINUTE_PER_QUESTION = 1800;
	
	private long id;
	private User assessor;
	private User assessee;
	private IPSPSurvey survey;
	private Date startAssessTime;
	private Date lastAssessTime;   
	private boolean finished;
	private int lastQuestionNum; // the last question index number that has been answer, equal to QuestionSet size when finished
	private Set<IQuestionScore> questionScores = new HashSet<IQuestionScore>();
	private int resetCount = 0;  //number of time that has been reset the answers
	
	private Double score1; 	//sum score
	private Double score2;
	private Double score3;
	private Double score4;
	
	
	public IPSPSurveyUser(){
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
	public IPSPSurvey getSurvey() {
		return survey;
	}
	public void setSurvey(IPSPSurvey survey) {
		this.survey = survey;
	}
	public void setQuestionScores(Set<IQuestionScore> questionScores) {
		this.questionScores = questionScores;
	}
	public Set<IQuestionScore> getQuestionScores() {
		return questionScores;
	}
	public void addQuestionScore(IQuestionScore qs){
		this.questionScores.add(qs);
	}
	public void removeQuestionScore(IQuestionScore qs){
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
	
	
	public Double getScore1() {
		return score1;
	}
	public void setScore1(Double score1) {
		this.score1 = score1;
	}
	public Double getScore2() {
		return score2;
	}
	public void setScore2(Double score2) {
		this.score2 = score2;
	}
	public Double getScore3() {
		return score3;
	}
	public void setScore3(Double score3) {
		this.score3 = score3;
	}
	public Double getScore4() {
		return score4;
	}
	public void setScore4(Double score4) {
		this.score4 = score4;
	}
	public void setScoreDim(double score, int dimID ) {
			switch (dimID){
				case 1:		score1 = score; break;
				case 2:		 score2 = score; break; 
				case 3:		 score3 = score; break; 
				case 4:		 score4 = score; break; 
			}
		
	}
	//remember to check for Score=-1  => no selected/not finished
	public Double getScoreDim(int dimID){
		switch (dimID){
			case 1:		return score1; 
			case 2:		return score2; 
			case 3:		return score3; 
			case 4:		return score4; 
		}
		return null;
	}
	
	
	
}
