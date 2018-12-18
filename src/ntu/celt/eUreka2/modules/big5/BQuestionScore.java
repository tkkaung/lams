package ntu.celt.eUreka2.modules.big5;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.internal.antlr.PropertyExpressionParser.constant_return;
import org.hibernate.annotations.Entity;

@Entity
public class BQuestionScore {
	private String id;
	private BIG5Question question;
	private int score = 0;  //original selected by student
	private int score1 = 0; 	//remember to check for Score=0  => no selected
	private int score2 = 0;
	private int score3 = 0;
	private int score4 = 0;
	private int score5 = 0;
	private BIG5SurveyUser big5User; //reference back
	private Integer orderNum;
	
	public static final int MAX_SCORE = 5;

	
	public BQuestionScore(){
		super();
		id =  Util.generateUUID();
	}
	
	public BQuestionScore(BIG5Question question, int score, int score1, int score2,
			int score3, int score4, int score5, BIG5SurveyUser big5User,
			Integer orderNum) {
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.big5User = big5User;
		this.orderNum = orderNum;
		this.score = score;
		this.score1 = score1;
		this.score2 = score2;
		this.score3 = score3;
		this.score4 = score4;
		this.score5 = score5;
	}
	public BQuestionScore(BIG5Question question, int score, int[] scores, BIG5SurveyUser big5User,
			Integer orderNum) {
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.big5User = big5User;
		this.orderNum = orderNum;
		this.score = score;
		this.score1 = scores[0];
		this.score2 = scores[1];
		this.score3 = scores[2];
		this.score4 = scores[3];
		this.score5 = scores[4];
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public BIG5Question getQuestion() {
		return question;
	}
	public void setQuestion(BIG5Question question) {
		this.question = question;
	}

	public void setBig5User(BIG5SurveyUser big5User) {
		this.big5User = big5User;
	}

	public BIG5SurveyUser getBig5User() {
		return big5User;
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
	
	

}
