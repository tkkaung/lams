package ntu.celt.eUreka2.modules.lcdp;

import ntu.celt.eUreka2.internal.Util;

import org.hibernate.annotations.Entity;

@Entity
public class PQuestionScore {
	private String id;
	private LCDPQuestion question;
	private int score;
	private LCDPSurveyUser lcdpUser; //reference back
	private Integer orderNum;
	
	public PQuestionScore(){
		super();
		id =  Util.generateUUID();
	}
	public PQuestionScore(LCDPQuestion question, int score, LCDPSurveyUser lcdpUser, int orderNum){
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.score = score;
		this.lcdpUser = lcdpUser;
		this.orderNum = orderNum;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public LCDPQuestion getQuestion() {
		return question;
	}
	public void setQuestion(LCDPQuestion question) {
		this.question = question;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}

	public void setLcdpUser(LCDPSurveyUser lcdpUser) {
		this.lcdpUser = lcdpUser;
	}

	public LCDPSurveyUser getLcdpUser() {
		return lcdpUser;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	public Integer getOrderNum() {
		return orderNum;
	}
	
	

}
