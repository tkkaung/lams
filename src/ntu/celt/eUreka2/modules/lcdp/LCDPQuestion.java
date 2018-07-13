package ntu.celt.eUreka2.modules.lcdp;


import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.profiling.ProfileQuestion;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class LCDPQuestion {
	@NonVisual
	private long id;
	private String des;
	private Integer number;
	private boolean dimLeadership; 
	private boolean dimManagement; 
	private boolean dimCommand; 
	
	
	public LCDPQuestion(){
		super();
		id =  Util.generateLongID();
	}


	public LCDPQuestion(String des, Integer number,
			boolean dimLeadership, boolean dimManagement, boolean dimCommand) {
		super();
		id =  Util.generateLongID();
		this.des = des;
		this.number = number;
		this.dimLeadership = dimLeadership;
		this.dimManagement = dimManagement;
		this.dimCommand = dimCommand;
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
	
	
	public LCDPQuestion createCopy(){
		return new LCDPQuestion(this.des, this.number, this.dimLeadership, this.dimManagement, this.dimCommand);
	}
	
	public String getNumberDisplay(){
		if(number==null)
			return "";
		return "Q" + number + ". ";
	}


	public boolean isDimLeadership() {
		return dimLeadership;
	}


	public void setDimLeadership(boolean dimLeadership) {
		this.dimLeadership = dimLeadership;
	}


	public boolean isDimManagement() {
		return dimManagement;
	}


	public void setDimManagement(boolean dimManagement) {
		this.dimManagement = dimManagement;
	}


	public boolean isDimCommand() {
		return dimCommand;
	}


	public void setDimCommand(boolean dimCommand) {
		this.dimCommand = dimCommand;
	}

	
}
