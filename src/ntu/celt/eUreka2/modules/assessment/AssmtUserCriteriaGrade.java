package ntu.celt.eUreka2.modules.assessment;


import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class AssmtUserCriteriaGrade implements Cloneable{
	@NonVisual
	private long id;
	private AssessCriteria criteria ;
	private Float score;
	private String comment;
	
	public AssmtUserCriteriaGrade clone(){
		AssmtUserCriteriaGrade a = new AssmtUserCriteriaGrade();
	//	a.setId(id);
		a.setCriteria(criteria);
		a.setScore(score);
		a.setComment(comment);
		
		return a;
	}
	public AssmtUserCriteriaGrade(){
		id = Util.generateLongID();
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public AssessCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(AssessCriteria criteria) {
		this.criteria = criteria;
	}

	



	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
