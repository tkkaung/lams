package ntu.celt.eUreka2.modules.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class GroupUser implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2720007322751987999L;
	@NonVisual
	private long id;
	private int groupNum;
	private String groupNumName;
	private Group group;
	private List<User> users = new ArrayList<User>();
	private User tutor ;
	private String bbID;

	
	public GroupUser(){
		super();
		id = Util.generateLongID();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getGroupNum() {
		return groupNum;
	}
	public void setGroupNum(int groupNum) {
		this.groupNum = groupNum;
	}
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public void addUser(User user){
		this.users.add(user);
	}
	public void removeUser(User user){
		this.users.remove(user);
	}

	public void setGroupNumName(String groupNumName) {
		this.groupNumName = groupNumName;
	}

	public String getGroupNumName() {
		return groupNumName;
	}
	public String getGroupNumNameDisplay(){
		if(groupNumName != null)
			return groupNumName;
		return Integer.toString(groupNum);
	}

	public void setTutor(User tutor) {
		this.tutor = tutor;
	}

	public User getTutor() {
		return tutor;
	}
	public String getBbID() {
		if(bbID==null)
			return "";
		return bbID;
	}
	public void setBbID(String bbID) {
		this.bbID = bbID;
	}
}
