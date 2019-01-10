package ntu.celt.eUreka2.modules.teameffectiveness;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.internal.antlr.PropertyExpressionParser.constant_return;
import org.hibernate.annotations.Entity;

@Entity
public class TEQuestionScore {
	private String id;
	private TEQuestion question;
	private int score = 0;  //original selected by student
	private int score1 = 0; 	//remember to check for Score=0  => no selected
	private int score2 = 0;
	private int score3 = 0;
	private int score4 = 0;
	private int score5 = 0;
	private int score6 = 0; 	//remember to check for Score=0  => no selected
	private int score7 = 0;
	private int score8 = 0;
	private int score9 = 0;
	private int score10 = 0;
	private int score11 = 0; 	//remember to check for Score=0  => no selected
	private int score12 = 0;
	private int score13 = 0;
	private int score14 = 0;
	private int score15 = 0;
	private TESurveyUser TEUser; //reference back
	private Integer orderNum;
	
	public static final int MAX_SCORE = 5;

	
	public TEQuestionScore(){
		super();
		id =  Util.generateUUID();
	}
	
	public TEQuestionScore(TEQuestion question, int score, int score1, int score2,
			int score3, int score4, int score5, int score6, int score7,
			int score8, int score9, int score10, int score11, int score12,
			int score13, int score14, int score15, TESurveyUser TEUser,
			Integer orderNum) {
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.TEUser = TEUser;
		this.orderNum = orderNum;
		this.score = score;
		this.score1 = score1;
		this.score2 = score2;
		this.score3 = score3;
		this.score4 = score4;
		this.score5 = score5;
		this.score6 = score6;
		this.score7 = score7;
		this.score8 = score8;
		this.score9 = score9;
		this.score10 = score10;
		this.score11 = score11;
		this.score12 = score12;
		this.score13 = score13;
		this.score14 = score14;
		this.score15 = score15;
	}
	public TEQuestionScore(TEQuestion question, int score, int[] scores, TESurveyUser TEUser,
			Integer orderNum) {
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.TEUser = TEUser;
		this.orderNum = orderNum;
		this.score = score;
		this.score1 = scores[0];
		this.score2 = scores[1];
		this.score3 = scores[2];
		this.score4 = scores[3];
		this.score5 = scores[4];
		this.score6 = scores[5];
		this.score7 = scores[6];
		this.score8 = scores[7];
		this.score9 = scores[8];
		this.score10 = scores[9];
		this.score11 = scores[10];
		this.score12 = scores[11];
		this.score13 = scores[12];
		this.score14 = scores[13];
		this.score15 = scores[14];
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public TEQuestion getQuestion() {
		return question;
	}
	public void setQuestion(TEQuestion question) {
		this.question = question;
	}

	public void setTEUser(TESurveyUser TEUser) {
		this.TEUser = TEUser;
	}

	public TESurveyUser getTEUser() {
		return TEUser;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	public Integer getOrderNum() {
		return orderNum;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	//remember to check for Score=0  => no selected
	public int getScoreDim(int dimID){
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
		return 0;
	}
	
	
	public int getScore1() {
		return score1;
	}

	public void setScore1(int score1) {
		this.score1 = score1;
	}

	public int getScore2() {
		return score2;
	}

	public void setScore2(int score2) {
		this.score2 = score2;
	}

	public int getScore3() {
		return score3;
	}

	public void setScore3(int score3) {
		this.score3 = score3;
	}

	public int getScore4() {
		return score4;
	}

	public void setScore4(int score4) {
		this.score4 = score4;
	}

	public int getScore5() {
		return score5;
	}

	public void setScore5(int score5) {
		this.score5 = score5;
	}

	public int getScore6() {
		return score6;
	}

	public void setScore6(int score6) {
		this.score6 = score6;
	}

	public int getScore7() {
		return score7;
	}

	public void setScore7(int score7) {
		this.score7 = score7;
	}

	public int getScore8() {
		return score8;
	}

	public void setScore8(int score8) {
		this.score8 = score8;
	}

	public int getScore9() {
		return score9;
	}

	public void setScore9(int score9) {
		this.score9 = score9;
	}

	public int getScore11() {
		return score11;
	}

	public void setScore11(int score11) {
		this.score11 = score11;
	}

	public int getScore10() {
		return score10;
	}

	public void setScore10(int score10) {
		this.score10 = score10;
	}

	public int getScore12() {
		return score12;
	}

	public void setScore12(int score12) {
		this.score12 = score12;
	}

	public int getScore13() {
		return score13;
	}

	public void setScore13(int score13) {
		this.score13 = score13;
	}

	public int getScore14() {
		return score14;
	}

	public void setScore14(int score14) {
		this.score14 = score14;
	}

	public int getScore15() {
		return score15;
	}

	public void setScore15(int score15) {
		this.score15 = score15;
	}
	
	

}
