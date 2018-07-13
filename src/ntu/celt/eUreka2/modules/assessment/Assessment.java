package ntu.celt.eUreka2.modules.assessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.group.Group;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Assessment implements Cloneable, Serializable, JSONable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6757991973120306518L;
	@NonVisual
	private int id;
	private String name; 
	private String shortName; //if empty, 'Name' is used instead
	private String des;
	private Project project;
	private User creator;
	private User editor;
	private Date cdate;
	private Date mdate;
	private Date sdate;
	private Date edate;
	private Date rdate;
	
	private int orderNumber;
	private boolean allowViewGradeCriteria = true;  //allow student to see assess criterias
	private boolean allowViewScoredCriteria = true;  //allow student to see scored rubrics
	private boolean allowViewGrade = true;	//allow student to see grade
	private Boolean allowViewGradeDetail = true;	//allow student to see grade detail (e.g which rubric matric cell was choosen)
	private Boolean allowSubmitFile;	//allow student submit file
	private Boolean allowViewComment = true;	//allow student view comments
	private Boolean allowReleaseResult = true;  
	private Boolean allowViewCommentRightAway;
	
	
	private int possibleScore = 100; //default to 100
	private Rubric rubric;		//just for reference, because criterias is loaded from this
	private List<AssessCriteria> criterias = new ArrayList<AssessCriteria>();
	private List<AssessmentUser> assmtUsers = new ArrayList<AssessmentUser>();
	private Float weightage; 
	private Group group;
	private Boolean gmat;
	
	public Assessment clone(){
		Assessment a = new Assessment();
		//a.setId(id);
		a.setName(name);
		a.setShortName(shortName);
		a.setDes(des);
		a.setProject(project);
		a.setCreator(creator);
		a.setEditor(editor);
		a.setCdate(cdate);
		a.setMdate(mdate);
		a.setSdate(sdate);
		a.setEdate(edate);
		a.setRdate(rdate);
		a.setOrderNumber(orderNumber);
		a.setAllowViewGradeCriteria(allowViewGradeCriteria);
		a.setAllowViewScoredCriteria(allowViewScoredCriteria);
		a.setAllowViewGrade(allowViewGrade);
		a.setAllowViewGradeDetail(allowViewGradeDetail);
		a.setAllowSubmitFile(allowSubmitFile);
		a.setAllowViewComment(allowViewComment);
		a.setAllowViewCommentRightAway(allowViewCommentRightAway);
		a.setAllowReleaseResult(allowReleaseResult);
		
		a.setPossibleScore(possibleScore);
		a.setWeightage(weightage);
		a.setGmat(gmat);
		a.setGroup(group);
		
		if(rubric!=null)
			a.setRubric(rubric.clone());
		for(AssessCriteria ac : criterias){
			a.addCriteria(ac.clone());
		}
		for(AssessmentUser au : assmtUsers){
			a.addAssmtUsers(au.clone());
		}
		
		return a;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
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
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getSdate() {
		return sdate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public Date getEdate() {
		return edate;
	}
	public void setRdate(Date rdate) {
		this.rdate = rdate;
	}

	public Date getRdate() {
		return rdate;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	
	public boolean isAllowView() {
		if(allowViewGrade || allowViewGradeCriteria || allowViewScoredCriteria || allowViewGradeDetail)
			return true;
		return false;
	}
	public boolean isAllowViewGradeCriteria() {
		return allowViewGradeCriteria;
	}
	public void setAllowViewGradeCriteria(boolean allowViewGradeCriteria) {
		this.allowViewGradeCriteria = allowViewGradeCriteria;
	}
	public boolean isAllowViewScoredCriteria() {
		return allowViewScoredCriteria;
	}
	public void setAllowViewScoredCriteria(boolean allowViewScoredCriteria) {
		this.allowViewScoredCriteria = allowViewScoredCriteria;
	}
	public boolean isAllowViewGrade() {
		return allowViewGrade;
	}
	public void setAllowViewGrade(boolean allowViewGrade) {
		this.allowViewGrade = allowViewGrade;
	}
	public void setAllowViewGradeDetail(Boolean allowViewGradeDetail) {
		this.allowViewGradeDetail = allowViewGradeDetail;
	}
	public Boolean getAllowViewGradeDetail() {
		if(allowViewGradeDetail==null)
			allowViewGradeDetail = true;
		return allowViewGradeDetail;
	}

	public void setPossibleScore(int possibleScore) {
		this.possibleScore = possibleScore;
	}
	public int getPossibleScore() {
		return possibleScore;
	}
	public Rubric getRubric() {
		return rubric;
	}
	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}
	
	public void setCriterias(List<AssessCriteria> criterias) {
		this.criterias = criterias;
		//this.criterias.clear();
	    //this.criterias.addAll(criterias);
	}
	public List<AssessCriteria> getCriterias() {
		return criterias;
	}
	public void addCriteria(AssessCriteria ac){
		criterias.add(ac);
	}
	public void removeCriteria(AssessCriteria ac){
		criterias.remove(ac);
	}
	
	
	public void setAssmtUsers(List<AssessmentUser> assmtUsers) {
		this.assmtUsers = assmtUsers;
	}
	public List<AssessmentUser> getAssmtUsers() {
		return assmtUsers;
	}
	public void addAssmtUsers(AssessmentUser assmtUser){
		assmtUsers.add(assmtUser);
	}
	public void removeAssmtUsers(AssessmentUser assmtUser){
		assmtUsers.remove(assmtUser);
	}
	
	public AssessmentUser getAssmtUser(User user){
		for(AssessmentUser au : assmtUsers){
			if(au!=null && au.getAssessee().equals(user))
				return au;
		}
		return null;
	}
	
	public String getShortNameDisplay(){
		if(shortName!=null && !shortName.isEmpty())
			return shortName; 
		return name;
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}
	public String getSdateDisplay(){
		return Util.formatDateTime(sdate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getEdateDisplay(){
		return Util.formatDateTime(edate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public String getRdateDisplay() {
		if(rdate==null) return "";
		return Util.formatDateTime(rdate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
	public boolean isDiffEditor(){
		if(editor==null || editor.equals(creator))
			return false;
		return true;
	}

	public void setWeightage(Float weightage) {
		this.weightage = weightage;
	}
	public Float getWeightage() {
		if(weightage==null)
			return (float) 0.0;
		return weightage;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}

	public void setAllowSubmitFile(Boolean allowSubmitFile) {
		if(allowSubmitFile==null)
			allowSubmitFile = false;
		this.allowSubmitFile = allowSubmitFile;
	}

	public Boolean getAllowSubmitFile() {
		return allowSubmitFile;
	}
	
	public void setAllowViewComment(Boolean allowViewComment) {
		this.allowViewComment = allowViewComment;
	}
	public Boolean getAllowViewComment() {
		if(allowViewComment==null)
			allowViewComment = true;
		return allowViewComment;
	}
	
	public void setAllowViewCommentRightAway(Boolean allowViewCommentRightAway) {
		
		this.allowViewCommentRightAway = allowViewCommentRightAway;
	}
	public Boolean getAllowViewCommentRightAway() {
		if(allowViewCommentRightAway==null)
			allowViewCommentRightAway = false;
		return allowViewCommentRightAway;
	}
	public void setAllowReleaseResult(Boolean allowReleaseResult) {
		this.allowReleaseResult = allowReleaseResult;
	}
	public Boolean getAllowReleaseResult() {
		if(allowReleaseResult==null)
			allowReleaseResult = true;
		return allowReleaseResult;
	}
	
	public Boolean getGmat() {
		if(gmat == null)
			return false;
		return gmat;
	}

	public void setGmat(Boolean gmat) {
		this.gmat = gmat;
	}

	
	
	public AuditTrail getDifferent(Assessment o2, AuditTrail ad){
		if(!this.name.equals(o2.getName())){
			ad.appendPrevValue("Name: " + o2.getName());
			ad.appendNewValue("Name: " + name);
		}
		if(!Util.nvl(shortName).equals(Util.nvl(o2.getShortName()))){
			ad.appendPrevValue("Short Name: " + Util.nvl(o2.getShortName()));
			ad.appendNewValue("Short Name: " + Util.nvl(shortName));
		}
		if(!Util.nvl(des).equals(Util.nvl(o2.getDes()))){
			ad.appendPrevValue("Description: " + Util.nvl(o2.getDes()));
			ad.appendNewValue("Description: " + Util.nvl(des));
		}
		if(!weightage.equals(o2.getWeightage())){
			ad.appendPrevValue("Weight: " + o2.getWeightage());
			ad.appendNewValue("Weight: " + weightage);
		}
		
		if(!(group==null ? "" : group).equals(o2.getGroup() == null ? "" : o2.getGroup())){
			ad.appendPrevValue("Group: " + (o2.getGroup()==null ? "--No Group--" : o2.getGroup().getGroupType()));
			ad.appendNewValue("Group: " + (group==null ? "--No Group--" : group.getGroupType()) );
		}
		if(!(rdate==null ? "" : rdate).equals(o2.getRdate() == null ? "" : o2.getRdate())){
			ad.appendPrevValue("Release Result Date: " + o2.getRdateDisplay());
			ad.appendNewValue("Release Result Date: " + getRdateDisplay());
		}
		if(!this.allowSubmitFile.equals(o2.getAllowSubmitFile())){
			ad.appendPrevValue("Allow File Submission: " + o2.getAllowSubmitFile());
			ad.appendNewValue("Allow File Submission: " + getAllowSubmitFile());
		}
		if(this.allowViewGradeCriteria != o2.isAllowViewGradeCriteria()){
			ad.appendPrevValue("Allow View Rubrics: " + o2.isAllowViewGradeCriteria());
			ad.appendNewValue("Allow View Rubrics: " + isAllowViewGradeCriteria());
		}
		if(this.allowViewScoredCriteria != o2.isAllowViewScoredCriteria()){
			ad.appendPrevValue("Allow View Scored Rubrics: " + o2.isAllowViewScoredCriteria());
			ad.appendNewValue("Allow View Scored Rubrics: " + isAllowViewScoredCriteria());
		}
		if(this.allowViewGrade != o2.isAllowViewGrade()){
			ad.appendPrevValue("Allow View Grade: " + o2.isAllowViewGrade());
			ad.appendNewValue("Allow View Grade: " + isAllowViewGrade());
		}
		if(!this.getAllowViewComment().equals(o2.getAllowViewComment())){
			ad.appendPrevValue("Allow View Comments: " + o2.getAllowViewComment());
			ad.appendNewValue("Allow View Comments: " + getAllowViewComment());
		}
		if(!this.getAllowViewCommentRightAway().equals(o2.getAllowViewCommentRightAway())){
			ad.appendPrevValue("Allow View Comments Right Away: " + o2.getAllowViewCommentRightAway());
			ad.appendNewValue("Allow View Comments Right Away: " + getAllowViewCommentRightAway());
		}
		if(!this.getAllowReleaseResult().equals(o2.getAllowReleaseResult())){
			ad.appendPrevValue("Release Result: " + o2.getAllowReleaseResult());
			ad.appendNewValue("Release Result: " + getAllowReleaseResult());
		}
		
		
		return ad;
	}
	
	
	
	
	@Override
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("shortName", shortName);
		j.put("description", des);
		j.put("creator", creator==null? null : creator.getId());
		j.put("creatorUsername", creator==null? null : creator.getUsername());
		j.put("editor", editor==null? null : editor.getId());
		j.put("project", project==null? null : project.getId());
		j.put("projectName", project==null? null : project.getName());
		j.put("cdate", cdate.getTime());
		j.put("mdate", mdate.getTime());
		j.put("sdate", sdate==null? null : sdate.getTime());
		j.put("edate", edate==null? null : edate.getTime());
		j.put("rdate", rdate==null? null : rdate.getTime());
		j.put("orderNumber", orderNumber);
		j.put("possibleScore", possibleScore);
		j.put("weightage", weightage);
		j.put("gmat", gmat);
		j.put("rubric", rubric==null? null : rubric.getId());
		j.put("rubricName", rubric==null? null : rubric.getName());
		
		j.put("groupId", group==null? null : group.getId());
		j.put("groupName", group==null? null : group.getGroupType());
		
		j.put("allowViewGradeCriteria", allowViewGradeCriteria);
		j.put("allowViewScoredCriteria", allowViewScoredCriteria);
		j.put("allowViewGrade", allowViewGrade);
		j.put("allowViewGradeDetail", allowViewGradeDetail);
		j.put("allowSubmitFile", allowSubmitFile);
		j.put("allowViewComment", allowViewComment);
		j.put("allowReleaseResult", allowReleaseResult);
		j.put("allowViewCommentRightAway", allowViewCommentRightAway);
		
		
		j.put("numGraded", assmtUsers==null ? 0 : assmtUsers.size() );
				
		return j;
	}
	
	
}
