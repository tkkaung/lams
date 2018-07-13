package ntu.celt.eUreka2.modules.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.ProjFYPExtraInfo;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.hibernate.annotations.Entity;

@Entity
public class Group implements JSONable, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4273514267136164649L;
	@NonVisual
	private long id;
	private String groupType;
	private List<GroupUser> groupUsers = new ArrayList<GroupUser>();
	private Project project;
	private User creator;
	private Date cdate= new Date();
	private Date mdate= new Date();
	private Boolean allowSelfEnroll = false;
	private Integer maxPerGroup = 0;
	private String bbID;
	
	/*IMPORTANT: if other Modules use Grouping. must also delete groupId in that Module when deleteing in GroupIndex.java 
	 * (assume cannot auto Cascade within Database)
	*/	
	
	public Group(){
		super();
		id = Util.generateLongID();
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getGroupType() {
		return groupType;
	}
	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}
	public List<GroupUser> getGroupUsers() {
		return groupUsers;
	}
	public void setGroupUsers(List<GroupUser> groupUsers) {
		this.groupUsers = groupUsers;
	}
	public void addGroupUser(GroupUser groupUser){
		groupUsers.add(groupUser);
	}
	public void removeGroupUser(GroupUser groupUser){
		groupUsers.remove(groupUser);
	}
	public int getTotalEnrolled(){
		int total = 0;
		for(int i=0; i<groupUsers.size(); i++){
			total += groupUsers.get(i).getUsers().size();
		}
		return total;
	}
	
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public Boolean getAllowSelfEnroll() {
		if(allowSelfEnroll==null)
			return false;
		return allowSelfEnroll;
	}
	public void setAllowSelfEnroll(Boolean allSelfEnroll) {
		this.allowSelfEnroll = allSelfEnroll;
	}
	public Integer getMaxPerGroup() {
		if(maxPerGroup==null)
			return 0;
		return maxPerGroup;
	}
	public void setMaxPerGroup(Integer maxPerGroup) {
		this.maxPerGroup = maxPerGroup;
	}
	
	public String getBbID() {
		if(bbID==null)
			return "";
		return bbID;
	}
	public void setBbID(String bbID) {
		this.bbID = bbID;
	}
	@Override
	public JSONObject toJSONObject() {
		return toJSONObject(true);
	}
	public JSONObject toJSONObject(boolean includeGroupDetail) {
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("groupsetName", groupType);
		j.put("projectId", project.getId());
		j.put("creator", creator==null? null : creator.getId());
		j.put("creatorUsername", creator==null? null : creator.getUsername());
		j.put("cdate", cdate.getTime());
		j.put("mdate", mdate.getTime());
		j.put("allowSelfEnroll", allowSelfEnroll);
		j.put("typeName", maxPerGroup);
		j.put("maxPerGroup", maxPerGroup);
		j.put("numSubGroup", groupUsers.size());
		j.put("bbID", bbID);
		
		if(includeGroupDetail){
			JSONArray jGroupUsers = new JSONArray();
			for(GroupUser obj : groupUsers){
				JSONObject jobj = new JSONObject();
				jobj.put("id", obj.getId());
				jobj.put("groupNum", obj.getGroupNum());
				jobj.put("groupNumName", obj.getGroupNumName());
				jobj.put("tutorUsername", (obj.getTutor()==null? "": obj.getTutor().getUsername()));
				jobj.put("bbID", obj.getBbID());
				
				JSONArray jUsers = new JSONArray();
				for(User u : obj.getUsers()){
					JSONObject jU = new JSONObject();
					jU.put("id", u.getId());
					jU.put("username", u.getUsername());
					
					jUsers.put(jU);
				}
				jobj.put("users", jUsers);
				
				jGroupUsers.put(jobj);
			}
			j.put("groupUsers", jGroupUsers);
		}
		
		return j;
	}
	
}
