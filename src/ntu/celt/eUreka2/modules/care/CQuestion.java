package ntu.celt.eUreka2.modules.care;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class CQuestion extends CAREQuestion implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3339750222461930297L;
	@NonVisual
	private CQuestionSet questionset;
	
	
	public CQuestion(){
		super();
	}
	
	public CQuestion(String des, CQuestionSet questionset, Integer number, String[] dims) {
		super(des, number, dims);
		this.questionset = questionset;
	}

	public CQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(CQuestionSet questionset) {
		this.questionset = questionset;
	}
	
	public CQuestion createCopy(){
		String dims[] = {getDimension1(), getDimension2(), getDimension3(), getDimension4(), getDimension5(), getDimension6(), getDimension7(), 
				getDimension8(), getDimension9(), getDimension10(), getDimension11(), getDimension12(), getDimension13(), getDimension14(), getDimension15() };
		return new CQuestion(this.getDes(), this.getQuestionset(), this.getNumber(), dims);
	}
}
