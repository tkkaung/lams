package ntu.celt.eUreka2.modules.ipsp;

import java.io.Serializable;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class IQuestion extends IPSPQuestion implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3339723223361930187L;
	@NonVisual
	private IQuestionSet questionset;
	
	
	public IQuestion(){
		super();
	}
	
	public IQuestion(String des, IQuestionSet questionset, Integer number, String[] dims) {
		super(des, number, dims);
		this.questionset = questionset;
	}

	public IQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(IQuestionSet questionset) {
		this.questionset = questionset;
	}
	
	public IQuestion createCopy(){
		String dims[] = {getDimension1(), getDimension2(), getDimension3(), getDimension4() };
		return new IQuestion(this.getDes(), this.getQuestionset(), this.getNumber(), dims);
	}
}
