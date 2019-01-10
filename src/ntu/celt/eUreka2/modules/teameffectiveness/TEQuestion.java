package ntu.celt.eUreka2.modules.teameffectiveness;


import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class TEQuestion {
	@NonVisual
	private long id;
	private String des;
	private Integer number;
	private String dimension1;  //X , R, or null
	private String dimension2;
	private String dimension3;
	private String dimension4;
	private String dimension5;
	private String dimension6;  //X , R, or null
	private String dimension7;
	private String dimension8;
	private String dimension9;
	private String dimension10;
	private String dimension11;  //X , R, or null
	private String dimension12;
	private String dimension13;
	private String dimension14;
	private String dimension15;
	
	
	public TEQuestion(){
		super();
		id =  Util.generateLongID();
	}


	public TEQuestion(String des, Integer number, String[] dims ) {
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
	
	
	public TEQuestion createCopy(){
		String dims[] = {dimension1, dimension2, dimension3, dimension4, dimension5, dimension6, dimension7, dimension8, dimension9, dimension10, dimension11, dimension12, dimension13, dimension14, dimension15 };
		return new TEQuestion(this.des, this.number, dims);
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
		if(dimension6 != null)
			strBuilder.append("6_" + dimension6 + ", " );
		if(dimension7 != null)
			strBuilder.append("7_" + dimension7 + ", " );
		if(dimension8 != null)
			strBuilder.append("8_" + dimension8 + ", " );
		if(dimension9 != null)
			strBuilder.append("9_" + dimension9 + ", " );
		if(dimension10 != null)
			strBuilder.append("10_" + dimension10 + ", " );
		if(dimension11 != null)
			strBuilder.append("11_" + dimension11 + ", " );
		if(dimension12 != null)
			strBuilder.append("12_" + dimension12 + ", " );
		if(dimension13 != null)
			strBuilder.append("13_" + dimension13 + ", " );
		if(dimension14 != null)
			strBuilder.append("14_" + dimension14 + ", " );
		if(dimension15 != null)
			strBuilder.append("15_" + dimension15 + ", " );
		
		return Util.removeLastSeparator(strBuilder.toString(), ", ");
	}
	public String getDimension(int num){
		switch (num){
			case 1:		return dimension1; 
			case 2:		return dimension2; 
			case 3:		return dimension3; 
			case 4:		return dimension4; 
			case 5:		return dimension5; 
			case 6:		return dimension6; 
			case 7:		return dimension7; 
			case 8:		return dimension8; 
			case 9:		return dimension9; 
			case 10:	return dimension10; 
			case 11:	return dimension11; 
			case 12:	return dimension12; 
			case 13:	return dimension13; 
			case 14:	return dimension14; 
			case 15:	return dimension15; 
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
			case 6:		dimension6 = dim; break; 
			case 7:		dimension7 = dim; break;
			case 8:		dimension8 = dim; break;
			case 9:		dimension9 = dim; break;
			case 10:	dimension10 = dim; break;
			case 11:	dimension11 = dim; break; 
			case 12:	dimension12 = dim; break;
			case 13:	dimension13 = dim; break;
			case 14:	dimension14 = dim; break;
			case 15:	dimension15 = dim; break;
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


	public String getDimension6() {
		return dimension6;
	}


	public void setDimension6(String dimension6) {
		this.dimension6 = dimension6;
	}


	public String getDimension7() {
		return dimension7;
	}


	public void setDimension7(String dimension7) {
		this.dimension7 = dimension7;
	}


	public String getDimension8() {
		return dimension8;
	}


	public void setDimension8(String dimension8) {
		this.dimension8 = dimension8;
	}


	public String getDimension9() {
		return dimension9;
	}


	public void setDimension9(String dimension9) {
		this.dimension9 = dimension9;
	}


	public String getDimension10() {
		return dimension10;
	}


	public void setDimension10(String dimension10) {
		this.dimension10 = dimension10;
	}


	public String getDimension11() {
		return dimension11;
	}


	public void setDimension11(String dimension11) {
		this.dimension11 = dimension11;
	}


	public String getDimension12() {
		return dimension12;
	}


	public void setDimension12(String dimension12) {
		this.dimension12 = dimension12;
	}


	public String getDimension13() {
		return dimension13;
	}


	public void setDimension13(String dimension13) {
		this.dimension13 = dimension13;
	}


	public String getDimension14() {
		return dimension14;
	}


	public void setDimension14(String dimension14) {
		this.dimension14 = dimension14;
	}


	public String getDimension15() {
		return dimension15;
	}


	public void setDimension15(String dimension15) {
		this.dimension15 = dimension15;
	}


}
