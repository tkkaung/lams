package ntu.celt.eUreka2.modules.assessment;

import java.io.Serializable;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class AssessCriterion implements Cloneable {
	@NonVisual
	private long id;
	private int score;  //can also view as performanceLevel
	private String des; //descriptor of the performanceLevel
	private AssessCriteria criteria;
	
	public AssessCriterion clone(){
		AssessCriterion a = new AssessCriterion();
//		a.setId(id);
		a.setScore(score);
		a.setDes(des);
		a.setCriteria(criteria);
		return a;
	}
	public AssessCriterion(){
		id = Util.generateLongID();
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
	public void setCriteria(AssessCriteria criteria) {
		this.criteria = criteria;
	}
	public AssessCriteria getCriteria() {
		return criteria;
	}
}
