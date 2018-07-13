package ntu.celt.eUreka2.modules.peerevaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.json.JSONObject;

import ntu.celt.eUreka2.entities.AuditTrail;
import ntu.celt.eUreka2.entities.Project;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.assessment.Rubric;
import ntu.celt.eUreka2.modules.group.Group;

import ntu.celt.eUreka2.modules.backuprestore.JSONable;

public class Evaluation implements Serializable, Cloneable, JSONable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4534944364504122695L;
	@NonVisual
	private long id;
	private String name; 
	private String des;
	private Project project;
	private User creator;
	private User editor;
	private Date cdate;
	private Date mdate;
	private Date sdate;
	private Date edate;
	//private Date rdate;
	private Boolean released = false;
	private Float weightage ;
	
	private int orderNumber;
	
	private Boolean allowSelfEvaluation;
	private boolean allowViewScoredCriteria;  //allow student to see scored rubrics
	private boolean allowViewGrade;	//allow student to see grade
	private Boolean allowViewGradeDetail;	//allow student to see grade detail
	private Boolean allowViewGradeDetailPeer;	//allow student to see grade detail
	
	

	private Rubric rubric;		//just for reference, because criterias is loaded from this
	private List<EvaluationCriteria> criterias = new ArrayList<EvaluationCriteria>();
	private Set<EvaluationUser> evalUsers = new HashSet<EvaluationUser>();
	private Set<Integer> daysToRemind = new HashSet<Integer>();
	private Set<Integer> daysToRemindInstructor = new HashSet<Integer>();
	private Group group;
	
	private Boolean reminderLaunch = true;
	
	private String customNameCmtStrength;
	private String customNameCmtWeakness;
	private String customNameCmtOther;
	private Boolean useCmtStrength;
	private Boolean useCmtWeakness;
	private Boolean useCmtOther;
	
	
	private List<String> openEndedQuestions = new ArrayList<String>();
	private List<EvalAssesseeView> evalAssesseeViews = new ArrayList<EvalAssesseeView>();

	private Boolean useFixedPoint = false;
	private Integer totalFixedPoint = 0;  //for 2 person per group
	private Integer totalFixedPoint3 = 0; //for 3 person per group
	private Integer totalFixedPoint4 = 0; //for 4 person per group
	private Integer totalFixedPoint5 = 0;
	private Integer totalFixedPoint6 = 0;
	private Integer totalFixedPoint7 = 0;
	private Integer totalFixedPoint8 = 0;
	private Integer totalFixedPoint9 = 0;
	private Integer totalFixedPoint10 = 0;
	
	
	
	public Evaluation(){
		super();
		id = Util.generateLongID();
	}
	
	
	
	
	public Evaluation clone(){
		Evaluation e = new Evaluation();
		e.setName(name);
		e.setDes(des);
		e.setProject(project);
		e.setCreator(creator);
		e.setEditor(editor);
		e.setCdate(cdate);
		e.setMdate(mdate);
		e.setSdate(sdate);
		e.setEdate(edate);
		e.setReleased(released);
		e.setWeightage(weightage);
		e.setOrderNumber(orderNumber);
		
		e.setAllowSelfEvaluation(allowSelfEvaluation);
		e.setAllowViewGrade(allowViewGrade);
		e.setAllowViewGradeDetail(allowViewGradeDetail);
		e.setAllowViewGradeDetailPeer(allowViewGradeDetailPeer);
		e.setAllowViewScoredCriteria(allowViewScoredCriteria);
		

		if(rubric != null)
			e.setRubric(rubric.clone());
		for(EvaluationCriteria ac : criterias){
			e.addCriteria(ac.clone());
		}
		for(Integer i : daysToRemind){
			e.addDaysToRemind(i);
		}
		for(Integer i : daysToRemindInstructor){
			e.addDaysToRemindInstructor(i);
		}
		e.setGroup(group);
		e.setReminderLaunch(reminderLaunch);
		e.setCustomNameCmtStrength(customNameCmtStrength);
		e.setCustomNameCmtWeakness(customNameCmtWeakness);
		e.setCustomNameCmtOther(customNameCmtOther);
		e.setUseCmtStrength(useCmtStrength);
		e.setUseCmtWeakness(useCmtWeakness);
		e.setUseCmtOther(useCmtOther);
		
		for(String str : openEndedQuestions){
			e.addOpenEndedQuestions(str);
		}
		
		e.setUseFixedPoint(useFixedPoint);
		e.setTotalFixedPoint(totalFixedPoint);
		e.setTotalFixedPoint3(totalFixedPoint3);
		e.setTotalFixedPoint4(totalFixedPoint4);
		e.setTotalFixedPoint5(totalFixedPoint5);
		e.setTotalFixedPoint6(totalFixedPoint6);
		e.setTotalFixedPoint7(totalFixedPoint7);
		e.setTotalFixedPoint8(totalFixedPoint8);
		e.setTotalFixedPoint9(totalFixedPoint9);
		e.setTotalFixedPoint10(totalFixedPoint10);
		
		return e;
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	
	public Date getSdate() {
		return sdate;
	}

	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}

	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public Date getEdate() {
		return edate;
	}
	/*public void setRdate(Date rdate) {
		this.rdate = rdate;
	}

	public Date getRdate() {
		return rdate;
	}*/

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	
	public Rubric getRubric() {
		return rubric;
	}
	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}
	
	public void setCriterias(List<EvaluationCriteria> criterias) {
		/*if (this.criterias == null) {
		    this.criterias = criterias;
		  } else {
		    this.criterias.retainAll(criterias);
		   this.criterias.addAll(criterias);
		  }*/
		this.criterias = criterias;
		//this.criterias.clear();
	    //this.criterias.addAll(criterias);
	}
	public List<EvaluationCriteria> getCriterias() {
		return criterias;
	}
	public void addCriteria(EvaluationCriteria ac){
		criterias.add(ac);
	}
	public void removeCriteria(EvaluationCriteria ac){
		criterias.remove(ac);
	}
	
	
	public void setEvalUsers(Set<EvaluationUser> assmtUsers) {
		this.evalUsers = assmtUsers;
		//this.evalUsers.clear();
		//this.evalUsers.addAll(assmtUsers);
	}
	public Set<EvaluationUser> getEvalUsers() {
		return evalUsers;
	}
	public void addEvalUsers(EvaluationUser assmtUser){
		evalUsers.add(assmtUser);
	}
	public void removeEvalUsers(EvaluationUser assmtUser){
		evalUsers.remove(assmtUser);
	}
	public List<EvaluationUser> getEvalUsersByAssessee(User Assessee){
		List<EvaluationUser> eu = new ArrayList<EvaluationUser>();
		for(EvaluationUser au : evalUsers){
			if(au!=null && au.getAssessee()!=null && Assessee.equals(au.getAssessee()))
				eu.add(au);
		}
		return eu;
	}
	public List<EvaluationUser> getEvalUsersByAssessor(User Assessor){
		List<EvaluationUser> eu = new ArrayList<EvaluationUser>();
		for(EvaluationUser au : evalUsers){
			if(au.getAssessee()!=null && Assessor.equals(au.getAssessor()))
				eu.add(au);
		}
		return eu;
	}
	public EvaluationUser getEvalUserByAssessee(List<EvaluationUser> euList, User assessee){
		if(euList==null || assessee==null)
			return null;
		for(EvaluationUser au : euList){
			if(au.getAssessee()!=null && assessee.equals(au.getAssessee()))
				return au;
		}
		return null;
	}
	public EvaluationUser getEvalUserByAssessor(List<EvaluationUser> euList, User Assessor){
		if(euList==null)
			return null;
		for(EvaluationUser au : euList){
			if(au.getAssessee()!=null && Assessor.equals(au.getAssessor()))
				return au;
		}
		return null;
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
/*	public String getRdateDisplay() {
		return Util.formatDateTime(rdate, Config.getString(Config.FORMAT_DATE_PATTERN));
	}
*/	public boolean isDiffEditor(){
		if(editor==null || editor.equals(creator))
			return false;
		return true;
	}

	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof Evaluation) && obj != null 
				&& (((Evaluation) obj).getId() == id);
		
	}
	// The need for a hashCode() method is discussed at http://www.hibernate.org/109.html
	@Override
	public int hashCode() {
		return (id + "").hashCode();
	}



	public void setGroup(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return group;
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
	public Boolean getAllowViewGradeDetail() {
		if(allowViewGradeDetail==null)
			return false;
		return allowViewGradeDetail;
	}

	public void setAllowViewGradeDetail(Boolean allowViewGradeDetail) {
		this.allowViewGradeDetail = allowViewGradeDetail;
	}
	public Boolean getAllowViewGradeDetailPeer() {
		if(allowViewGradeDetailPeer==null)
			return false;
		return allowViewGradeDetailPeer;
	}

	public void setAllowViewGradeDetailPeer(Boolean allowViewGradeDetailPeer) {
		this.allowViewGradeDetailPeer = allowViewGradeDetailPeer;
	}
	
	

	public void setDaysToRemind(Set<Integer> daysEarlierToRemind) {
		this.daysToRemind = daysEarlierToRemind;
	}
	public Set<Integer> getDaysToRemind() {
		return daysToRemind;
	}
	public void addDaysToRemind(Integer numDay){
		daysToRemind.add(numDay);
	}
	public void removeDaysToRemind(Integer numDay){
		daysToRemind.remove(numDay);
	}
	
	public void setDaysToRemindInstructor(Set<Integer> daysEarlierToRemind) {
		this.daysToRemindInstructor = daysEarlierToRemind;
	}
	public Set<Integer> getDaysToRemindInstructor() {
		return daysToRemindInstructor;
	}
	public void addDaysToRemindInstructor(Integer numDay){
		daysToRemindInstructor.add(numDay);
	}
	public void removeDaysToRemindInstructor(Integer numDay){
		daysToRemindInstructor.remove(numDay);
	}

	public void setAllowSelfEvaluation(Boolean allowSelfEvaluation) {
		this.allowSelfEvaluation = allowSelfEvaluation;
	}

	public Boolean getAllowSelfEvaluation() {
		return allowSelfEvaluation;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public Boolean getReleased() {
		if(released==null)
			return false;
		return released;
	}

	public void setWeightage(Float weightage) {
		this.weightage = weightage;
	}

	public Float getWeightage() {
		if(weightage==null)
			return (float) 0.0;
		return weightage;
	}

	public void setReminderLaunch(Boolean reminderLaunch) {
		this.reminderLaunch = reminderLaunch;
	}

	public Boolean getReminderLaunch() {
		return reminderLaunch;
	}

	public String getCustomNameCmtStrength() {
		return customNameCmtStrength;
	}

	public void setCustomNameCmtStrength(String customNameCmtStrength) {
		this.customNameCmtStrength = customNameCmtStrength;
	}

	public String getCustomNameCmtWeakness() {
		return customNameCmtWeakness;
	}

	public void setCustomNameCmtWeakness(String customNameCmtWeakness) {
		this.customNameCmtWeakness = customNameCmtWeakness;
	}

	public String getCustomNameCmtOther() {
		return customNameCmtOther;
	}

	public void setCustomNameCmtOther(String customNameCmtOther) {
		this.customNameCmtOther = customNameCmtOther;
	}
	
	
	public Boolean getUseCmtStrength() {
		if(useCmtStrength==null)
			return true;
		return useCmtStrength;
	}

	public void setUseCmtStrength(Boolean useCmtStrength) {
		this.useCmtStrength = useCmtStrength;
	}

	public Boolean getUseCmtWeakness() {
		if(useCmtWeakness==null)
			return true;
		return useCmtWeakness;
	}

	public void setUseCmtWeakness(Boolean useCmtWeakness) {
		this.useCmtWeakness = useCmtWeakness;
	}

	public Boolean getUseCmtOther() {
		if(useCmtOther==null)
			return true;
		return useCmtOther;
	}

	public void setUseCmtOther(Boolean useCmtOther) {
		this.useCmtOther = useCmtOther;
	}

	
	public List<String> getOpenEndedQuestions() {
		return openEndedQuestions;
	}

	public void setOpenEndedQuestions(List<String> openEndedQuestions) {
		this.openEndedQuestions = openEndedQuestions;
	}
	public void addOpenEndedQuestions(String rq){
		openEndedQuestions.add(rq);
	}
	public void removeOpenEndedQuestions(String rq){
		openEndedQuestions.remove(rq);
	}

	public void setEvalAssesseeViews(List<EvalAssesseeView> evalAssesseeViews) {
		this.evalAssesseeViews = evalAssesseeViews;
	}

	public List<EvalAssesseeView> getEvalAssesseeViews() {
		return evalAssesseeViews;
	}
	
	public void addEvalAssesseeViews(EvalAssesseeView evalAssesseeView) {
		this.evalAssesseeViews.add(evalAssesseeView);
	}

	public void removeEvalAssesseeViews(EvalAssesseeView evalAssesseeView) {
		this.evalAssesseeViews.remove(evalAssesseeView);
	}

	public AuditTrail getDifferent(Evaluation o2, AuditTrail ad){

		if(!this.name.equals(o2.getName())){
			ad.appendPrevValue("Name: " + o2.getName());
			ad.appendNewValue("Name: " + name);
		}
		if(!Util.nvl(des).equals(Util.nvl(o2.getDes()))){
			ad.appendPrevValue("Description: " + Util.nvl(o2.getDes()));
			ad.appendNewValue("Description: " + Util.nvl(des));
		}
		if(!sdate.equals(o2.getSdate())){
			ad.appendPrevValue("Start Date: " + o2.getSdateDisplay());
			ad.appendNewValue("Start Date: " + getSdateDisplay());
		}
		if(!edate.equals(o2.getEdate())){
			ad.appendPrevValue("End Date: " + o2.getEdateDisplay());
			ad.appendNewValue("End Date: " + getEdateDisplay());
		}
		if(this.released != o2.getReleased()){
			ad.appendPrevValue("Released: " + o2.getReleased());
			ad.appendNewValue("Released: " + getReleased());
		}

		if(!weightage.equals(o2.getWeightage())){
			ad.appendPrevValue("Weight: " + o2.getWeightage());
			ad.appendNewValue("Weight: " + weightage);
		}
		
		if(!(group==null ? "" : group).equals(o2.getGroup() == null ? "" : o2.getGroup())){
			ad.appendPrevValue("Group: " + (o2.getGroup()==null ? "--Random Group--" : o2.getGroup().getGroupType()));
			ad.appendNewValue("Group: " + (group==null ? "--Random Group--" : group.getGroupType()) );
		}
		
		if(!this.allowSelfEvaluation.equals(o2.getAllowSelfEvaluation())){
			ad.appendPrevValue("Allow Self Evaluation: " + o2.getAllowSelfEvaluation());
			ad.appendNewValue("Allow Self Evaluation: " + getAllowSelfEvaluation());
		}
		if(this.allowViewGrade != (o2.isAllowViewGrade())){
			ad.appendPrevValue("Allow View Grade: " + o2.isAllowViewGrade());
			ad.appendNewValue("Allow View Grade: " + isAllowViewGrade());
		}
		if(this.allowViewScoredCriteria != (o2.isAllowViewScoredCriteria())){
			ad.appendPrevValue("Allow View Scored Rubrics: " + o2.isAllowViewScoredCriteria());
			ad.appendNewValue("Allow View Scored Rubrics: " + isAllowViewScoredCriteria());
		}
		if(!this.allowViewGradeDetail.equals(o2.getAllowViewGradeDetail())){
			ad.appendPrevValue("Allow View Grade Detail: " + o2.getAllowViewGradeDetail());
			ad.appendNewValue("Allow View Grade Detail: " + getAllowViewGradeDetail());
		}
		if(!this.allowViewGradeDetailPeer.equals(o2.getAllowViewGradeDetailPeer())){
			ad.appendPrevValue("Allow View Grade Detail Evaluator: " + o2.getAllowViewGradeDetailPeer());
			ad.appendNewValue("Allow View Grade Detail Evaluator: " + getAllowViewGradeDetailPeer());
		}
		
		
		String daysToRemind1 = Util.setToOrderedString(daysToRemind, ","); 
		String daysToRemind2 = Util.setToOrderedString(o2.getDaysToRemind(), ","); 
		if(!daysToRemind1.equals(daysToRemind2)){
			ad.appendPrevValue("Days to Remind Students: " + daysToRemind1);
			ad.appendNewValue("Days to Remind Students: " + daysToRemind2);
		}
		
		String daysToRemindInstructor1 = Util.setToOrderedString(daysToRemindInstructor, ","); 
		String daysToRemindInstructor2 = Util.setToOrderedString(o2.getDaysToRemindInstructor(), ","); 
		if(!daysToRemindInstructor1.equals(daysToRemindInstructor2)){
			ad.appendPrevValue("Days to Remind Instructor: " + daysToRemindInstructor1);
			ad.appendNewValue("Days to Remind Instructor: " + daysToRemindInstructor2);
		}

		if(!getReminderLaunch().equals(o2.getReminderLaunch())){
			ad.appendPrevValue("Reminder Launch: " + o2.getReminderLaunch());
			ad.appendNewValue("Reminder Launch: " + getReminderLaunch());
		}
		if(!getCustomNameCmtStrength().equals(o2.getCustomNameCmtStrength())){
			ad.appendPrevValue("Qualitative Feedback 1: " + o2.getCustomNameCmtStrength());
			ad.appendNewValue("Qualitative Feedback 2: " + getCustomNameCmtStrength());
		}
		if(!getCustomNameCmtWeakness().equals(o2.getCustomNameCmtWeakness())){
			ad.appendPrevValue("Qualitative Feedback 2: " + o2.getCustomNameCmtWeakness());
			ad.appendNewValue("Qualitative Feedback 2: " + getCustomNameCmtWeakness());
		}
		if(!getCustomNameCmtOther().equals(o2.getCustomNameCmtOther())){
			ad.appendPrevValue("Qualitative Feedback 3: " + o2.getCustomNameCmtOther());
			ad.appendNewValue("Qualitative Feedback 3: " + getCustomNameCmtOther());
		}
		if(!getUseCmtStrength().equals(o2.getUseCmtStrength())){
			ad.appendPrevValue("Use Qualitative Feedback 1: " + o2.getUseCmtStrength());
			ad.appendNewValue("Use Qualitative Feedback 1: " + getUseCmtStrength());
		}
		if(!getUseCmtWeakness().equals(o2.getUseCmtWeakness())){
			ad.appendPrevValue("Use Qualitative Feedback 2: " + o2.getUseCmtWeakness());
			ad.appendNewValue("Use Qualitative Feedback 2: " + getUseCmtWeakness());
		}
		if(!getUseCmtOther().equals(o2.getUseCmtOther())){
			ad.appendPrevValue("Use Qualitative Feedback 3: " + o2.getUseCmtOther());
			ad.appendNewValue("Use Qualitative Feedback 3: " + getUseCmtOther());
		}

		
//		for(String str : openEndedQuestions){
//			e.addOpenEndedQuestions(str);
//		}
		
		return ad;
	}




	public void setUseFixedPoint(Boolean useFixedPoint) {
		this.useFixedPoint = useFixedPoint;
	}




	public Boolean getUseFixedPoint() {
		if(useFixedPoint == null)
			return false;
		return useFixedPoint;
	}




	public void setTotalFixedPoint(Integer totalFixedPoint) {
		this.totalFixedPoint = totalFixedPoint;
	}

	public Integer getTotalFixedPoint() {
		if(totalFixedPoint == null)
			return 0;
		return totalFixedPoint;
	}
	public void setTotalFixedPoint3(Integer totalFixedPoint) {
		this.totalFixedPoint3 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint3() {
		if(totalFixedPoint3 == null)
			return 0;
		return totalFixedPoint3;
	}
	public void setTotalFixedPoint4(Integer totalFixedPoint) {
		this.totalFixedPoint4 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint4() {
		if(totalFixedPoint4 == null)
			return 0;
		return totalFixedPoint4;
	}
	public void setTotalFixedPoint5(Integer totalFixedPoint) {
		this.totalFixedPoint5 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint5() {
		if(totalFixedPoint5 == null)
			return 0;
		return totalFixedPoint5;
	}
	public void setTotalFixedPoint6(Integer totalFixedPoint) {
		this.totalFixedPoint6 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint6() {
		if(totalFixedPoint6 == null)
			return 0;
		return totalFixedPoint6;
	}
	public void setTotalFixedPoint7(Integer totalFixedPoint) {
		this.totalFixedPoint7 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint7() {
		if(totalFixedPoint7 == null)
			return 0;
		return totalFixedPoint7;
	}
	public void setTotalFixedPoint8(Integer totalFixedPoint) {
		this.totalFixedPoint8 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint8() {
		if(totalFixedPoint8 == null)
			return 0;
		return totalFixedPoint8;
	}
	public void setTotalFixedPoint9(Integer totalFixedPoint) {
		this.totalFixedPoint9 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint9() {
		if(totalFixedPoint9 == null)
			return 0;
		return totalFixedPoint9;
	}
	public void setTotalFixedPoint10(Integer totalFixedPoint) {
		this.totalFixedPoint10 = totalFixedPoint;
	}

	public Integer getTotalFixedPoint10() {
		if(totalFixedPoint10 == null)
			return 0;
		return totalFixedPoint10;
	}
	public Integer getTotalFixedPointByNum(Integer numPerGroup){
		switch(numPerGroup){
		case 2:
			return totalFixedPoint;
		case 3:
			return totalFixedPoint3;
		case 4:
			return totalFixedPoint4;
		case 5:
			return totalFixedPoint5;
		case 6:
			return totalFixedPoint6;
		case 7:
			return totalFixedPoint7;
		case 8:
			return totalFixedPoint8;
		case 9:
			return totalFixedPoint9;
		case 10:
			return totalFixedPoint10;
		}
		return 0;
	}
	


	
	@Override
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
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
		j.put("released", released);
		j.put("orderNumber", orderNumber);
		j.put("weightage", weightage);

		j.put("rubric", rubric==null? null : rubric.getId());
		j.put("rubricName", rubric==null? null : rubric.getName());

		j.put("groupId", group==null? null : group.getId());
		j.put("groupName", group==null? null : group.getGroupType());
		
		j.put("allowViewGrade", allowViewGrade);
		j.put("allowViewGradeDetail", allowViewGradeDetail);
		j.put("allowViewGradeDetailPeer", allowViewGradeDetailPeer);
		j.put("allowSelfEvaluation", allowSelfEvaluation);
		j.put("numGraded", evalUsers==null ? 0 : evalUsers.size() );
				
		j.put("useFixedPoint", useFixedPoint);
		j.put("totalFixedPoint", totalFixedPoint);
		j.put("totalFixedPoint3", totalFixedPoint3);
		j.put("totalFixedPoint4", totalFixedPoint4);
		j.put("totalFixedPoint5", totalFixedPoint5);
		j.put("totalFixedPoint6", totalFixedPoint6);
		j.put("totalFixedPoint7", totalFixedPoint7);
		j.put("totalFixedPoint8", totalFixedPoint8);
		j.put("totalFixedPoint9", totalFixedPoint9);
		j.put("totalFixedPoint10", totalFixedPoint10);
		
		return j;
	}


}
