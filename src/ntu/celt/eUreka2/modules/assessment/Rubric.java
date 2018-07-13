package ntu.celt.eUreka2.modules.assessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.hibernate.annotations.Entity;

import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;
import ntu.celt.eUreka2.modules.backuprestore.JSONable;

@Entity
public class Rubric implements Cloneable, Serializable, JSONable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7142439004994229593L;
	@NonVisual
	private int id;
	@Validate("Required")
	private String name;
	private String des;
	private User owner;
	private boolean shared;
	private boolean master;
	private Date cdate;
	private Date mdate;
	private List<RubricCriteria> criterias = new ArrayList<RubricCriteria>();
	private School school; //null = all school
	private Boolean gmat;
	private Boolean useCmtStrength;
	private Boolean useCmtWeakness;
	private Boolean useCmtOther;
	private String cmtStrength;
	private String cmtWeakness;
	private String cmtOther;
	private List<String> openEndedQuestions = new ArrayList<String>();


//	public Rubric(){
//		super();
//		id =  Util.generateLongID();
//	}
	public Rubric clone(){
		Rubric r = new Rubric();
		//r.setId(id);
		r.setName(name);
		r.setDes(des);
		r.setOwner(owner);
		r.setShared(shared);
		r.setMaster(master);
		r.setCdate(cdate);
		r.setMdate(mdate);
		for(RubricCriteria c : criterias){
			r.addCriteria(c.clone());
		}
		r.setSchool(school);
		r.setGmat(gmat);
		r.setUseCmtStrength(useCmtStrength);
		r.setUseCmtWeakness(useCmtWeakness);
		r.setUseCmtOther(useCmtOther);
		r.setCmtStrength(cmtStrength);
		r.setCmtWeakness(cmtWeakness);
		r.setCmtOther(cmtOther);
		for(String oq : openEndedQuestions){
			r.addOpenEndedQuestions(oq);
		}
		
		return r;
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public Date getCdate() {
		return cdate;
	}
	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public boolean isShared() {
		return shared;
	}
	public boolean isMaster() {
		return master;
	}
	public void setMaster(boolean master) {
		this.master = master;
	}
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public List<RubricCriteria> getCriterias() {
		return criterias;
	}
	public void setCriterias(List<RubricCriteria> criterias) {
		this.criterias = criterias;
	}
	public void addCriteria(RubricCriteria rc){
		criterias.add(rc);
	}
	public void removeCriteria(RubricCriteria rc){
		criterias.remove(rc);
	}
	public String getCdateDisplay(){
		return Util.formatDateTime(cdate);
	}
	public String getMdateDisplay(){
		return Util.formatDateTime(mdate);
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public School getSchool() {
		return school;
	}
	public Boolean getGmat() {
		if(gmat == null)
			return false;
		return gmat;
	}

	public void setGmat(Boolean gmat) {
		this.gmat = gmat;
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

	
	public String getCmtStrength() {
		if(cmtStrength==null)
			return "Strength";
		return cmtStrength;
	}

	public void setCmtStrength(String cmtStrength) {
		this.cmtStrength = cmtStrength;
	}

	public String getCmtWeakness() {
		if(cmtWeakness==null)
			return "Areas to Improve";
		return cmtWeakness;
	}

	public void setCmtWeakness(String cmtWeakness) {
		this.cmtWeakness = cmtWeakness;
	}

	public String getCmtOther() {
		if(cmtOther==null)
			return "Others";
		return cmtOther;
	}

	public void setCmtOther(String cmtOther) {
		this.cmtOther = cmtOther;
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
	@Override
	public JSONObject toJSONObject(){
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("name", name);
		j.put("description", des);
		j.put("owner", owner==null? null : owner.getId());
		j.put("shared", shared);
		j.put("master", master);
		j.put("cdate", cdate.getTime());
		j.put("mdate", mdate.getTime());
		j.put("school", school==null? null : school.getId());
		j.put("schoolName", school==null? null : school.getName());
		j.put("gmat", gmat);
		
		j.put("numRow", criterias==null? 0 : criterias.size());
		j.put("numColumn", (criterias==null || criterias.size()==0 
				|| criterias.get(0).getCriterions()==null)? 0 : criterias.get(0).getCriterions().size());
		
		j.put("numRow", criterias==null? 0 : criterias.size());
		
		/*
		j.put("useCmtStrength", useCmtStrength);
		j.put("useCmtWeakness", useCmtWeakness);
		j.put("useCmtOther", useCmtOther);
		j.put("cmtStrength", cmtStrength);
		j.put("cmtWeakness", cmtWeakness);
		j.put("cmtOther", cmtOther);
		
		JSONArray openEndedQuests = new JSONArray();
		for(String opq : openEndedQuestions){
			openEndedQuests.put(opq);
		}
		j.put("openEndedQuestions", openEndedQuests);
		*/
		
		return j;
	}
}
