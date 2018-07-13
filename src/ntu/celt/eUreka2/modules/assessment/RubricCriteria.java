package ntu.celt.eUreka2.modules.assessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Entity;

@Entity
public class RubricCriteria implements Cloneable{
	@NonVisual
	private int id;
	private String name;
	private String des;
	private List<RubricCriterion> criterions = new ArrayList<RubricCriterion>();
	private Rubric rubric;
	private Integer weightage; //if null, = 100/numOfCriteria
	
	@Override
	public RubricCriteria clone(){
		RubricCriteria rc = new RubricCriteria();
		//rc.setId(id);
		rc.setName(name);
		rc.setDes(des);
		rc.setWeightage(weightage);
		rc.setRubric(rubric);
		for(RubricCriterion rcion : criterions){
			rc.addCriterion(rcion.clone());
		}
		
		return rc;
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
	public List<RubricCriterion> getCriterions() {
		return criterions;
	}
	public void setCriterions(List<RubricCriterion> criterions) {
		this.criterions = criterions;
	}
	public void addCriterion(RubricCriterion rc){
		criterions.add(rc);
	}
	public void removeCriterion(RubricCriterion rc){
		criterions.remove(rc);
	}
	public Rubric getRubric() {
		return rubric;
	}
	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}

	public void setWeightage(Integer weightage) {
		this.weightage = weightage;
	}
	public Integer getWeightage() {
		if(weightage==null)
			return 0;
		return weightage;
	}

	
	
	
	
}
