package ntu.celt.eUreka2.modules.peerevaluation;

import java.io.Serializable;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class EvaluationCriterion implements Cloneable  {
	@NonVisual
	private long id;
	private int score;  //can also view as performanceLevel
	private String des; //descriptor of the performanceLevel
	private EvaluationCriteria criteria;
	
	
	public EvaluationCriterion(){
		super();
		id = Util.generateLongID();
	}
	public EvaluationCriterion clone(){
		EvaluationCriterion e = new EvaluationCriterion();
		e.setScore(score);
		e.setDes(des);
		e.setCriteria(criteria);
		return e;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public void setCriteria(EvaluationCriteria criteria) {
		this.criteria = criteria;
	}
	public EvaluationCriteria getCriteria() {
		return criteria;
	}
}
