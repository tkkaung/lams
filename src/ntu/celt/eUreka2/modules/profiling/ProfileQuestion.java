package ntu.celt.eUreka2.modules.profiling;


import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class ProfileQuestion {
	@NonVisual
	private long id;
	private String des;
	private LDimension dimension; //if null, = reminder question
	private boolean reverseScore;  //calculate score reversely ( 8 - selected score)
	private Integer number;
	private Boolean tenScale;  //calculate score reversely ( 8 - selected score)
	
	
	public ProfileQuestion(){
		super();
		id =  Util.generateLongID();
	}


	public ProfileQuestion(String des, LDimension dimension,
			boolean reverseScore, Integer number, boolean tenScale) {
		super();
		id =  Util.generateLongID();
		this.des = des;
		this.dimension = dimension;
		this.reverseScore = reverseScore;
		this.number = number;
		this.tenScale = tenScale;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getDes() {
		return des;
	}


	public void setDes(String desc) {
		this.des = desc;
	}


	

	public LDimension getDimension() {
		return dimension;
	}


	public void setDimension(LDimension dimension) {
		this.dimension = dimension;
	}


	public void setReverseScore(boolean reverseScore) {
		this.reverseScore = reverseScore;
	}


	public boolean isReverseScore() {
		return reverseScore;
	}


	public void setNumber(Integer number) {
		this.number = number;
	}


	public Integer getNumber() {
		return number;
	}
	
	
	public ProfileQuestion createCopy(){
		return new ProfileQuestion(this.des, this.dimension, this.reverseScore, this.number, this.tenScale);
	}
	
	public String getNumberDisplay(){
		if(number==null)
			return "";
		return "Q" + number + ". ";
	}


	public void setTenScale(Boolean tenScale) {
		this.tenScale = tenScale;
	}


	public Boolean getTenScale() {
		if(tenScale==null)
			return false;
		return tenScale;
	}
	
	
}
