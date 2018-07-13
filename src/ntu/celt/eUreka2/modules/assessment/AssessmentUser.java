package ntu.celt.eUreka2.modules.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.internal.Util;

public class AssessmentUser implements Cloneable{
	private long id;
	private User assessor;
	private User assessee;
	private Assessment assessment;

	private Date assessedDate;
	private float totalScore; //sum of selectedCriterions' score, or absolute score (if not use criteria to grade)
	private List<AssessCriterion> selectedCriterions = new ArrayList<AssessCriterion>();
	private List<AssmtUserCriteriaGrade> selectedCritGrades  = new ArrayList<AssmtUserCriteriaGrade>();
	
	private List<String> selectedGMATHL = new ArrayList<String>();
	
	private String comments;
	private boolean studentViewComments = true;
	private String totalGrade;  //grade used to display, e.g: A+, A, B .... if NULL totalScore is shown (allowViewGrade was set) 
	private List<AssmtFile> files = new ArrayList<AssmtFile>();
	private List<AssmtInstructorFile> instructorFiles = new ArrayList<AssmtInstructorFile>();

	private List<Date> assesseeViewTimes = new ArrayList<Date>();

		
	
	public AssessmentUser clone(){
		AssessmentUser au = new AssessmentUser();
		//au.setId(id);
		au.setAssessor(assessor);
		au.setAssessee(assessee);
		au.setAssessment(assessment);
		au.setAssessedDate(assessedDate);
		au.setTotalScore(totalScore);
		au.setComments(comments);
		au.setStudentViewComments(studentViewComments);
		au.setTotalGrade(totalGrade);
		
		//not do this because it can conflict with AssessCriterion.id
		//so, graded data is not cloned
	/*	for(AssessCriterion ac : selectedCriterions){  
			au.getSelectedCriterions().add(ac.clone());
		}
	*/	
		return au;
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
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public Date getAssessedDate() {
		return assessedDate;
	}
	public void setAssessedDate(Date assessedDate) {
		this.assessedDate = assessedDate;
	}
	public float getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(float totalScore) {
		this.totalScore = totalScore;
	}
	public String getTotalScoreDisplay() {
		if(assessment.getCriterias().size()>0 && selectedCriterions.size() == 0)
			return "-";
		return Util.formatDecimal(totalScore);
	}
	public List<AssessCriterion> getSelectedCriterions() {
		return selectedCriterions;
	}
	public void setSelectedCriterions(List<AssessCriterion> selectedCriterions) {
		
		this.selectedCriterions = selectedCriterions;
	}
	public String getAssessedDateDisplay(){
		return Util.formatDateTime2(assessedDate);
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getComments() {
		return comments;
	}
	public void setStudentViewComments(boolean studentViewComments) {
		this.studentViewComments = studentViewComments;
	}
	public boolean isStudentViewComments() {
		return studentViewComments;
	}

	public void setTotalGrade(String totalGrade) {
		this.totalGrade = totalGrade;
	}

	public String getTotalGrade() {
		if(assessment==null)
			return totalGrade;
		if(	assessment.getCriterias().size()>0 
				&& selectedCriterions.size() == 0)
			return "-";
		return totalGrade;
	}

	public void setSelectedCritGrades(List<AssmtUserCriteriaGrade> selectedCritGrades) {
		this.selectedCritGrades = selectedCritGrades;
	}

	public List<AssmtUserCriteriaGrade> getSelectedCritGrades() {
		return selectedCritGrades;
	}

	public void setFiles(List<AssmtFile> files) {
		this.files = files;
	}
	public List<AssmtFile> getFiles() {
		return files;
	}
	public void addFile(AssmtFile f) {
		files.add(f);
	}
	public void removeFile(AssmtFile f) {
		files.remove(f);
	}
	
	public void setInstructorFiles(List<AssmtInstructorFile> Instructorfiles) {
		this.instructorFiles = Instructorfiles;
	}
	public List<AssmtInstructorFile> getInstructorFiles() {
		return instructorFiles;
	}
	public void addInstructorFile(AssmtInstructorFile f) {
		instructorFiles.add(f);
	}
	public void removeInstructorFile(AssmtInstructorFile f) {
		instructorFiles.remove(f);
	}
	public boolean hasComments(){
		if(this.comments != null && !this.comments.trim().isEmpty() )
			return true;
		return false;
	}
	public boolean hasCritComments(){
		for(AssmtUserCriteriaGrade auCritGrade : this.selectedCritGrades ){
			String comment = auCritGrade.getComment();
			if(comment != null && !comment.trim().isEmpty() )
				return true;
		}
		return false;
	}

	public void setSelectedGMATHL(List<String> selectedGMATHL) {
		this.selectedGMATHL = selectedGMATHL;
	}

	public List<String> getSelectedGMATHL() {
		return selectedGMATHL;
	}

	public void setAssesseeViewTimes(List<Date> assesseeViewTime) {
		this.assesseeViewTimes = assesseeViewTime;
	}
	public List<Date> getAssesseeViewTimes() {
		return assesseeViewTimes;
	}
	public void addAssesseeViewTimes(Date t) {
		assesseeViewTimes.add(t);
	}
	public void removeAssesseeViewTimes(Date t) {
		assesseeViewTimes.remove(t);
	}
	public String getFirstAssesseeViewTimeDisplay() {
		if(assesseeViewTimes.size()>0)
			return Util.formatDateTime(assesseeViewTimes.get(0));
		return "";
	}
	public String getLastAssesseeViewTimeDisplay() {
		if(assesseeViewTimes.size()>1)
			return Util.formatDateTime(assesseeViewTimes.get(assesseeViewTimes.size() -1));
		return "";
	}
	
}
