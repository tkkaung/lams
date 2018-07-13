package ntu.celt.eUreka2.modules.profiling;

import java.io.Serializable;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class LQuestion extends ProfileQuestion implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7746883727402547532L;
	@NonVisual
	private LQuestionSet questionset;
	
	
	public LQuestion(){
		super();
	}
	
	public LQuestion(String des, LQuestionSet questionset,
			LDimension dimension, boolean reverseScore, Integer number, boolean tenScale) {
		super(des, dimension, reverseScore, number, tenScale);
		this.questionset = questionset;
	}

	public LQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(LQuestionSet questionset) {
		this.questionset = questionset;
	}
	
}
