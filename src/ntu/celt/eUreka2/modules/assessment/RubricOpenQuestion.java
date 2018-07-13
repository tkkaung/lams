package ntu.celt.eUreka2.modules.assessment;


import java.io.Serializable;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class RubricOpenQuestion implements Cloneable, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -788501296235413364L;
	@NonVisual
	private long id;
	private String question;
	private Rubric rubric;
	
	public RubricOpenQuestion(){
		super();
		id = Util.generateLongID();
	}
	
	@Override
	public RubricOpenQuestion clone(){
		RubricOpenQuestion rquestion = new RubricOpenQuestion();
		//rquestion.setId(id);
		rquestion.setQuestion(question);
		
		return rquestion;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}

	public Rubric getRubric() {
		return rubric;
	}
	
	
	
}
