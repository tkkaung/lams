package ntu.celt.eUreka2.modules.assessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;

public class AssessCriteria implements Cloneable{
	@NonVisual
	private int id;
	private String name;
	private String des;
	private List<AssessCriterion> criterions = new ArrayList<AssessCriterion>();
	private Assessment assessment;
	private Integer weightage; //if null, = 100/numOfCriteria
	
	public AssessCriteria clone(){
		AssessCriteria a = new AssessCriteria();
		a.setName(name);
		a.setDes(des);
		a.setAssessment(assessment);
		a.setWeightage(weightage);
		for(AssessCriterion ac : criterions){
			a.addCriterion(ac.clone());
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public List<AssessCriterion> getCriterions() {
		return criterions;
	}
	public void setCriterions(List<AssessCriterion> criterions) {
		this.criterions = criterions;
	}
	public void addCriterion(AssessCriterion ac){
		criterions.add(ac);
	}
	public void removeCriterion(AssessCriterion ac){
		criterions.remove(ac);
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public void setWeightage(Integer weightage) {
		this.weightage = weightage;
	}
	public Integer getWeightage() {
		if(weightage==null)
			return 0;
		return weightage;
	}
	public Integer getMaxCritScore(){
		Integer max = 0;
		for(AssessCriterion rcion : criterions){
			if(max<rcion.getScore())
				max = rcion.getScore();
		}
		return max;
	}
	
}
