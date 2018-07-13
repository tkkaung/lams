package ntu.celt.eUreka2.modules.profiling;

import ntu.celt.eUreka2.internal.Util;

import org.hibernate.annotations.Entity;

@Entity
public class QuestionScore {
	private String id;
	private ProfileQuestion question;
	private int score;
	private ProfileUser profUser; //reference back
	private Integer orderNum;
	
	public QuestionScore(){
		super();
		id =  Util.generateUUID();
	}
	public QuestionScore(ProfileQuestion question, int score, ProfileUser profUser, int orderNum){
		super();
		id =  Util.generateUUID();
		this.question = question;
		this.score = score;
		this.profUser = profUser;
		this.orderNum = orderNum;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public ProfileQuestion getQuestion() {
		return question;
	}
	public void setQuestion(ProfileQuestion question) {
		this.question = question;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}

	public void setProfUser(ProfileUser profUser) {
		this.profUser = profUser;
	}

	public ProfileUser getProfUser() {
		return profUser;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	public Integer getOrderNum() {
		return orderNum;
	}
	
	

}
