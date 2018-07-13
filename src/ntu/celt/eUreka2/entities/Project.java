package ntu.celt.eUreka2.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.announcement.Announcement;
import ntu.celt.eUreka2.modules.assessment.Assessment;
import ntu.celt.eUreka2.modules.backuprestore.BackupEntry;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;
import ntu.celt.eUreka2.modules.blog.Blog;
import ntu.celt.eUreka2.modules.budget.Budget;
import ntu.celt.eUreka2.modules.elog.Elog;
import ntu.celt.eUreka2.modules.forum.Forum;
import ntu.celt.eUreka2.modules.learninglog.LogEntry;
import ntu.celt.eUreka2.modules.message.Message;
import ntu.celt.eUreka2.modules.peerevaluation.Evaluation;
import ntu.celt.eUreka2.modules.profiling.Profiling;
import ntu.celt.eUreka2.modules.resources.Resource;
import ntu.celt.eUreka2.modules.scheduling.Schedule;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.hibernate.annotations.Entity;

@Entity
public class Project implements JSONable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6898433481949102646L;
	private String id;
	@Validate("Required")
	private String name;
	private String description;
	private User creator;
	private User editor;
	@NonVisual
	private Date cdate;
	private Date mdate;
	@Validate("Required")
	private Date sdate;
	@Validate("Required")
	private Date edate;
	@Validate("Required")
	private ProjType type;
	private String courseId;
	private String groupId;
	private String seqNo;
	private String term;
	private String courseCode;
	
	
	private School school;
	@Validate("Required")
	private ProjStatus status;
	
	private boolean shared;
	private String remarks;
	private List<ProjUser> members = new ArrayList<ProjUser>();
	private List<ProjModule> projmodules = new ArrayList<ProjModule>();
	@NonVisual
	private Date lastAccess;
	@NonVisual
	private int numVisit = 0 ; //default value
	@NonVisual
	private Date lastStatusChange;
	@NonVisual
	private boolean noAutoChangeStatus = false; //default to False
	private List<ProjectAttachedFile> attachedFiles = new ArrayList<ProjectAttachedFile>();
	private Set<String> keywords = new HashSet<String>();
	private String companyInfo;
	private Set<ProjCAOExtraInfo> projCAOExtraInfos = new HashSet<ProjCAOExtraInfo>();
	private Set<ProjFYPExtraInfo> projFYPExtraInfos = new HashSet<ProjFYPExtraInfo>();
	
	
	/*purpose of having these, is to be able to cascade delete when the project is delete in database*/
	private Set<Announcement> announcements = new HashSet<Announcement>();
	private Set<Forum> forums = new HashSet<Forum>();
	private Set<Schedule> schedules = new HashSet<Schedule>();
	private Set<Budget> budgets = new HashSet<Budget>();
	private Set<Message> messages = new HashSet<Message>();
	private Set<LogEntry> logEntrys = new HashSet<LogEntry>();
	private Set<Resource> resources = new HashSet<Resource>();
	private Set<Assessment> assessments = new HashSet<Assessment>();
	private Set<Evaluation> evaluations = new HashSet<Evaluation>();
	private Set<Profiling> profilings = new HashSet<Profiling>();
	private Set<Blog> blogs = new HashSet<Blog>();
	private Set<Elog> elogs = new HashSet<Elog>();
	private Set<BackupEntry> backupEntrys = new HashSet<BackupEntry>();
	
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public void setEditor(User editor) {
		this.editor = editor;
	}
	public User getEditor() {
		return editor;
	}
	
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public Date getSdate() {
		return sdate;
	}
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getEdate() {
		return edate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public School getSchool() {
		return school;
	}
	public void setSchool(School school) {
		this.school = school;
	}
	public ProjStatus getStatus() {
		return status;
	}
	public void setStatus(ProjStatus status) {
		this.status = status;
	}
	public boolean isReference(){
		if(status.getName().equals(PredefinedNames.PROJSTATUS_REFERENCE))
			return true;
		return false;
	}
	public boolean isActive(){
		if(status.getName().equals(PredefinedNames.PROJSTATUS_ACTIVE))
			return true;
		return false;
	}
	
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public boolean isShared() {
		return shared;
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	/*try to avoid using this, use addMember() instead*/
	public void setMembers(List<ProjUser> members) {
		this.members = members;
	}
	public List<ProjUser> getMembers() {
		Collections.sort(members, new Comparator<ProjUser>(){
			@Override
			public int compare(ProjUser o1, ProjUser o2) {
				if(o1==null || o2==null)
					return 0;
				return o1.getUser().getDisplayName().compareTo(o2.getUser().getDisplayName());
			}
		});
		return members;
	}
	public ProjUser getMember(User user){
		for(ProjUser pu : members){
			if(pu==null){
				System.err.println("Error, member has NULL value (idx of list is non-consistent) projID=" + id);
				members.remove(pu);
				continue;
			}
			
			if(pu.getUser().equals(user)){
				return pu;
			}
		}
		return null;
	}
	public boolean hasMember(User user){
		if(getMember(user)!=null)
			return true;
		return false;
	}
	public boolean hasMember(String username){
		for(ProjUser pu : members){
			if(pu.getUser().getUsername().toLowerCase().equals(username.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	
	public void addMember(ProjUser projUser) {
		members.add(projUser);
		projUser.getUser().addProject(projUser);
	}
	public void removeMember(ProjUser projUser) {
		members.remove(projUser);
		projUser.getUser().removeProject(projUser);
	}
	public void setProjmodules(List<ProjModule> projmodules) {
		this.projmodules = projmodules;
	}
	public List<ProjModule> getProjmodules() {
		return projmodules;
	}
	/*private List<ProjModule> sortByName(List<ProjModule> m) {
		List<ProjModule> tt = m;
		for (int i = 0; i < tt.size() - 1; i++) {
			for (int j = i + 1; j < tt.size(); j++) {
				if (tt.get(i).getModule().getName().compareTo(tt.get(j).getModule().getName()) > 0) {
					ProjModule t = tt.get(i);
					tt.set(i, tt.get(j));
					tt.set(j, t);
				}
			}
		}
		return tt;
	}
	*/
	public ProjModule getProjModule(Module module){
		for(ProjModule pm : projmodules){
			if(pm.getModule().equals(module)){
				return pm;
			}
		}
		return null;
	}
	public void addModule(ProjModule module) {
		projmodules.add(module);
	}
	public void removeModule(ProjModule module) {
		getProjmodules().remove(module);
	}
	public ProjModule getProjModuleById(int id) {
		ProjModule temp = null;
		ProjModule rtn = null;
		Iterator<ProjModule> it = getProjmodules().iterator();
		while (it.hasNext()) {
			temp = it.next();
			if (temp.getId() == id) {
				rtn = temp;
				break;
			}
		}
		return rtn;
	}
	public List<User> getUsers() {
		List<ProjUser> pus = getMembers();
		List<User> rtn = new ArrayList<User>();
		for (int i = 0; i < pus.size(); i++) {
			rtn.add(pus.get(i).getUser());
		}
		return rtn;
	}
	public boolean hasMembersByRole(ProjRole pr){
		for(ProjUser pu : members){
			if(pu.getRole().equals(pr))
				return true;
		}
		return false;
	}
	public List<User> getMembersByRole(ProjRole pr){
		List<User> uList = new ArrayList<User>();
		for(ProjUser pu : members){
			if(pu.getRole().equals(pr))
				uList.add(pu.getUser());
		}
		return uList;
	}
	public List<ProjUser> getProjUsersByRole(ProjRole pr){
		List<ProjUser> puList = new ArrayList<ProjUser>();
		for(ProjUser pu : this.getMembers()){
			if(pu.getRole().equals(pr))
				puList.add(pu);
		}
		return puList;
	}
	public ProjType getType() {
		return type;
	}
	public void setType(ProjType type) {
		this.type = type;
	}
	public List<Module> getModules(){
		List<Module> mList = new ArrayList<Module>();
		for(ProjModule pm : projmodules){
			mList.add(pm.getModule());
		}
		return mList;
	}
	public boolean hasModule(String moduleName){
		for(ProjModule pm : projmodules){
			if(pm.getModule().getName().equals(moduleName))
				return true;
		}
		return false;
	}
	public ProjModule getProjModule(String moduleName){
		for(ProjModule pm : projmodules){
			if(pm.getModule().getName().equals(moduleName))
				return pm;
		}
		return null;
	}
	
	@Override
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("description", description);
		j.put("creator", creator==null? null : creator.getId());
		j.put("creatorUsername", creator==null? null : creator.getUsername());
		j.put("editor", editor==null? null : editor.getId());
		j.put("editorUsername", editor==null? null : editor.getUsername());
		j.put("cdate", cdate.getTime());
		j.put("mdate", mdate.getTime());
		j.put("sdate", sdate==null? null : sdate.getTime());
		j.put("edate", edate==null? null : edate.getTime());
		j.put("type", type.getId());
		j.put("typeName", type.getName());
		j.put("courseId", courseId);
		j.put("groupId", groupId);
		j.put("seqNo", seqNo);
		j.put("term", term);
		j.put("courseCode", courseCode);
		j.put("school", school==null? null : school.getId());
		j.put("schoolName", school==null? null : school.getName());
		j.put("status", status.getId());
		j.put("statusName", status.getName());
		j.put("shared", shared);
		j.put("remarks", remarks);
		j.put("lastAccess", lastAccess==null? null : lastAccess.getTime());
		j.put("lastStatusChange", lastStatusChange==null? null : lastStatusChange.getTime());
		j.put("noAutoChangeStatus", noAutoChangeStatus);
		j.put("companyInfo", companyInfo);
		
		
		JSONArray membrs = new JSONArray();
		for(ProjUser pu : members){
			JSONObject jpu = new JSONObject();
			jpu.put("user", pu.getUser().getId());
			jpu.put("userUserName", pu.getUser().getUsername());
			jpu.put("role", pu.getRole().getId());
			jpu.put("roleName", pu.getRole().getName());
			membrs.put(jpu);
		}
		j.put("members", membrs);
		JSONArray projMods = new JSONArray();
		for(ProjModule pm : projmodules){
			JSONObject jpm = new JSONObject();
			jpm.put("module", pm.getModule().getId());
			jpm.put("moduleName", pm.getModule().getName());
			projMods.put(jpm);
		}
		j.put("projmodules", projMods);
		
		JSONArray attFiles = new JSONArray();
		for(ProjectAttachedFile at : attachedFiles){
			JSONObject jat = new JSONObject();
			jat.put("fileName", at.getFileName());
			jat.put("path", at.getPath());
			attFiles.put(jat);
		}
		j.put("attachedFiles", attFiles);
		
		JSONArray kwords = new JSONArray();
		for(String kw : keywords){
			kwords.put(kw);
		}
		j.put("keywords", kwords);
		
		JSONArray projCAOInfos = new JSONArray();
		for(ProjCAOExtraInfo obj : projCAOExtraInfos){
			JSONObject jobj = new JSONObject();
			jobj.put("id", obj.getId());
			jobj.put("caoID", obj.getCaoID());
			projCAOInfos.put(jobj);
		}
		j.put("projCAOExtraInfos", projCAOInfos);
		
		JSONArray projFYPInfos = new JSONArray();
		for(ProjFYPExtraInfo obj : projFYPExtraInfos){
			JSONObject jobj = new JSONObject();
			jobj.put("id", obj.getId());
			jobj.put("fypID", obj.getFypID());
			projFYPInfos.put(jobj);
		}
		j.put("projFYPExtraInfos", projFYPInfos);
		
		return j;
	}
	
	
	public String getSdateDisplay(){
		return Util.formatDateTime(sdate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getEdateDisplay(){
		return Util.formatDateTime(edate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getLastAccessDisplay(){
		if(lastAccess==null)
			return "";
		return Util.formatDateTime(lastAccess);
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public String getDisplayName(){
		//return id + " - " + name;
		//return id;
		return name;
	}
	public String getEncodedDisplayName(){
		return Util.encode(getDisplayName());
	}
	public void setLastAccess(Date lastAccess) {
		this.lastAccess = lastAccess;
	}
	public Date getLastAccess() {
		return lastAccess;
	}
	public void setNumVisit(int numVisit) {
		this.numVisit = numVisit;
	}
	public int getNumVisit() {
		return numVisit;
	}
	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}
	public Date getLastStatusChange() {
		return lastStatusChange;
	}
	public void setNoAutoChangeStatus(boolean noAutoChangeStatus) {
		this.noAutoChangeStatus = noAutoChangeStatus;
	}
	public boolean isNoAutoChangeStatus() {
		return noAutoChangeStatus;
	}
	public void setAttachedFiles(List<ProjectAttachedFile> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}
	public List<ProjectAttachedFile> getAttachedFiles() {
		return attachedFiles;
	}
	public void addAttachFile(ProjectAttachedFile attachedFile){
		attachedFiles.add(attachedFile);
	}
	public void removeAttachFile(ProjectAttachedFile attachedFile){
		attachedFiles.remove(attachedFile);
	}
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
	public Set<String> getKeywords() {
		return keywords;
	}
	public void addKeywords(String keyword) {
		keywords.add(keyword);
	}
	public void removeKeywords(String keyword) {
		keywords.remove(keyword);
	}
	
	
	// The need for a equal() method is discussed at http://www.hibernate.org/109.html
    @Override
    public boolean equals(Object obj) {
    	return (obj == this) || (obj instanceof Project) && id != null 
    			&& id.equals(((Project) obj).getId());
    	
    }
    // The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
    @Override
	public int hashCode() {
		return id == null ? super.hashCode() : getId().hashCode();
	}
	public void setCompanyInfo(String companyInfo) {
		this.companyInfo = companyInfo;
	}
	public String getCompanyInfo() {
		return companyInfo;
	}
	public Set<ProjCAOExtraInfo> getProjCAOExtraInfos() {
		return projCAOExtraInfos;
	}
	public void setProjCAOExtraInfos(Set<ProjCAOExtraInfo> projCAOExtraInfos) {
		this.projCAOExtraInfos = projCAOExtraInfos;
	}
	public Set<ProjFYPExtraInfo> getProjFYPExtraInfos() {
		return projFYPExtraInfos;
	}
	public void setProjFYPExtraInfos(Set<ProjFYPExtraInfo> projFYPExtraInfos) {
		this.projFYPExtraInfos = projFYPExtraInfos;
	}
	
	
	public Set<Announcement> getAnnouncements() {
		return announcements;
	}
	public void setAnnouncements(Set<Announcement> announcements) {
		this.announcements = announcements;
	}
	public Set<Forum> getForums() {
		return forums;
	}
	public void setForums(Set<Forum> forums) {
		this.forums = forums;
	}
	public Set<Schedule> getSchedules() {
		return schedules;
	}
	public void setSchedules(Set<Schedule> schedules) {
		this.schedules = schedules;
	}
	public Set<Budget> getBudgets() {
		return budgets;
	}
	public void setBudgets(Set<Budget> budgets) {
		this.budgets = budgets;
	}
	public Set<Message> getMessages() {
		return messages;
	}
	public void setMessages(Set<Message> messages) {
		this.messages = messages;
	}
	public Set<BackupEntry> getBackupEntrys() {
		return backupEntrys;
	}
	public void setBackupEntrys(Set<BackupEntry> backupEntrys) {
		this.backupEntrys = backupEntrys;
	}
	public Set<LogEntry> getLogEntrys() {
		return logEntrys;
	}
	public void setLogEntrys(Set<LogEntry> logEntrys) {
		this.logEntrys = logEntrys;
	}
	public Set<Elog> getElogs() {
		return elogs;
	}
	public void setElogs(Set<Elog> elogs) {
		this.elogs = elogs;
	}
	public Set<Assessment> getAssessments() {
		return assessments;
	}
	public void setAssessments(Set<Assessment> assessments) {
		this.assessments = assessments;
	}
	
	public Set<Evaluation> getEvaluations() {
		return evaluations;
	}
	public void setEvaluations(Set<Evaluation> evaluations) {
		this.evaluations = evaluations;
	}
	public Set<Profiling> getProfilings() {
		return profilings;
	}
	public void setProfilings(Set<Profiling> profilings) {
		this.profilings = profilings;
	}
	public Set<Resource> getResources() {
		return resources;
	}
	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}
	public Set<Blog> getBlogs() {
		return blogs;
	}
	public void setBlogs(Set<Blog> blogs) {
		this.blogs = blogs;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getTerm() {
		return term;
	}
	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}
	public String getCourseCode() {
		return courseCode;
	}
}
