package ntu.celt.eUreka2.modules.teameffectiveness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class TQuestion extends TEQuestion implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3339722222461930297L;
	@NonVisual
	private TEQuestionSet questionset;
	
	
	public TQuestion(){
		super();
	}
	
	public TQuestion(String des, TEQuestionSet questionset, Integer number, String[] dims) {
		super(des, number, dims);
		this.questionset = questionset;
	}

	public TEQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(TEQuestionSet questionset) {
		this.questionset = questionset;
	}
	
	public TQuestion createCopy(){
		String dims[] = {getDimension1(), getDimension2(), getDimension3(), getDimension4(), getDimension5(), getDimension6(), getDimension7(), getDimension8(), getDimension9(), getDimension10(), getDimension11(), getDimension12(), getDimension13(), getDimension14(), getDimension15() };
		return new TQuestion(this.getDes(), this.getQuestionset(), this.getNumber(), dims);
	}
}
