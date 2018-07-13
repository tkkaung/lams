package ntu.celt.eUreka2.modules.care;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;

public class CARESurveyUser {
	public static final int VALID_MINUTE_PER_QUESTION = 1800;
	
	private long id;
	private User assessor;
	private User assessee;
	private CARESurvey survey;
	private Date startAssessTime;
	private Date lastAssessTime;   
	private boolean finished;
	private int lastQuestionNum; // the last question index number that has been answer, equal to QuestionSet size when finished
	private Set<CQuestionScore> questionScores = new HashSet<CQuestionScore>();
	private int resetCount = 0;  //number of time that has been reset the answers
	
	private Double score1; 	//average score
	private Double score2;
	private Double score3;
	private Double score4;
	private Double score5;
	private Double score6;
	private Double score7;
	private Double score8;
	private Double score9;
	private Double score10;
	private Double score11;
	private Double score12;
	private Double score13;
	private Double score14;
	private Double score15;
	
	
	public CARESurveyUser(){
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
	public CARESurvey getSurvey() {
		return survey;
	}
	public void setSurvey(CARESurvey survey) {
		this.survey = survey;
	}
	public void setQuestionScores(Set<CQuestionScore> questionScores) {
		this.questionScores = questionScores;
	}
	public Set<CQuestionScore> getQuestionScores() {
		return questionScores;
	}
	public void addQuestionScore(CQuestionScore qs){
		this.questionScores.add(qs);
	}
	public void removeQuestionScore(CQuestionScore qs){
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
	public Double getScore5() {
		return score5;
	}
	public void setScore5(Double score5) {
		this.score5 = score5;
	}
	public Double getScore6() {
		return score6;
	}
	public void setScore6(Double score6) {
		this.score6 = score6;
	}
	public Double getScore7() {
		return score7;
	}
	public void setScore7(Double score7) {
		this.score7 = score7;
	}
	public Double getScore8() {
		return score8;
	}
	public void setScore8(Double score8) {
		this.score8 = score8;
	}
	public Double getScore9() {
		return score9;
	}
	public void setScore9(Double score9) {
		this.score9 = score9;
	}
	public Double getScore10() {
		return score10;
	}
	public void setScore10(Double score10) {
		this.score10 = score10;
	}
	public Double getScore11() {
		return score11;
	}
	public void setScore11(Double score11) {
		this.score11 = score11;
	}
	public Double getScore12() {
		return score12;
	}
	public void setScore12(Double score12) {
		this.score12 = score12;
	}
	public Double getScore13() {
		return score13;
	}
	public void setScore13(Double score13) {
		this.score13 = score13;
	}
	public Double getScore14() {
		return score14;
	}
	public void setScore14(Double score14) {
		this.score14 = score14;
	}
	public Double getScore15() {
		return score15;
	}
	public void setScore15(Double score15) {
		this.score15 = score15;
	}
	public void setScoreDim(double score, int dimID ) {
			switch (dimID){
				case 1:		score1 = score; break;
				case 2:		 score2 = score; break; 
				case 3:		 score3 = score; break; 
				case 4:		 score4 = score; break; 
				case 5:		 score5 = score; break; 
				case 6:		 score6 = score; break; 
				case 7:		 score7 = score; break; 
				case 8:		 score8 = score; break; 
				case 9:		 score9 = score; break; 
				case 10:	 score10 = score; break; 
				case 11:	 score11 = score; break; 
				case 12:	 score12 = score; break; 
				case 13:	 score13 = score; break; 
				case 14:	 score14 = score; break; 
				case 15:	 score15 = score; break; 		
			}
		
	}
	//remember to check for Score=-1  => no selected/not finished
	public Double getScoreDim(int dimID){
		switch (dimID){
			case 1:		return score1; 
			case 2:		return score2; 
			case 3:		return score3; 
			case 4:		return score4; 
			case 5:		return score5; 
			case 6:		return score6; 
			case 7:		return score7; 
			case 8:		return score8; 
			case 9:		return score9; 
			case 10:	return score10; 
			case 11:	return score11; 
			case 12:	return score12; 
			case 13:	return score13; 
			case 14:	return score14; 
			case 15:	return score15; 		
		}
		return null;
	}
	
	
	
}
