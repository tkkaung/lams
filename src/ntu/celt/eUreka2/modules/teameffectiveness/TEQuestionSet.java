package ntu.celt.eUreka2.modules.teameffectiveness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Entity;

import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

@Entity
public class TEQuestionSet implements  Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5822332735436251557L;
	
	@NonVisual
	private long id;
	@Validate("Required")
	private String name;
	private String des;
	private User owner;
	private Date cdate;
	private Date mdate;
	private List<TQuestion> questions = new ArrayList<TQuestion>();
	private School school; //null = all school
	

	public TEQuestionSet(){
		super();
		id =  Util.generateLongID();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String desc) {
		this.des = desc;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getCdate() {
		return cdate;
	}

	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}

	public Date getMdate() {
		return mdate;
	}

	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}

	public List<TQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<TQuestion> questions) {
		this.questions = questions;
	}

	public void addQuestion(TQuestion rc){
		questions.add(rc);
	}
	public void removeQuestion(TQuestion rc){
		questions.remove(rc);
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public School getSchool() {
		return school;
	}
	
	
}
