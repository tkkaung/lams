package ntu.celt.eUreka2.modules.backuprestore;

import java.util.Date;

import ntu.celt.eUreka2.internal.Util;

public class MigratedProjInfo {
	private String eureka1ProjId;
	private String eureka2ProjId; //ID of this entity
	private Date createDate;
	private String creatorUsername;
	private String remark;
	
	
	public String getEureka1ProjId() {
		return eureka1ProjId;
	}
	public void setEureka1ProjId(String eureka1ProjId) {
		this.eureka1ProjId = eureka1ProjId;
	}
	public String getEureka2ProjId() {
		return eureka2ProjId;
	}
	public void setEureka2ProjId(String eureka2ProjId) {
		this.eureka2ProjId = eureka2ProjId;
	}
	public String getCreatorUsername() {
		return creatorUsername;
	}
	public void setCreatorUsername(String creatorUsername) {
		this.creatorUsername = creatorUsername;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateDateDisplay(){
		return Util.formatDateTime(createDate);
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getRemark() {
		return remark;
	}
	public void appendRemark(String remark) {
		if(this.remark==null)
			this.remark = remark;
		this.remark += remark;
	}
	public void appendlnRemark(String remark) {
		appendRemark(remark+"\n");
	}
	
}
