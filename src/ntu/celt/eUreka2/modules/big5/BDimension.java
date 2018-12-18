package ntu.celt.eUreka2.modules.big5;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class BDimension {
	@NonVisual
	private int id;
	private String name;
	private String des;
	private String colorCode;
	private int orderNum;
	private Boolean active;
	private String styleGroup;
	private Date createTime;
	
	
	public static final int NUM_DIMENSIONS = 5;

	
	
	public BDimension(){
		super();
		this.id = 1;
		this.createTime = new Date();
	}


	public BDimension(int id, String name, String des, String colorCode,
			int orderNum, boolean active, String styleGroup) {
		super();
		this.id = id;
		this.name = name;
		this.des = des;
		this.colorCode = colorCode;
		this.orderNum = orderNum;
		this.active = active;
		this.styleGroup = styleGroup;
		this.createTime = new Date();
	}


	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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


	public void setDes(String des) {
		this.des = des;
	}


	public String getColorCode() {
		return colorCode;
	}


	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}


	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}


	public int getOrderNum() {
		return orderNum;
	}


	public void setActive(Boolean active) {
		this.active = active;
	}


	public Boolean getActive() {
		if(active==null)
			return false;
		return active;
	}


	public void setStyleGroup(String styleGroup) {
		this.styleGroup = styleGroup;
	}


	public String getStyleGroup() {
		if(styleGroup == null)
			return "";
		return styleGroup;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public Date getCreateTime() {
		return createTime;
	}

	
}
