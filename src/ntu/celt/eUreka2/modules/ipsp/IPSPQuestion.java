package ntu.celt.eUreka2.modules.ipsp;


import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class IPSPQuestion {
	@NonVisual
	private long id;
	private String des;
	private Integer number;
	private String dimension1;  //description (separated with <br/>      ------//other modules:  X , R, or null
	private String dimension2;
	private String dimension3;
	private String dimension4;

	
	
	public IPSPQuestion(){
		super();
		id =  Util.generateLongID();
	}


	public IPSPQuestion(String des, Integer number, String[] dims ) {
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
	
	
	public IPSPQuestion createCopy(){
		String dims[] = {dimension1, dimension2, dimension3, dimension4 };
		return new IPSPQuestion(this.des, this.number, dims);
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
		return Util.removeLastSeparator(strBuilder.toString(), ", ");
	}
	public String getDimension(int num){
		switch (num){
			case 1:		return dimension1; 
			case 2:		return dimension2; 
			case 3:		return dimension3; 
			case 4:		return dimension4; 
		}
		return null;
	}
	public void setDimension(int num, String dim){
		switch (num){
			case 1:		dimension1 = dim; break; 
			case 2:		dimension2 = dim; break;
			case 3:		dimension3 = dim; break;
			case 4:		dimension4 = dim; break;
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


}
