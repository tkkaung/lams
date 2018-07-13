package ntu.celt.eUreka2.modules.peerevaluation;

import java.util.ArrayList;
import java.util.List;

import ntu.celt.eUreka2.internal.Util;

import org.apache.tapestry5.beaneditor.NonVisual;

public class EvaluationCriteria implements Cloneable {
	@NonVisual
	private Long id;
	private String name;
	private String des;
	private List<EvaluationCriterion> criterions = new ArrayList<EvaluationCriterion>();
	private Evaluation evaluation;
	private Integer weightage; //if null, = 100/numOfCriteria
	private Integer maxScore;
	private Integer minScore;
	
	
	public EvaluationCriteria(){
		super();
		id = Util.generateLongID();
	}
	
	public EvaluationCriteria clone(){
		EvaluationCriteria a = new EvaluationCriteria();
		a.setName(name);
		a.setDes(des);
		a.setEvaluation(evaluation);
		a.setWeightage(weightage);

		for(EvaluationCriterion ac : criterions){
			a.addCriterion(ac.clone());
		}
		return a;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
	public List<EvaluationCriterion> getCriterions() {
		return criterions;
	}
	public void setCriterions(List<EvaluationCriterion> criterions) {
		this.criterions = criterions;
	}
	public void addCriterion(EvaluationCriterion ac){
		criterions.add(ac);
	}
	public void removeCriterion(EvaluationCriterion ac){
		criterions.remove(ac);
	}
	
	public Evaluation getEvaluation() {
		return evaluation;
	}
	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}
	public void setWeightage(Integer weightage) {
		this.weightage = weightage;
	}
	public Integer getWeightage() {
		if(weightage==null)
			return 0;
		return weightage;
	}
	public Integer getMaxScore(){
		if(maxScore!=null)
			return maxScore;
		
		int max = 0;
		for(EvaluationCriterion rcion : criterions){
			max = Math.max(rcion.getScore(), max);
		}
		maxScore = max;
		return max;
	}
	public Integer getMinScore(){
		if(minScore!=null)
			return minScore;
		
		int min = Integer.MAX_VALUE;
		for(EvaluationCriterion rcion : criterions){
			min = Math.min(rcion.getScore(), min);
		}
		minScore = min;
		return min;
	}
	
}
