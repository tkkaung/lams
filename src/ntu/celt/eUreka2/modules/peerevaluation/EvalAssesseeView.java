package ntu.celt.eUreka2.modules.peerevaluation;

import java.io.Serializable;
import java.util.Date;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class EvalAssesseeView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3744074404817186205L;
	private long id;
	private User assessee;
	private Evaluation evaluation;

	private Date firstGradeViewTime;
	private Date lastGradeViewTime;
	
	
	public EvalAssesseeView(){
		super();
		id = Util.generateLongID();
	}
	public EvalAssesseeView(User assessee){
		super();
		id = Util.generateLongID();
		this.assessee = assessee;
		firstGradeViewTime = new Date();
		lastGradeViewTime = new Date();
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public User getAssessee() {
		return assessee;
	}
	public void setAssessee(User assessee) {
		this.assessee = assessee;
	}
	public Evaluation getEvaluation() {
		return evaluation;
	}
	public void setEvaluation(Evaluation assessment) {
		this.evaluation = assessment;
	}
	public void setFirstGradeViewTime(Date firstGradeViewTime) {
		this.firstGradeViewTime = firstGradeViewTime;
	}
	public Date getFirstGradeViewTime() {
		return firstGradeViewTime;
	}
	public String getFirstGradeViewTimeDisplay() {
		return Util.formatDateTime(firstGradeViewTime);
	}
	public void setLastGradeViewTime(Date lastGradeViewTime) {
		this.lastGradeViewTime = lastGradeViewTime;
	}
	public Date getLastGradeViewTime() {
		return lastGradeViewTime;
	}
	public String getLastGradeViewTimeDisplay() {
		return Util.formatDateTime(lastGradeViewTime);
	}
	
	// The need for a equal() method is discussed at http://www.hibernate.org/109.html
    @Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof EvalAssesseeView) && assessee != null 
    			&& assessee.equals(((EvalAssesseeView) obj).getAssessee());
    	
    }
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return assessee == null ? super.hashCode() : getAssessee().hashCode();
	}

}
