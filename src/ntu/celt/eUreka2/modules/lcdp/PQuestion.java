package ntu.celt.eUreka2.modules.lcdp;

import java.io.Serializable;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class PQuestion extends LCDPQuestion implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7756883767402547532L;
	@NonVisual
	private PQuestionSet questionset;
	
	
	public PQuestion(){
		super();
	}
	
	public PQuestion(String des, PQuestionSet questionset,
			Integer number, boolean dimLeadership, boolean dimManagement, boolean dimCommand) {
		super(des, number, dimLeadership, dimManagement, dimCommand);
		this.questionset = questionset;
	}

	public PQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(PQuestionSet questionset) {
		this.questionset = questionset;
	}
	
}
