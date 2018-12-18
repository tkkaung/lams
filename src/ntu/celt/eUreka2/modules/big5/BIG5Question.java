package ntu.celt.eUreka2.modules.big5;


import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class BIG5Question {
	@NonVisual
	private long id;
	private String des;
	private Integer number;
	private String dimension1;  //X , R, or null
	private String dimension2;
	private String dimension3;
	private String dimension4;
	private String dimension5;
	
	
	public BIG5Question(){
		super();
		id =  Util.generateLongID();
	}


	public BIG5Question(String des, Integer number, String[] dims ) {
		super();
		id =  Util.generateLongID();
		this.des = des;
		this.number = number;
		for(int i=0; i<dims.length; i++){
			setDimension(i+1, dims[i]);
		}
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


	public void setNumber(Integer number) {
		this.number = number;
	}


	public Integer getNumber() {
		return number;
	}
	
	
	public BIG5Question createCopy(){
		String dims[] = {dimension1, dimension2, dimension3, dimension4, dimension5 };
		return new BIG5Question(this.des, this.number, dims);
	}
	
	public String getNumberDisplay(){
		if(number==null)
			return "";
		return "Q" + number + ". ";
	}

	public String getDimensionsDisplay(){
		StringBuilder strBuilder = new StringBuilder();
		if(dimension1 != null)
			strBuilder.append("1_" + dimension1 + ", " );
		if(dimension2 != null)
			strBuilder.append("2_" + dimension2 + ", " );
		if(dimension3 != null)
			strBuilder.append("3_" + dimension3 + ", " );
		if(dimension4 != null)
			strBuilder.append("4_" + dimension4 + ", " );
		if(dimension5 != null)
			strBuilder.append("5_" + dimension5 + ", " );
		
		return Util.removeLastSeparator(strBuilder.toString(), ", ");
	}
	public String getDimension(int num){
		switch (num){
			case 1:		return dimension1; 
			case 2:		return dimension2; 
			case 3:		return dimension3; 
			case 4:		return dimension4; 
			case 5:		return dimension5; 
		}
		return null;
	}
	public void setDimension(int num, String dim){
		switch (num){
			case 1:		dimension1 = dim; break; 
			case 2:		dimension2 = dim; break;
			case 3:		dimension3 = dim; break;
			case 4:		dimension4 = dim; break;
			case 5:		dimension5 = dim; break;
		}
	}


	public String getDimension1() {
		return dimension1;
	}


	public void setDimension1(String dimension1) {
		this.dimension1 = dimension1;
	}


	public String getDimension2() {
		return dimension2;
	}


	public void setDimension2(String dimension2) {
		this.dimension2 = dimension2;
	}


	public String getDimension3() {
		return dimension3;
	}


	public void setDimension3(String dimension3) {
		this.dimension3 = dimension3;
	}


	public String getDimension4() {
		return dimension4;
	}


	public void setDimension4(String dimension4) {
		this.dimension4 = dimension4;
	}


	public String getDimension5() {
		return dimension5;
	}


	public void setDimension5(String dimension5) {
		this.dimension5 = dimension5;
	}


}
