package ntu.celt.eUreka2.modules.care;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class CDimension {
	@NonVisual
	private int id;
	private String name;
	private String des;
	private String colorCode;
	private int orderNum;
	private Boolean active;
	private String styleGroup;
	private Date createTime;
	
	
	public static final int NUM_DIMENSIONS = 15;

	/*public static final int _1_EMOTIONAL_SELF_AWARENESS = 1;
	public static final int _2_EMOTIONAL_SELF_MANAGEMENT = 2;
	public static final int _3_SOCIAL_COMPETENCE = 3;
	public static final int _4_RELATIONSHIP_MANAGEMENT = 4;
	
	public static final int _5_RESILENCE_STRESS_PERCEPTION = 5;
	public static final int _6_RESILENCE_CONTROL = 6;
	public static final int _7_RESILENCE_OWNERSHIP = 7;
	public static final int _8_REACH = 8;
	public static final int _9_ENDURANCE = 9;
	public static final int _10_RESILENCE_COPING = 10;
	
	public static final int _11_ADAPTABILITY_SENSE_MAKING = 11;
	public static final int _12_ADAPTABILITY_SENSE_GIVING = 12;
	public static final int _13_ADAPTABILITY_MEANING_MAKING = 13;
	
	public static final int _14_CREATIVITY_OTE = 14;
	public static final int _15_CREATIVITY_APPROPRIATE = 15;
	*/
	public static final String _S_1_EMOTIONAL_INTELLIGENCE = "Emotional Intelligence";
	public static final String _S_2_RESILENT = "Resilent";
	public static final String _S_3_ADAPTABILITY = "Adaptability";
	public static final String _S_4_CREATIVE = "Creative";
	
	
	public CDimension(){
		super();
		this.id = 1;
		this.createTime = new Date();
	}


	public CDimension(int id, String name, String des, String colorCode,
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
