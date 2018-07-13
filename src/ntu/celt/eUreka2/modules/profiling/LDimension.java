package ntu.celt.eUreka2.modules.profiling;

import java.util.Date;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class LDimension {
	@NonVisual
	private String id;
	private String name;
	private String des;
	private String colorCode;
	private int orderNum;
	private Boolean active;
	private String styleGroup;
	private Date createTime;
	
	
//	public static final String _1_MBE_ACTIVE = "1_MBE_A";
//	public static final String _2_MBE_PASSIVE = "2_MBE_P";
//	public static final String _3_CONTINGENT_REWARD = "3_CONT_R";
//	public static final String _4_TRANSF_INDIVIDUAL_CONSIDERATION = "4_TRAN_I_C";
//	public static final String _5_TRANSF_IDEALIZED_INFLUENCE = "5_TRAN_I_I";
//	public static final String _6_LDR_FLEX = "6_LDR_F";
//	public static final String _7_TRANSF_INSPIRATION_MOTIVATION = "7_TRAN_I_M";
//	public static final String _8_TRANSF_INTELLECTUAL_SIMULATION = "8_TRAN_I_S";
//	public static final String _9_LASSEIZ_FAIRE = "9_LASS_F";
//	public static final String _0_CALIBRATION = "0_CALIBRATION";

	

	public static final String _1_INDIVIDUAL_CONSIDERATION = "1_I_C";
	public static final String _2_INTELLECTUAL_SIMULATION = "2_I_S";
	public static final String _3_INSPIRATION_MOTIVATION = "3_I_M";
	public static final String _4_IDEALIZED_INFLUENCE = "4_I_I";
	public static final String _5_CONTINGENT_REWARD = "5_CONT_R";
	public static final String _6_MNG_BY_EXCEPT_POLICEMAN = "6_MBE_POLICE";
	public static final String _7_MNG_BY_EXCEPT_FIREMAN = "7_MBE_FIREMAN";
	
	public static final String _8_LASSEIZ_FAIRE = "8_LASS_F";
	public static final String _9_LDR_FLEX = "9_LDR_F";
	public static final String _10_PERCEIVED_LEADERSHIP_EFFECTIVENESS = "10_P_L_E";
	public static final String _11_CALIBRATION = "11_CALIBRATION";
	
	public static final String _S_1_TRANSFORMATIONAL = "Tranformational Leadership Style";
	public static final String _S_2_TRANSACTIONAL = "Transactional Leadership Style";
	public static final String _S_3_LEADERSHIP_FLEXIBILITY = "Leadership Flexibility";
	public static final String _S_4_PERCEIVED_LEADERSHIP_EFFECTIVENESS = "Perceived Leadership Effectiveness";
	public static final String _S_5_LASSEIZ_FAIRE = "Lasseiz- Faire";
	public static final String _S_6_CALIBRATION = "Calibration";
	
	
/*	public static final String _1_MBE_ACTIVE = "1_MBE_A";
	public static final String _2_MBE_PASSIVE = "2_MBE_P";
	public static final String _3_CONTINGENT_REWARD = "3_CONT_R";
	public static final String _4_TRANSF_INDIVIDUAL_CONSIDERATION = "4_TRAN_I_C";
	public static final String _5_TRANSF_IDEALIZED_INFLUENCE = "5_TRAN_I_I";
	public static final String _6_LDR_FLEX = "6_LDR_F";
	public static final String _7_TRANSF_INSPIRATION_MOTIVATION = "7_TRAN_I_M";
	public static final String _8_TRANSF_INTELLECTUAL_SIMULATION = "8_TRAN_I_S";
	public static final String _9_LASSEIZ_FAIRE = "9_LASS_F";
	public static final String _0_CALIBRATION = "0_CALIBRATION";*/
	
	public LDimension(){
		super();
		this.id = LDimension._10_PERCEIVED_LEADERSHIP_EFFECTIVENESS;
		this.createTime = new Date();
	}


	public LDimension(String id, String name, String des, String colorCode,
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


	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
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
