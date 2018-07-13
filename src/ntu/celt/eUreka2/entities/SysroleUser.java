package ntu.celt.eUreka2.entities;

import java.io.Serializable;

import org.hibernate.annotations.Entity;

@Entity
public class SysroleUser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3910620664343774851L;
	private long id;
	private SysRole sysRole;
	private User user;
	private Integer param;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public SysRole getSysRole() {
		return sysRole;
	}
	public void setSysRole(SysRole sysRole) {
		this.sysRole = sysRole;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getParam() {
		return param;
	}
	public void setParam(Integer param) {
		this.param = param;
	}
	
	
}
