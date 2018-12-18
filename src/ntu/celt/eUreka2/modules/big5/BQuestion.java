package ntu.celt.eUreka2.modules.big5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class BQuestion extends BIG5Question implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3339722222461930297L;
	@NonVisual
	private BQuestionSet questionset;
	
	
	public BQuestion(){
		super();
	}
	
	public BQuestion(String des, BQuestionSet questionset, Integer number, String[] dims) {
		super(des, number, dims);
		this.questionset = questionset;
	}

	public BQuestionSet getQuestionset() {
		return questionset;
	}


	public void setQuestionset(BQuestionSet questionset) {
		this.questionset = questionset;
	}
	
	public BQuestion createCopy(){
		String dims[] = {getDimension1(), getDimension2(), getDimension3(), getDimension4(), getDimension5() };
		return new BQuestion(this.getDes(), this.getQuestionset(), this.getNumber(), dims);
	}
}
