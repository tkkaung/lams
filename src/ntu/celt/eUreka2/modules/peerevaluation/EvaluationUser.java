package ntu.celt.eUreka2.modules.peerevaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class EvaluationUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6013759074627883496L;
	private long id;
	private User assessor;
	private User assessee;
	private Evaluation evaluation;

	private Date assessedDate;
	private List<EvaluationCriterion> selectedCriterions = new ArrayList<EvaluationCriterion>();
	private List<EvaluationCriterion> selectedCriterionsByLeader = new ArrayList<EvaluationCriterion>();
	
	private boolean submited;
	private String cmtStrength;
	private String cmtWeakness;
	private String cmtOther;
	private String editedCmtStrength;
	private String editedCmtWeakness;
	private String editedCmtOther;
	private User editedPerson;
	private Date editedDate;
	

	private List<String> openEndedQuestionAnswers = new ArrayList<String>();

	
	
	public EvaluationUser(){
		super();
		id = Util.generateLongID();
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public User getAssessor() {
		return assessor;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public User getAssessee() {
		return assessee;
	}
	public void setAssessee(User assessee) {
		this.assessee = assessee;
	}
	public Evaluation getEvaluation() {
		return evaluation;
	}
	public void setEvaluation(Evaluation assessment) {
		this.evaluation = assessment;
	}
	public Date getAssessedDate() {
		return assessedDate;
	}
	public void setAssessedDate(Date assessedDate) {
		this.assessedDate = assessedDate;
	}
	public List<EvaluationCriterion> getSelectedCriterions() {
		return selectedCriterions;
	}
	public void setSelectedCriterions(List<EvaluationCriterion> selectedCriterions) {
		this.selectedCriterions = selectedCriterions;
	}
	public void addSelectedCriterions(EvaluationCriterion selectedCriterions) {
		this.selectedCriterions.add(selectedCriterions);
	}

	public List<EvaluationCriterion> getSelectedCriterionsByLeader() {
		return selectedCriterionsByLeader;
	}
	public void setSelectedCriterionsByLeader(
			List<EvaluationCriterion> selectedCriterionsByLeader) {
		this.selectedCriterionsByLeader = selectedCriterionsByLeader;
	}
	
	public boolean hasEditedCritByLeader(EvaluationCriteria crit){
		for(EvaluationCriterion ec : selectedCriterionsByLeader){
			if(ec.getCriteria().equals(crit)){
				return true;
			}
		}
		return false;
	}
	
	public void addOrUpdateSelCritLeader(EvaluationCriterion ecrion){
		for(EvaluationCriterion ec : selectedCriterionsByLeader){
			if(ec.getCriteria().equals(ecrion.getCriteria())){
				selectedCriterionsByLeader.remove(ec);
				selectedCriterionsByLeader.add(ecrion);
				return;
			}
		}
		selectedCriterionsByLeader.add(ecrion);
	}
	
	public String getAssessedDateDisplay(){
		return Util.formatDateTime2(assessedDate);
	}
	
	public String getCmtStrength() {
		return cmtStrength;
	}
	public void setCmtStrength(String cmtStrength) {
		this.cmtStrength = cmtStrength;
	}
	public String getCmtStrengthDisplay() {
		if(editedCmtStrength!=null)
			return editedCmtStrength;
		return Util.nvl(cmtStrength);
	}
	
	public String getCmtWeakness() {
		return cmtWeakness;
	}
	public void setCmtWeakness(String cmtWeakness) {
		this.cmtWeakness = cmtWeakness;
	}
	public String getCmtWeaknessDisplay() {
		if(editedCmtWeakness!=null){
			return editedCmtWeakness;
		}
		return Util.nvl(cmtWeakness);
	}
	public boolean hasEditedWeaknessByLeader(){
		if(editedCmtWeakness==null || editedCmtWeakness.isEmpty())
			return false;
		if( !editedCmtWeakness.equals(cmtWeakness))
			return true;
		return false;
	}
	public String getCmtOther() {
		return cmtOther;
	}
	public void setCmtOther(String cmtOther) {
		this.cmtOther = cmtOther;
	}
	public String getCmtOtherDisplay() {
		if(editedCmtOther!=null){
			return editedCmtOther;
		}
		return Util.nvl(cmtOther);
	}
	public boolean hasEditedOtherByLeader(){
		if(editedCmtOther==null || editedCmtOther.isEmpty())
			return false;
		if( !editedCmtOther.equals(cmtOther))
			return true;
		return false;
	}
	
	public boolean hasEditedStrengthByLeader(){
		if(editedCmtStrength==null || editedCmtStrength.isEmpty())
			return false;
		if(!editedCmtStrength.equals(cmtStrength))
			return true;
		return false;
	}
	public String getEditedCmtStrength() {
		return editedCmtStrength;
	}
	public void setEditedCmtStrength(String editedCmtStrength) {
		this.editedCmtStrength = editedCmtStrength;
	}
	public String getEditedCmtWeakness() {
		return editedCmtWeakness;
	}
	public void setEditedCmtWeakness(String editedCmtWeakness) {
		this.editedCmtWeakness = editedCmtWeakness;
	}
	public String getEditedCmtOther() {
		return editedCmtOther;
	}
	public void setEditedCmtOther(String editedCmtOther) {
		this.editedCmtOther = editedCmtOther;
	}
	
	public boolean isSubmited() {
		return submited;
	}
	public void setSubmited(boolean submited) {
		this.submited = submited;
	}
	
	public float getTotalScore(){
		float total = 0;
		for(EvaluationCriterion ec : getSelectedCriterions()){
			total += (double) ec.getScore()* ec.getCriteria().getWeightage() /ec.getCriteria().getMaxScore();
		}
		return total;
	}
	public float getTotalScoreAfterAmend(){
		float total = 0;
		for(EvaluationCriterion ec : getSelectedCriterionsDisplay()){
			total += (double) ec.getScore()* ec.getCriteria().getWeightage() /ec.getCriteria().getMaxScore();
		}
		return total;
	}
	
	private EvaluationCriterion getEditedCriterion(EvaluationCriteria crit){
		for(EvaluationCriterion ec : getSelectedCriterionsByLeader()){
			if(ec.getCriteria().equals(crit))
				return ec;
		}
		return null;
	}
	
	public Set<EvaluationCriterion> getSelectedCriterionsDisplay(){
		Set<EvaluationCriterion> crionSet = new HashSet<EvaluationCriterion>();
		
		for(EvaluationCriterion ec : selectedCriterions){
			EvaluationCriterion ecEdited = getEditedCriterion(ec.getCriteria());
			if(ecEdited==null){
				crionSet.add(ec);
			}
			else{
				crionSet.add(ecEdited);
			}
		}
		for(EvaluationCriterion ec : getSelectedCriterionsByLeader()){
			crionSet.add(ec);
		}
		
		return crionSet;
	}
	
	public Integer getCritScoreEdited(EvaluationCriteria crit){
		for(EvaluationCriterion ac : selectedCriterionsByLeader){
			if(ac.getCriteria().equals(crit)){
				return ac.getScore();
			}
		}
		return null;
	}
	
	public List<String> getOpenEndedQuestionAnswers() {
		return openEndedQuestionAnswers;
	}

	public void setOpenEndedQuestionAnswers(List<String> openEndedQuestionAnswers) {
		this.openEndedQuestionAnswers = openEndedQuestionAnswers;
	}
	public void addOpenEndedQuestionAnswers(String rq){
		openEndedQuestionAnswers.add(rq);
		
	}
	public void removeOpenEndedQuestionAnswers(String rq){
		openEndedQuestionAnswers.remove(rq);	
	}
	
	public String getOpenEndedQuestionAnswers(int index){
		if(index >= openEndedQuestionAnswers.size())
			return "";
		return openEndedQuestionAnswers.get(index);
	}
	public User getEditedPerson() {
		return editedPerson;
	}
	public void setEditedPerson(User editedPerson) {
		this.editedPerson = editedPerson;
	}
	public Date getEditedDate() {
		return editedDate;
	}
	public void setEditedDate(Date editedDate) {
		this.editedDate = editedDate;
	}
	
	
	
}
