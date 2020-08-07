package org.lamsfoundation.lams.monitoring.dto;

import java.util.ArrayList;
import java.util.List;

import org.lamsfoundation.lams.learningdesign.Group;
import org.lamsfoundation.lams.util.AlphanumComparator;

public class TblGroupDTO implements Comparable {

    private Long groupID;
    private String groupName;
    private int orderID;
    private List<TblUserDTO> userList;

    private TblUserDTO groupLeader;

    private Double traScore;

    /**
     * Get the DTO for this group. Does not include the GroupBranchActivities as they will be in a separate array for
     * Flash.
     *
     * @param group
     */
    @SuppressWarnings("unchecked")
    public TblGroupDTO(Group group) {
	groupID = group.getGroupId();
	groupName = group.getGroupName();
	orderID = group.getOrderId();
	userList = new ArrayList<TblUserDTO>();
    }

    @Override
    public int compareTo(Object o) {
	TblGroupDTO castOther = (TblGroupDTO) o;

	String grp1Name = castOther != null && castOther.getGroupName() != null ? castOther.getGroupName() : "";
	String grp2Name = this.groupName != null ? this.groupName : "";

	AlphanumComparator comparator = new AlphanumComparator();
	return -comparator.compare(grp1Name, grp2Name);
    }

    public Long getGroupID() {
	return groupID;
    }

    public void setGroupID(Long groupID) {
	this.groupID = groupID;
    }

    public String getGroupName() {
	return groupName;
    }

    public void setGroupName(String groupName) {
	this.groupName = groupName;
    }

    public int getOrderID() {
	return orderID;
    }

    public void setOrderID(int orderID) {
	this.orderID = orderID;
    }

    public List<TblUserDTO> getUserList() {
	return userList;
    }

    public void setUserList(List<TblUserDTO> userList) {
	this.userList = userList;
    }

    public TblUserDTO getGroupLeader() {
	return groupLeader;
    }

    public void setGroupLeader(TblUserDTO groupLeader) {
	this.groupLeader = groupLeader;
    }

    public void setTraScore(Double traScore) {
	this.traScore = traScore;
    }

    public Double getTraScore() {
	return traScore;
    }
}
