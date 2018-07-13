package ntu.celt.eUreka2.modules.assessment;


import java.io.Serializable;

import org.apache.tapestry5.beaneditor.NonVisual;

public class RubricCriterion implements Cloneable{
	@NonVisual
	private long id;
	private int score;  //can also view as performanceLevel
	private String des; //descriptor of the performanceLevel
	private RubricCriteria criteria;
	
	@Override
	public RubricCriterion clone(){
		RubricCriterion rcion = new RubricCriterion();
		//rcion.setId(id);
		rcion.setScore(score);
		rcion.setDes(des);
		rcion.setCriteria(criteria);
		
		return rcion;
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
	public void setCriteria(RubricCriteria criteria) {
		this.criteria = criteria;
	}
	public RubricCriteria getCriteria() {
		return criteria;
	}
	
	
}
